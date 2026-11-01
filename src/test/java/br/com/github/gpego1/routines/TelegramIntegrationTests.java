package br.com.github.gpego1.routines;

import br.com.github.gpego1.routines.config.TokenProvider;
import br.com.github.gpego1.routines.database.model.*;
import br.com.github.gpego1.routines.database.repository.*;
import br.com.github.gpego1.routines.service.*;
import br.com.github.gpego1.routines.enums.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TelegramIntegrationTests {
    @org.springframework.beans.factory.annotation.Value("${local.server.port}") int port;
    @Autowired IUserRepository users;
    @Autowired ITelegramLinkTokenRepository tokens;
    @Autowired ITaskRepository tasks;
    @Autowired ITaskReminderRepository reminders;
    @Autowired TokenProvider jwt;
    @Autowired TelegramLinkService links;
    @Autowired TaskReminderService reminderService;
    @Autowired TaskService taskService;
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();
    private UserEntity user;
    private String bearer;

    @BeforeEach void setup() {
        user = users.save(UserEntity.builder().name("Test").email(UUID.randomUUID() + "@test.local")
                .password("unused").timezone("America/Sao_Paulo").roles(Set.of()).build());
        bearer = jwt.generateToken(new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }
    private HttpResponse<String> request(String method, String path, String body, String auth, String secret) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        if (auth != null) builder.header("Authorization", "Bearer " + auth);
        if (secret != null) builder.header("X-Telegram-Bot-Api-Secret-Token", secret);
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
    private String payload(String token, String chatType) {
        // Telegram Update / Message / Chat / User fields from the official Bot API contract.
        return """
            {"update_id":100,"message":{"message_id":1,"date":1700000000,
            "from":{"id":12345,"is_bot":false,"first_name":"Test"},
            "chat":{"id":12345,"type":"%s","first_name":"Test"},"text":"/start %s"}}
            """.formatted(chatType, token);
    }
    private String tokenFrom(String url) { return url.substring(url.indexOf("?start=") + 7); }

    @Test void authenticatedLinkWebhookStatusReplayAndUnlink() throws Exception {
        var response = request("POST", "/v1/telegram/link", null, bearer, null);
        assertEquals(200, response.statusCode());
        String token = tokenFrom(json.readTree(response.body()).get("url").asText());
        assertEquals(43, token.length());
        assertEquals(204, request("POST", "/v1/telegram/webhook", payload(token, "private"), null, "test-secret").statusCode());
        var linked = users.findById(user.getId()).orElseThrow();
        assertTrue(linked.isTelegramEnabled());
        assertEquals(12345L, linked.getTelegramChatId());
        assertEquals(204, request("POST", "/v1/telegram/webhook", payload(token, "private"), null, "test-secret").statusCode());
        assertEquals(200, request("GET", "/v1/telegram/status", null, bearer, null).statusCode());
        assertEquals(204, request("DELETE", "/v1/telegram/link", null, bearer, null).statusCode());
        assertFalse(users.findById(user.getId()).orElseThrow().isTelegramEnabled());
        assertEquals(204, request("POST", "/v1/telegram/webhook", payload(token, "private"), null, "test-secret").statusCode());
        assertFalse(users.findById(user.getId()).orElseThrow().isTelegramEnabled());
    }
    @Test void requiresJwtAndWebhookSecret() throws Exception {
        assertEquals(401, request("POST", "/v1/telegram/link", null, null, null).statusCode());
        assertEquals(401, request("GET", "/v1/telegram/status", null, null, null).statusCode());
        assertEquals(401, request("DELETE", "/v1/telegram/link", null, null, null).statusCode());
        assertEquals(403, request("POST", "/v1/telegram/webhook", payload("bad", "private"), null, null).statusCode());
        assertEquals(403, request("POST", "/v1/telegram/webhook", payload("bad", "private"), null, "wrong").statusCode());
        assertEquals(400, request("POST", "/v1/telegram/webhook", "{}", null, "test-secret").statusCode());
        assertEquals(204, request("POST", "/v1/telegram/webhook", "{\"update_id\":9}", null, "test-secret").statusCode());
    }
    @Test void expiredSupersededAndGroupTokensDoNotLink() throws Exception {
        String old = tokenFrom(links.createLink(user.getEmail()).url());
        String current = tokenFrom(links.createLink(user.getEmail()).url());
        request("POST", "/v1/telegram/webhook", payload(old, "private"), null, "test-secret");
        request("POST", "/v1/telegram/webhook", payload(current, "group"), null, "test-secret");
        assertFalse(users.findById(user.getId()).orElseThrow().isTelegramEnabled());
        var expired = tokens.findAll().stream().filter(t -> t.getToken().equals(current)).findFirst().orElseThrow();
        expired.setExpiresAt(Instant.now().minusSeconds(1));
        tokens.save(expired);
        request("POST", "/v1/telegram/webhook", payload(current, "private"), null, "test-secret");
        assertFalse(users.findById(user.getId()).orElseThrow().isTelegramEnabled());
    }
    @Test void remindersUseStartTimezoneAndAreCancelledAndDeletedWithTask() {
        var task = tasks.save(TaskEntity.builder().name("Training").user(user).finished(false)
                .startDateTime(LocalDateTime.of(2027, 1, 10, 10, 0))
                .endDateTime(LocalDateTime.of(2027, 1, 10, 11, 0)).build());
        reminderService.schedule(task);
        var reminder = reminders.findByTaskId(task.getId()).getFirst();
        assertEquals(Instant.parse("2027-01-10T12:45:00Z"), reminder.getScheduledAt());
        assertNull(reminder.getSent());
        assertEquals(ReminderStatusTypeEnum.PENDING, reminder.getStatus());
        task.setStartDateTime(task.getStartDateTime().plusHours(1));
        tasks.save(task);
        reminderService.schedule(task);
        assertEquals(Instant.parse("2027-01-10T13:45:00Z"), reminders.findByTaskId(task.getId()).getFirst().getScheduledAt());
        task.setDurationHour(1);
        task.setDurationMinutes(0);
        tasks.save(task);
        taskService.finishTask(task.getId());
        assertEquals(ReminderStatusTypeEnum.CANCELLED, reminders.findByTaskId(task.getId()).getFirst().getStatus());
        taskService.deleteById(task.getId());
        assertTrue(reminders.findByTaskId(task.getId()).isEmpty());
        assertFalse(tasks.existsById(task.getId()));
    }
    @Test void taskCreationAndUpdateScheduleRemindersAutomatically() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        var created = taskService.createTask(new br.com.github.gpego1.routines.dto.requests.CreateTaskRequest(
                "Read", "Chapter one", CategoryEnumType.READING, start, start.plusHours(1), user.getId()));
        var first = reminders.findByTaskId(created.id()).getFirst();
        assertEquals(start.atZone(ZoneId.of("America/Sao_Paulo")).toInstant().minusSeconds(900), first.getScheduledAt());
        taskService.updateTask(new br.com.github.gpego1.routines.dto.requests.UpdateTaskRequest(
                "Read", "Chapter two", CategoryEnumType.READING, start.plusHours(1), start.plusHours(2)), created.id());
        assertEquals(first.getScheduledAt().plusSeconds(3600), reminders.findByTaskId(created.id()).getFirst().getScheduledAt());
    }
}
