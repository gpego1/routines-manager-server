package br.com.github.gpego1.routines.presentation;
import br.com.github.gpego1.routines.config.TelegramProperties;
import br.com.github.gpego1.routines.dto.requests.TelegramUpdate;
import br.com.github.gpego1.routines.service.TelegramLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Principal;
@RestController
@RequestMapping("/v1/telegram")
@RequiredArgsConstructor
public class TelegramController {
    private final TelegramLinkService links;
    private final TelegramProperties properties;
    @PostMapping("/link")
    public TelegramLinkService.LinkResponse link(Principal principal) { return links.createLink(principal.getName()); }
    @GetMapping("/status")
    public TelegramLinkService.StatusResponse status(Principal principal) { return links.status(principal.getName()); }
    @DeleteMapping("/link")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlink(Principal principal) { links.unlink(principal.getName()); }
    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void webhook(@RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secret,
                        @RequestBody TelegramUpdate update) {
        if (properties.getWebhookSecret().isBlank() || secret == null || !MessageDigest.isEqual(
                secret.getBytes(StandardCharsets.UTF_8), properties.getWebhookSecret().getBytes(StandardCharsets.UTF_8)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        links.receive(update);
    }
}
