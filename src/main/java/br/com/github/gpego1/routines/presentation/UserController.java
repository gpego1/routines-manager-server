//package br.com.github.gpego1.routines.presentation;
//import br.com.github.gpego1.routines.dto.requests.RegisterUserRequest;
//import br.com.github.gpego1.routines.dto.responses.UserResponseDto;
//import br.com.github.gpego1.routines.service.UserService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/v1/users")
//@RequiredArgsConstructor
//public class UserController {
//    private final UserService userService;
//
//    @PostMapping
//    public ResponseEntity<?> createUser(@RequestBody @Valid RegisterUserRequest req) {
//        userService.createUser(req);
//        return new ResponseEntity<>(HttpStatus.CREATED);
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<UserResponseDto> findUserById(@PathVariable Integer id) {
//        UserResponseDto res = userService.findUserById(id);
//        return new ResponseEntity<>(res, HttpStatus.OK);
//    }
//
//
//}
