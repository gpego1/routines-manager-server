package br.com.github.gpego1.routines.service;
import br.com.github.gpego1.routines.config.TelegramProperties;
import br.com.github.gpego1.routines.database.model.*;
import br.com.github.gpego1.routines.database.repository.*;
import br.com.github.gpego1.routines.dto.requests.TelegramUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
@Service
@RequiredArgsConstructor
public class TelegramLinkService {
    private final IUserRepository users;
    private final ITelegramLinkTokenRepository tokens;
    private final TelegramProperties properties;
    private final SecureRandom random = new SecureRandom();
    public record LinkResponse(String url, Instant expiresAt) {}
    public record StatusResponse(boolean enabled, Long chatId) {}
    @Transactional
    public LinkResponse createLink(String email) {
        if (properties.getBotUsername().isBlank() || properties.getBotToken().isBlank() || properties.getWebhookSecret().isBlank())
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Telegram nao configurado");
        UserEntity user = users.lockByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        tokens.invalidateByUserId(user.getId(), Instant.now());
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiresAt = Instant.now().plusSeconds(600);
        tokens.save(TelegramLinkTokenEntity.builder().token(token).user(user).expiresAt(expiresAt).build());
        return new LinkResponse("https://t.me/" + properties.getBotUsername() + "?start=" + token, expiresAt);
    }
    @Transactional(readOnly = true)
    public StatusResponse status(String email) {
        UserEntity user = users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return new StatusResponse(user.isTelegramEnabled(), user.getTelegramChatId());
    }
    @Transactional
    public void unlink(String email) {
        UserEntity user = users.lockByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        tokens.invalidateByUserId(user.getId(), Instant.now());
        user.setTelegramEnabled(false);
        user.setTelegramChatId(null);
    }
    @Transactional
    public void receive(TelegramUpdate update) {
        if (update.updateId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "update_id obrigatorio");
        var message = update.message();
        if (message == null || message.chat() == null || message.from() == null || message.from().bot()
                || !"private".equals(message.chat().type()) || message.chat().id() == null
                || !message.chat().id().equals(message.from().id()) || message.text() == null) return;
        String[] command = message.text().trim().split("\\s+", 2);
        if (command.length != 2 || !(command[0].equals("/start") || command[0].equals("/start@" + properties.getBotUsername()))) return;
        var userId = tokens.findUserIdByToken(command[1]).orElse(null);
        if (userId == null) return;
        // Always lock user before token, also in unlink/createLink.
        var user = users.lockById(userId).orElseThrow();
        var token = tokens.lockByToken(command[1]).orElse(null);
        Instant now = Instant.now();
        if (token == null || token.getConsumedAt() != null || token.getExpiresAt() == null || !token.getExpiresAt().isAfter(now)) return;
        user.setTelegramChatId(message.chat().id());
        user.setTelegramEnabled(true);
        token.setConsumedAt(now);
    }
}
