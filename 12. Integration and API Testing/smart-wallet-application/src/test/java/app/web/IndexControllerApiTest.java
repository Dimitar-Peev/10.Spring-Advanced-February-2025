package app.web;

import app.exception.UsernameAlreadyExistException;
import app.security.AuthenticationMetadata;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static app.TestBuilder.aRandomUser;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
public class IndexControllerApiTest {

    // ВАЖНО: Когато тествам контролери трябва да Mock-на всички dependencies на този контролер с анотация @MockitoBean!!!
    @MockitoBean
    private UserService userService;

    // Използвам MockMvc за да изпращам заявки
    @Autowired
    private MockMvc mockMvc;

    // Send @GetMapping("/")
    // Result -> view name "index"
    @Test
    void getRequestToIndexEndpoint_shouldReturnIndexView() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = get("/");

        // MockMvcResultMatchers.status() - проверка на статуса
        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())                        //.andExpect() - проверявам резултата
                .andExpect(view().name("index"));  //.andExpect() - проверявам резултата
    }

    @Test
    void getRequestToRegisterEndpoint_shouldReturnRegisterView() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = get("/register");

        //.andExpect() - проверявам резултата
        // MockMvcResultMatchers.status() - проверка на статуса
        // model().attributeExists("registerRequest") - проверява дали има конкретен NOT NULL атрибут -> AssertionErrors.assertNotNull
        // model().attribute("registerRequest", instanceOf(RegisterRequest.class))
        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"))
                .andExpect(model().attribute("registerRequest", instanceOf(RegisterRequest.class))); // проверява дали атрибута registerRequest е от тип RegisterRequest
                                                                    // import static org.hamcrest.Matchers.instanceOf;
    }

    @Test
    void getRequestToLoginEndpoint_shouldReturnLoginView() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = get("/login");

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    void getRequestToLoginEndpointWithErrorParameter_shouldReturnLoginViewAndErrorMessageAttribute() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = get("/login").param("error", "");

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest", "errorMessage"));
    }

    // POST with correct form data
    // Expect:
    // status - 3xx Redirect Status
    // called .register method of userService
    // redirect to /login
    @Test
    void postRequestToRegisterEndpoint_happyPath() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Dimitar")
                .formField("password", "123456")
                .formField("country", "BULGARIA")
                .with(csrf()); // spring security - защита от CSRF атаки

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // Verify that userService.register(...) was called once
        verify(userService, times(1)).register(any());
    }

    @Test
    void postRequestToRegisterEndpointWithInvalidData_returnRegisterView() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "")
                .formField("password", "")
                .formField("country", "BULGARIA")
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"));

        // Verify that userService.register(...) was never called
        verify(userService, never()).register(any());
    }

    // Testing Secured Endpoints
    @Test
    void getAuthenticatedRequestToHome_returnsHomeView() throws Exception {

        // 1. Build Request
        when(userService.getById(any())).thenReturn(aRandomUser());

        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/home")
                .with(user(principal));

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user"));

        // Verify that userService.getById(...) was called once
        verify(userService, times(1)).getById(userId);
    }

    @Test
    void getUnauthenticatedRequestToHome_redirectToLogin() throws Exception {

        // 1. Build Request
        MockHttpServletRequestBuilder request = get("/home");

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        // Verify that userService.getById(...) was never called
        verify(userService, never()).getById(any());
    }

    // Testing Exception Handling
    @Test
    void postRequestToRegisterEndpointWhenUsernameAlreadyExist_thenRedirectToRegisterWithFlashParameter() throws Exception {

        // 1. Build Request
        when(userService.register(any())).thenThrow(new UsernameAlreadyExistException("Username already exist!"));

        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "Dimitar")
                .formField("password", "123456")
                .formField("country", "BULGARIA")
                .with(csrf());

        // 2. Send Request
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("usernameAlreadyExistMessage"));

        // Verify that userService.register(...) was called once
        verify(userService, times(1)).register(any());
    }
}
