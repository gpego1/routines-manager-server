package br.com.github.gpego1.routines.service;
import br.com.github.gpego1.routines.database.model.UserEntity;
import br.com.github.gpego1.routines.database.repository.IProgressRepository;
import br.com.github.gpego1.routines.database.repository.IUserRepository;
import br.com.github.gpego1.routines.dto.responses.ProgressResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressService {
    private final IProgressRepository progressRepository;
    private final IUserRepository userRepository;

    public List<ProgressResponseDto> findProgressByUserId(Integer userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found.")
        );

        return progressRepository.findProgressByUserId(userId).stream()
                .map(p -> new ProgressResponseDto(
                        p.getId(),p.getLevel(), p.getStreak())
                )
                .toList();
    }
}
