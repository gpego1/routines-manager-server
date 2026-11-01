# Telegram

## Configuracao

O Spring carrega o `.env` local (ignorado pelo Git). Em producao, injete as variaveis de ambiente.

```dotenv
TELEGRAM_BOT_TOKEN=token_do_BotFather
TELEGRAM_BOT_USERNAME=nome_do_bot_sem_arroba
TELEGRAM_WEBHOOK_SECRET=segredo_aleatorio_com_letras_numeros_hifen_ou_underscore
TELEGRAM_WEBHOOK_URL=https://seu-dominio/v1/telegram/webhook
APP_TIMEZONE=America/Sao_Paulo
```

Com `TELEGRAM_WEBHOOK_URL` preenchida, a aplicacao registra `setWebhook` ao iniciar,
enviando `secret_token` e `allowed_updates: [message]`. Falha no registro impede a
inicializacao com mensagem sem credenciais. URL vazia desativa apenas o registro automatico.
O endpoint deve estar acessivel por HTTPS publico; configurar DNS, certificado e proxy
reverso pertence ao ambiente de deploy. O token e o dominio reais nao sao configurados pelos testes.

## API e fluxo

As rotas de conta exigem `Authorization: Bearer <JWT>` e usam o usuario do JWT, sem receber userId.

| Metodo | Rota | Resultado |
| --- | --- | --- |
| POST | /v1/telegram/link | 200: `{ "url": "https://t.me/bot?start=...", "expiresAt": "..." }`; 503 se faltam configuracoes |
| GET | /v1/telegram/status | 200: `{ "enabled": true, "chatId": 12345 }` |
| DELETE | /v1/telegram/link | 204: desativa e remove chatId, invalida tokens pendentes |
| POST | /v1/telegram/webhook | 204: evento processado ou ignorado; 403: segredo ausente/invalido; 400: JSON invalido ou update_id ausente |

1. Autenticado, solicitar POST /v1/telegram/link.
2. Abrir `url` e tocar em Iniciar no Telegram. O link expira em 10 minutos.
3. Telegram envia `/start TOKEN`. Apenas chats privados cujo remetente corresponde ao chat sao aceitos.
4. A conta e vinculada e o token e consumido na mesma transacao. Reentregas sao ignoradas.
5. Conferir GET /v1/telegram/status. A vinculacao nao envia mensagem adicional ao chat.

Gerar novo link invalida os anteriores. Desvincular tambem invalida links pendentes.
O webhook autentica pelo header `X-Telegram-Bot-Api-Secret-Token`, sem exigir JWT.
Nunca envie token do bot ou segredo do webhook ao frontend.

## Lembretes

Criar tarefa cria um lembrete BEFORE_START, 15 minutos antes do inicio.
O fuso vem de `UserEntity.timezone`, com fallback em APP_TIMEZONE; datas sao persistidas em Instant.
Se a tarefa comeca em menos de 15 minutos, o lembrete fica disponivel imediatamente.
Editar reagenda lembretes ainda nao enviados; concluir cancela pendentes; excluir remove lembretes antes da tarefa.
Lembretes enviados sao preservados ao editar. O service tambem suporta ON_START.
O horario e o status fornecidos no DTO legado nao controlam o agendamento: o servidor os calcula.

O scheduler verifica ate 50 lembretes por rodada, a cada 30 segundos, em transacoes separadas.
Bloqueios pessimistas na tarefa e no lembrete evitam envios concorrentes e serializam edicao/conclusao/exclusao.
Sucesso grava SENT e sent; erro tenta novamente apos 60 e 120 segundos, com limite de tres tentativas.
Conta ainda nao vinculada aguarda sem gastar tentativas. BEFORE_START expira no inicio;
ON_START tem tolerancia de 60 segundos. Lembretes vencidos sao cancelados.

Entrega exatamente uma vez nao e garantida: timeout apos aceite do Telegram ou queda entre envio
e commit pode causar duplicata. `sendMessage` nao recebe chave de idempotencia neste cliente.
Nao existem chamadas externas dentro da transacao de criar/editar uma tarefa.

## Verificacao local

`mvn test` usa H2 e credenciais sinteticas, sem MySQL ou Telegram real.
Para exercitar HTTP de saida com Postman:

```powershell
postman simulation run postman/simulations/telegram/telegram.sim.yaml --port 4010 --output ndjson --no-history
mvn '-Dtelegram.simulation=true' test
```

A simulacao valida serializacao chat_id/text, sucesso, falha HTTP 500, ok=false,
retentativas e entrega concorrente. Os logs devem mostrar requisicoes ao membro telegram.
Nao execute com token real: o mock aceita somente `test-token`.
Para desenvolvimento com a aplicacao completa: TELEGRAM_API_BASE_URL=http://localhost:4010/telegram.
TELEGRAM_SCHEDULER_ENABLED=false desativa o scheduler quando necessario.

Contrato externo: https://core.telegram.org/bots/api e https://core.telegram.org/bots/features#deep-linking.
