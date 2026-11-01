package br.com.github.gpego1.routines.dto.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramMessageResponse(
        boolean ok,
        String description
) {

}
