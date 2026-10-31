package br.com.github.gpego1.routines.database.repository;

import br.com.github.gpego1.routines.database.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserRepository extends JpaRepository<UserEntity, Integer> {
}
