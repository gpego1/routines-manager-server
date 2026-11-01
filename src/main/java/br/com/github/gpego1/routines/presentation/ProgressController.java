package br.com.github.gpego1.routines.presentation;
import br.com.github.gpego1.routines.dto.responses.ProgressResponseDto;
import br.com.github.gpego1.routines.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/progress")
@RequiredArgsConstructor
public class ProgressController {
    private final ProgressService progressService;

    //@PreAuthorize("#username == authentication.principal.username or hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProgressResponseDto>> findProgressByUserID(@PathVariable(name = "userId") Integer userId) {
        return new ResponseEntity<>(progressService.findProgressByUserId(userId), HttpStatus.OK);
    }
}
