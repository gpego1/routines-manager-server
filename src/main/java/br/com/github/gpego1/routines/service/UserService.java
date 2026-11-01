//package br.com.github.gpego1.routines.service;
//
//import br.com.github.gpego1.routines.database.model.RolesEntity;
//import br.com.github.gpego1.routines.database.model.UserEntity;
//import br.com.github.gpego1.routines.database.repository.IRolesRepository;
//import br.com.github.gpego1.routines.database.repository.IUserRepository;
//import br.com.github.gpego1.routines.dto.requests.RegisterUserRequest;
//import br.com.github.gpego1.routines.dto.responses.UserResponseDto;
//import br.com.github.gpego1.routines.enums.RoleEnumType;
//import lombok.RequiredArgsConstructor;
//import org.apache.coyote.BadRequestException;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.Set;
//
//@Service
//@RequiredArgsConstructor
//public class UserService {
//    private final IUserRepository userRepository;
//    private final IRolesRepository rolesRepository;
//
//    public void createUser(RegisterUserRequest dto) {
//        if (userRepository.findByEmail(dto.email()).isPresent()) throw new RuntimeException("User Already registered");
//
//        RolesEntity role = rolesRepository.findByName(String.valueOf(dto.role()))
//                .orElseThrow(() -> new RuntimeException("Role not found."));
//
//        userRepository.save(
//                 UserEntity.builder()
//                        .name(dto.name())
//                        .email(dto.email())
//                        .password(dto.password())
//                         .roles(Set.of(role))
//                         .createdAt(LocalDateTime.now())
//                         .updatedAt(LocalDateTime.now())
//                         .build()
//        );
//    }
//
//    public UserResponseDto findUserById(Integer id) {
//        UserEntity user = userRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("User Not Found."));
//
//        return new UserResponseDto(
//                user.getId(),
//                user.getName(),
//                user.getEmail(),
//                user.getRoles(),
//                user.getProgress()
//        );
//    }
//
//
//
//
//}
