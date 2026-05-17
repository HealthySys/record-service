package br.unifor.healthsys.record.security;

public record AuthenticatedUser(
        Long userId,
        String username,
        String role,
        String email,
        String nome
) {
}
