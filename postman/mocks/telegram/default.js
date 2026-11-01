// Contract: https://core.telegram.org/bots/api#sendmessage
// Only synthetic credentials and chat ids; never forwards to Telegram.
const http = require('http');
http.createServer((req, res) => {
  const respond = (status, value) => {
    res.writeHead(status, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify(value));
  };
  if (req.method !== 'POST') return respond(501, { ok: false, description: 'Not modeled' });
  if (req.url === '/bottest-token/setWebhook') return respond(200, { ok: true, result: true });
  if (req.url !== '/bottest-token/sendMessage') return respond(501, { ok: false, description: 'Not modeled' });
  const body = req.body;
  if (!body || !Number.isInteger(body.chat_id) || typeof body.text !== 'string' || !body.text.trim())
    return respond(400, { ok: false, error_code: 400, description: 'Bad Request' });
  if (body.chat_id === 500) return respond(500, { ok: false, error_code: 500, description: 'Simulated upstream failure' });
  if (body.chat_id === 400) return respond(200, { ok: false, error_code: 400, description: 'Simulated unsuccessful response' });
  respond(200, { ok: true, result: { message_id: 1, date: 1700000000,
    chat: { id: body.chat_id, type: 'private' }, text: body.text } });
}).listen(process.env.PORT || 4500);
