package br.com.github.gpego1.routines.database.repository;
import br.com.github.gpego1.routines.database.model.TelegramLinkTokenEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;
public interface ITelegramLinkTokenRepository extends JpaRepository<TelegramLinkTokenEntity, Long> {
    @Query("select t.user.id from TelegramLinkTokenEntity t where t.token = :token")
    Optional<Integer> findUserIdByToken(String token);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TelegramLinkTokenEntity t where t.token = :token")
    Optional<TelegramLinkTokenEntity> lockByToken(String token);
    @Modifying
    @Query("update TelegramLinkTokenEntity t set t.consumedAt = :now where t.user.id = :userId and t.consumedAt is null")
    void invalidateByUserId(Integer userId, java.time.Instant now);
}
