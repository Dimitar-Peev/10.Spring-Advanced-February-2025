package app.user.service;

import app.aspect.VeryImportant;
import app.exception.DomainException;
import app.exception.UsernameAlreadyExistException;
import app.notification.service.NotificationService;
import app.security.AuthenticationMetadata;
import app.subscription.model.Subscription;
import app.subscription.service.SubscriptionService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionService subscriptionService;
    private final WalletService walletService;
    private final NotificationService notificationService;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       SubscriptionService subscriptionService,
                       WalletService walletService, NotificationService notificationService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.subscriptionService = subscriptionService;
        this.walletService = walletService;
        this.notificationService = notificationService;
    }

    // Register
    // Test 1: When user exist with this username -> exception is thrown
    // Test 2: Happy path
    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public User register(RegisterRequest registerRequest) {

        Optional<User> optionUser = userRepository.findByUsername(registerRequest.getUsername());
        if (optionUser.isPresent()) {
//            throw new DomainException("Username [%s] already exist.".formatted(registerRequest.getUsername()));
            throw new UsernameAlreadyExistException("Username [%s] already exist.".formatted(registerRequest.getUsername()));
        }

        User user = userRepository.save(initializeUser(registerRequest));

        Subscription defaultSubscription = subscriptionService.createDefaultSubscription(user);
        user.setSubscriptions(List.of(defaultSubscription)); // Has 1 subscription

        Wallet standardWallet = walletService.initilizeFirstWallet(user);
        user.setWallets(List.of(standardWallet)); // Has 1 wallet

        // Persist new notification preference with isEnabled = false
        notificationService.saveNotificationPreference(user.getId(), false, null);

        log.info("Successfully create new user account for username [%s] and id [%s]".formatted(user.getUsername(), user.getId()));

        return user;
    }

    // JoinPoint
    // Test Case: When there is no user in the database (repository returns Optional.empty())
    // -> then expect an exception of type DomainException is thrown
    @CacheEvict(value = "users", allEntries = true)
    public void editUserDetails(UUID userId, UserEditRequest userEditRequest) {

        User user = getById(userId);

//        if (userEditRequest.getEmail().isBlank()) {
//            notificationService.saveNotificationPreference(userId, false, null);
//        }

        user.setFirstName(userEditRequest.getFirstName());
        user.setLastName(userEditRequest.getLastName());
        user.setEmail(userEditRequest.getEmail());
        user.setProfilePicture(userEditRequest.getProfilePicture());

        if (!userEditRequest.getEmail().isBlank()) {
            notificationService.saveNotificationPreference(userId, true, userEditRequest.getEmail());
        } else {
            notificationService.saveNotificationPreference(userId, false, null);
        }

        userRepository.save(user);

    }

    private User initializeUser(RegisterRequest registerRequest) {

        return User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.USER)
                .isActive(true)
                .country(registerRequest.getCountry())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    // В началото се изпълнява веднъж този метод и резултата се пази в кеш
    // Всяко следващо извикване на този метод ще се чете резултата от кеша и няма да се извиква четенето от базата
    @Cacheable("users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @VeryImportant
    public User getById(UUID id) {
//        return userRepository.findById(id).orElseThrow(() -> new DomainException("User with id [%s] does not exist.".formatted(id)));
        Optional<User> user = userRepository.findById(id);

        user.orElseThrow(() -> new DomainException("User with id [%s] does not exist.".formatted(id)));

        return user.get();
    }

    @CacheEvict(value = "users", allEntries = true)
    public void switchStatus(UUID userId) {

        User user = getById(userId);

        // НАЧИН 1:
//        if (user.isActive()){
//            user.setActive(false);
//        } else {
//            user.setActive(true);
//        }

        // false -> true
        // true -> false
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    // If user is ADMIN -> USER
    // If user is USER -> ADMIN
    @CacheEvict(value = "users", allEntries = true)
    public void switchRole(UUID userId) {

        User user = getById(userId);

        if (user.getRole() == UserRole.USER) {
            user.setRole(UserRole.ADMIN);
        } else {
            user.setRole(UserRole.USER);
        }

        userRepository.save(user);
    }

    // Всеки път, когато потребител се логва, Spring Security ще извиква този метод
    // за да вземе детайлите на потребителя с този username
    // Test 1: When user exist - then return new AuthenticationMetadata
    // Test 2: When User does not exist - then throws exception
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new DomainException("User with this username does not exist."));

        return new AuthenticationMetadata(user.getId(), username, user.getPassword(), user.getRole(), user.isActive());
    }
}
