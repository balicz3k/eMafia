package com.mafia.services;

import com.mafia.components.JwtTokenProvider;
import com.mafia.dto.AuthResponse;
import com.mafia.dto.LoginRequest;
import com.mafia.dto.RegistrationRequest;
import com.mafia.dto.UserResponse;
import com.mafia.exceptions.*;
import com.mafia.models.Role;
import com.mafia.models.User;
import com.mafia.repositories.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;
    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);

        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }

    @AfterEach
    void tearDown() {
        if (mockedSecurityContextHolder != null) {
            mockedSecurityContextHolder.close();
        }
    }

    private User setupAuthenticationPrincipal(UUID userId, String username, String email, String password) {
        User principalUser = new User();
        principalUser.setId(userId);
        principalUser.setUsername(username);
        principalUser.setEmail(email);
        principalUser.setPassword(password); // Encoded password
        principalUser.setRoles(Collections.singleton(Role.ROLE_USER));

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principalUser);

        // Dopiero potem przekaż skonfigurowany mock 'authentication' do
        // 'securityContext'
        when(securityContext.getAuthentication()).thenReturn(authentication);
        return principalUser;
    }

    // Tests for registerUser
    @Test
    void registerUser_success() {
        RegistrationRequest request = new RegistrationRequest("testuser", "test@example.com", "password");
        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setUsername(request.getUsername());
        savedUser.setEmail(request.getEmail());
        savedUser.setRoles(Collections.singleton(Role.ROLE_USER));

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateToken(savedUser)).thenReturn("testToken");

        AuthResponse response = userService.registerUser(request);

        assertNotNull(response);
        assertEquals("testToken", response.getToken());
        verify(userRepository).save(argThat(user -> user.getUsername().equals(request.getUsername()) &&
                user.getEmail().equals(request.getEmail()) &&
                user.getPassword().equals("encodedPassword") &&
                user.getRoles().contains(Role.ROLE_USER)));
    }

    @Test
    void registerUser_emailAlreadyExists() {
        RegistrationRequest request = new RegistrationRequest("testuser", "test@example.com", "password");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_usernameAlreadyExists() {
        RegistrationRequest request = new RegistrationRequest("testuser", "test@example.com", "password");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    // Tests for authenticateUser
    @Test
    void authenticateUser_success() {
        LoginRequest request = new LoginRequest("test@example.com", "password");
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.getEmail());
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(user)).thenReturn("testToken");

        AuthResponse response = userService.authenticateUser(request);

        assertNotNull(response);
        assertEquals("testToken", response.getToken());
    }

    @Test
    void authenticateUser_userNotFound() {
        LoginRequest request = new LoginRequest("test@example.com", "password");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.authenticateUser(request));
    }

    @Test
    void authenticateUser_invalidPassword() {
        LoginRequest request = new LoginRequest("test@example.com", "password");
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), "encodedPassword")).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> userService.authenticateUser(request));
    }

    // Tests for searchUsers
    @Test
    void searchUsers_success() {
        String query = "test";
        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setUsername("testuser1");
        user1.setEmail("t1@e.com");
        user1.setRoles(Collections.singleton(Role.ROLE_USER));
        List<User> users = Collections.singletonList(user1);
        when(userRepository.findByUsernameContainingIgnoreCase(query)).thenReturn(users);

        List<UserResponse> responses = userService.searchUsers(query);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("testuser1", responses.get(0).getUsername());
    }

    // Tests for updateUsername
    @Test
    void updateUsername_success() {
        UUID userId = UUID.randomUUID();
        setupAuthenticationPrincipal(userId, "oldUsername", "user@example.com", "encodedPass");

        User userFromDb = new User();
        userFromDb.setId(userId);
        userFromDb.setUsername("oldUsername");
        userFromDb.setEmail("user@example.com");
        userFromDb.setRoles(Collections.singleton(Role.ROLE_USER));
        String newUsername = "newUsername";

        when(userRepository.findById(userId)).thenReturn(Optional.of(userFromDb));
        when(userRepository.existsByUsername(newUsername)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUsername(newUsername);

        assertEquals(newUsername, response.getUsername());
        verify(userRepository).save(argThat(u -> u.getUsername().equals(newUsername)));
    }

    @Test
    void updateUsername_success_sameUsername() {
        UUID userId = UUID.randomUUID();
        String currentUsername = "currentUsername";
        setupAuthenticationPrincipal(userId, currentUsername, "user@example.com", "encodedPass");

        User userFromDb = new User();
        userFromDb.setId(userId);
        userFromDb.setUsername(currentUsername);
        userFromDb.setEmail("user@example.com");
        userFromDb.setRoles(Collections.singleton(Role.ROLE_USER));

        when(userRepository.findById(userId)).thenReturn(Optional.of(userFromDb));
        // No need to mock existsByUsername if username is the same, as the condition
        // `!user.getUsername().equals(newUsername)` will be false.
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUsername(currentUsername); // Using the same username

        assertEquals(currentUsername, response.getUsername());
        verify(userRepository).save(userFromDb);
    }

    @Test
    void updateUsername_usernameAlreadyTaken() {
        UUID userId = UUID.randomUUID();
        setupAuthenticationPrincipal(userId, "oldUsername", "user@example.com", "encodedPass");
        User userFromDb = new User();
        userFromDb.setId(userId);
        userFromDb.setUsername("oldUsername");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userFromDb));
        when(userRepository.existsByUsername("takenUsername")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> userService.updateUsername("takenUsername"));
    }

    @Test
    void updateUsername_whenAuthenticationIsNull_thenThrowsUserNotFoundException() {
        when(securityContext.getAuthentication()).thenReturn(null);
        assertThrows(UserNotFoundException.class, () -> userService.updateUsername("newUsername"));
    }

    // Tests for updateEmail
    @Test
    void updateEmail_success() {
        UUID userId = UUID.randomUUID();
        setupAuthenticationPrincipal(userId, "testuser", "old@example.com", "encodedPass");
        User userFromDb = new User();
        userFromDb.setId(userId);
        userFromDb.setUsername("testuser");
        userFromDb.setEmail("old@example.com");
        userFromDb.setRoles(Collections.singleton(Role.ROLE_USER));
        String newEmail = "new@example.com";

        when(userRepository.findById(userId)).thenReturn(Optional.of(userFromDb));
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateEmail(newEmail);

        assertEquals(newEmail, response.getEmail());
        verify(userRepository).save(argThat(u -> u.getEmail().equals(newEmail)));
    }

    @Test
    void updateEmail_success_sameEmail() {
        UUID userId = UUID.randomUUID();
        String currentEmail = "current@example.com";
        setupAuthenticationPrincipal(userId, "testuser", currentEmail, "encodedPass");
        User userFromDb = new User();
        userFromDb.setId(userId);
        userFromDb.setUsername("testuser");
        userFromDb.setEmail(currentEmail);
        userFromDb.setRoles(Collections.singleton(Role.ROLE_USER));

        when(userRepository.findById(userId)).thenReturn(Optional.of(userFromDb));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateEmail(currentEmail);

        assertEquals(currentEmail, response.getEmail());
        verify(userRepository).save(userFromDb);
    }

    @Test
    void updateEmail_emailAlreadyRegistered() {
        UUID userId = UUID.randomUUID();
        setupAuthenticationPrincipal(userId, "testuser", "old@example.com", "encodedPass123!");
        User userFromDb = new User();
        userFromDb.setId(userId);
        userFromDb.setEmail("old@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userFromDb));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.updateEmail("taken@example.com"));
    }

    // Tests for updatePassword
    @Test
    void updatePassword_success() {
        UUID userId = UUID.randomUUID();
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String encodedOldPassword = "encodedOldPassword";
        String encodedNewPassword = "encodedNewPassword";

        setupAuthenticationPrincipal(userId, "testuser", "user@example.com", encodedOldPassword);
        // User fetched from DB will have the encodedOldPassword
        User userFromDb = new User();
        userFromDb.setId(userId);
        userFromDb.setPassword(encodedOldPassword);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userFromDb));
        when(passwordEncoder.matches(oldPassword, encodedOldPassword)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        userService.updatePassword(oldPassword, newPassword);

        verify(userRepository).save(argThat(u -> u.getPassword().equals(encodedNewPassword)));
    }

    @Test
    void updatePassword_invalidOldPassword() {
        UUID userId = UUID.randomUUID();
        String oldPassword = "wrongOldPassword";
        String newPassword = "newPassword";
        String encodedPasswordInDb = "encodedCorrectOldPassword";

        setupAuthenticationPrincipal(userId, "testuser", "user@example.com", encodedPasswordInDb);
        User userFromDb = new User();
        userFromDb.setId(userId);
        userFromDb.setPassword(encodedPasswordInDb);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userFromDb));
        when(passwordEncoder.matches(oldPassword, encodedPasswordInDb)).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> userService.updatePassword(oldPassword, newPassword));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updatePassword_userNotFoundInDb() {
        UUID userId = UUID.randomUUID();
        setupAuthenticationPrincipal(userId, "testuser", "user@example.com", "encodedPass");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updatePassword("old", "new"));
    }

    // Tests for adminGetAllUsers
    @Test
    void adminGetAllUsers_success() {
        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setUsername("u1");
        user1.setEmail("u1@e.com");
        user1.setRoles(Collections.singleton(Role.ROLE_USER));
        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setUsername("u2");
        user2.setEmail("u2@e.com");
        user2.setRoles(Collections.singleton(Role.ROLE_ADMIN));
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<UserResponse> responses = userService.adminGetAllUsers();

        assertEquals(2, responses.size());
    }

    // Tests for adminUpdateUserRoles
    @Test
    void adminUpdateUserRoles_success() {
        UUID userId = UUID.randomUUID();
        User userToUpdate = new User();
        userToUpdate.setId(userId);
        userToUpdate.setRoles(Collections.singleton(Role.ROLE_USER));
        Set<Role> newRoles = Collections.singleton(Role.ROLE_ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userToUpdate));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.adminUpdateUserRoles(userId, newRoles);

        assertEquals(newRoles, response.getRoles());
        verify(userRepository).save(argThat(u -> u.getRoles().equals(newRoles)));
    }

    @Test
    void adminUpdateUserRoles_userNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.adminUpdateUserRoles(userId, Collections.emptySet()));
    }

    // Tests for adminDeleteUser
    @Test
    void adminDeleteUser_success() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        userService.adminDeleteUser(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void adminDeleteUser_userNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.adminDeleteUser(userId));
        verify(userRepository, never()).deleteById(any(UUID.class));
    }
}