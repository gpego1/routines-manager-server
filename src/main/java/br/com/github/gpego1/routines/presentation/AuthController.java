package br.com.github.gpego1.routines.presentation;
import br.com.github.gpego1.routines.dto.requests.LoginRequest;
import br.com.github.gpego1.routines.dto.requests.RegisterUserRequest;
import br.com.github.gpego1.routines.dto.responses.TokenResponseDto;
import br.com.github.gpego1.routines.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid RegisterUserRequest req) {
        authService.register(req);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody @Valid LoginRequest req) {
        return new ResponseEntity<>(authService.login(req), HttpStatus.OK);
    }

}
