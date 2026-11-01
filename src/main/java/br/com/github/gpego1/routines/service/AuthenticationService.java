package br.com.github.gpego1.routines.service;
import br.com.github.gpego1.routines.config.TokenProvider;
import br.com.github.gpego1.routines.database.model.RolesEntity;
import br.com.github.gpego1.routines.database.model.UserEntity;
import br.com.github.gpego1.routines.database.repository.IRolesRepository;
import br.com.github.gpego1.routines.database.repository.IUserRepository;
import br.com.github.gpego1.routines.dto.requests.LoginRequest;
import br.com.github.gpego1.routines.dto.requests.RegisterUserRequest;
import br.com.github.gpego1.routines.dto.responses.TokenResponseDto;
import br.com.github.gpego1.routines.dto.responses.UserResponseDto;
import br.com.github.gpego1.routines.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    @Value("${jwt.expiration}")
    private long expirationTime;

    private final IUserRepository userRepository;
    private final IRolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final AuthenticationManager authManager;


    public void register(RegisterUserRequest dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) throw new RuntimeException("User Already registered");

        RolesEntity role = rolesRepository.findByName(String.valueOf(dto.role()))
                .orElseThrow(() -> new RuntimeException("Role not found."));

        userRepository.save(
                UserEntity.builder()
                        .name(dto.name())
                        .email(dto.email())
                        .password(passwordEncoder.encode(dto.password()))
                        .roles(Set.of(role))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );
    }


    public TokenResponseDto login(LoginRequest loginReq) {
        try {
            Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(loginReq.email(), loginReq.password()));
            String token = tokenProvider.generateToken(auth);
            return new TokenResponseDto(token, expirationTime);
        } catch (BadRequestException e) {
            throw new BadRequestException("Invalid Email or Password");
        } catch (Exception e) {
            throw e;
        }
    }

    public UserResponseDto getUserInfo(String token) {
        String username = tokenProvider.catchUsername(token);
        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new BadRequestException("Invalid Email or password.")
        );
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles()
        );

    }
}
