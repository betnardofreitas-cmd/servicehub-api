package com.servicehub.service;

import com.servicehub.dto.AuthResponse;
import com.servicehub.dto.LoginRequest;
import com.servicehub.dto.RegisterRequest;
import com.servicehub.dto.UserResponse;
import com.servicehub.exception.EmailAlreadyExistsException;
import com.servicehub.exception.InvalidRequestException;
import com.servicehub.model.AppUser;
import com.servicehub.model.Role;
import com.servicehub.repository.UserRepository;
import com.servicehub.security.JwtService;
import com.servicehub.util.InputSanitizer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InputSanitizer sanitizer;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
                       UserRepository userRepository, PasswordEncoder passwordEncoder,
                       InputSanitizer sanitizer) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sanitizer = sanitizer;
    }

    /** Valida as credenciais via AuthenticationManager (BCrypt) e devolve um JWT. Senha errada -> BadCredentialsException (401). */
    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(sanitizer.email(request.email()), request.password()));

        UserDetails user = (UserDetails) auth.getPrincipal();
        String role = user.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return new AuthResponse(jwtService.generateToken(user), "Bearer", jwtService.getExpirationSeconds(), role);
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (request.role() == Role.ADMIN) {
            throw new InvalidRequestException("Não é permitido se cadastrar como ADMIN.");
        }
        String email = sanitizer.email(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Já existe um usuário cadastrado com este e-mail.");
        }
        AppUser user = new AppUser(sanitizer.text(request.name()), email,
                passwordEncoder.encode(request.password()), request.role());
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    private UserResponse toResponse(AppUser u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole().name());
    }
}
