package com.servicehub.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "c2VydmljZWh1Yi10ZXN0LXNlY3JldC1rZXktYXQtbGVhc3QtMjU2LWJpdHM=";
    private final UserDetails user = User.withUsername("ana@email.com").password("x").roles("CLIENT").build();

    @Test
    void deveGerarTokenValidoComSubjectDoUsuario() {
        JwtService jwt = new JwtService(SECRET, 60_000);
        String token = jwt.generateToken(user);

        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
        assertThat(jwt.extractUsername(token)).isEqualTo("ana@email.com");
        assertThat(jwt.isValid(token, user)).isTrue();
    }

    @Test
    void deveRejeitarTokenAdulterado() {
        JwtService jwt = new JwtService(SECRET, 60_000);
        String token = jwt.generateToken(user);
        String adulterado = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "BB" : "AA");

        assertThat(jwt.isValid(adulterado, user)).isFalse();
    }

    @Test
    void deveRejeitarTokenExpirado() {
        JwtService jwt = new JwtService(SECRET, -1_000); // já nasce expirado
        String token = jwt.generateToken(user);

        assertThat(jwt.isValid(token, user)).isFalse();
    }
}
