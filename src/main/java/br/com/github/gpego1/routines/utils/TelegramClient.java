package br.com.github.gpego1.routines.utils;

public interface TelegramClient {
    void sendMessage(Long chatId, String text);
}
