package com.servicehub.service;

import com.servicehub.dto.AuthResponse;
import com.servicehub.dto.LoginRequest;
import com.servicehub.dto.RegisterRequest;
import com.servicehub.exception.EmailAlreadyExistsException;
import com.servicehub.exception.InvalidRequestException;
import com.servicehub.model.AppUser;
import com.servicehub.model.Role;
import com.servicehub.repository.UserRepository;
import com.servicehub.security.JwtService;
import com.servicehub.util.InputSanitizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Spy  private InputSanitizer sanitizer = new InputSanitizer();

    @InjectMocks
    private AuthService authService;

    @Test
    void register_deveRecusarPapelAdmin() {
        RegisterRequest request = new RegisterRequest("Hacker", "h@x.com", "Senha123", Role.ADMIN);

        assertThrows(InvalidRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_deveSalvarSenhaComoHash() {
        RegisterRequest request = new RegisterRequest("Maria", "Maria@ServiceHub.com", "Senha123", Role.PROVIDER);
        when(userRepository.existsByEmail("maria@servicehub.com")).thenReturn(false);
        when(passwordEncoder.encode("Senha123")).thenReturn("$2a$10$hashFicticio");
        when(userRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(request);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("$2a$10$hashFicticio").isNotEqualTo("Senha123");
        assertThat(captor.getValue().getRole()).isEqualTo(Role.PROVIDER);
    }

    @Test
    void register_deveLancarConflito_quandoEmailJaCadastrado() {
        when(userRepository.existsByEmail("maria@servicehub.com")).thenReturn(true);
        RegisterRequest request = new RegisterRequest("Maria", "maria@servicehub.com", "Senha123", Role.CLIENT);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    void login_deveRetornarTokenComPapel_quandoCredenciaisValidas() {
        UserDetails user = User.withUsername("admin@servicehub.com").password("x").roles("ADMIN").build();
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(jwtService.generateToken(user)).thenReturn("token.jwt.fake");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        AuthResponse response = authService.login(new LoginRequest("admin@servicehub.com", "Admin@123"));

        assertThat(response.token()).isEqualTo("token.jwt.fake");
        assertThat(response.role()).isEqualTo("ADMIN");
    }

    @Test
    void login_devePropagarBadCredentials_quandoSenhaErrada() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("admin@servicehub.com", "errada")));
        verifyNoInteractions(jwtService);
    }
}
