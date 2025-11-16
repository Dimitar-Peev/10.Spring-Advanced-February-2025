package app;

import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionStatus;
import app.subscription.model.SubscriptionType;
import app.subscription.repository.SubscriptionRepository;
import app.user.model.Country;
import app.user.model.User;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.wallet.repository.WalletRepository;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
public class UserRegistrationITest {

    @Autowired
    private UserService userService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void whenNewUserRegister_thenDefaultSubscriptionAndActiveWalletCreated() {

        RegisterRequest registerRequest = new RegisterRequest("user1", "123123", Country.BULGARIA);

        User user = userService.register(registerRequest);

        assertNotNull(user);

        Optional<Subscription> subscription = subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, user.getId());
        assertTrue(subscription.isPresent());
        assertEquals("user1", subscription.get().getOwner().getUsername());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.get().getStatus());
        assertEquals(SubscriptionType.DEFAULT, subscription.get().getType());

        List<Wallet> wallets = walletRepository.findAllByOwnerUsername("user1");
        assertEquals(1, wallets.size());
        assertEquals("user1", wallets.get(0).getOwner().getUsername());
        assertEquals(WalletStatus.ACTIVE, wallets.get(0).getStatus());
    }
}
