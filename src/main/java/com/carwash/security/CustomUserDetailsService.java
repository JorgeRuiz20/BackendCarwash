package com.carwash.security;

import com.carwash.model.Usuario;
import com.carwash.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // ✅ CORRECCIÓN: Ahora busca por EMAIL directamente
        // El username en Spring Security será el EMAIL del usuario
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo");
        }

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + usuario.getTipo().name()));

        // ✅ CAMBIO PRINCIPAL: Usar EMAIL como username en lugar de ID
        // Esto permite que authentication.getName() devuelva el email
        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),  // ✅ ANTES: String.valueOf(usuario.getId())
                usuario.getPassword(),
                authorities);
    }
}