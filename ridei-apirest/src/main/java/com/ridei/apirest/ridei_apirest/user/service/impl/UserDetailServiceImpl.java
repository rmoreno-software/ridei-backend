package com.ridei.apirest.ridei_apirest.user.service.impl;

import com.ridei.apirest.ridei_apirest.user.model.entity.UserApp;
import com.ridei.apirest.ridei_apirest.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserApp user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("USUARIO NO ENCONTRADO CAMBIAR MESSAGES:PROPERTIES"));

        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return new User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true, true, true,
                authorities
        );
    }

}
