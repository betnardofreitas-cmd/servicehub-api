package com.servicehub.util;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Sanitização de entradas (Aula 8). Complementa o Bean Validation:
 * a validação REJEITA dados inválidos; a sanitização NORMALIZA dados aceitos.
 * Aplicada na camada de service, antes de persistir.
 */
@Component
public class InputSanitizer {

    /** Texto livre: remove espaços extras e faz escape de HTML (&lt;script&gt; etc.) para evitar XSS armazenado. */
    public String text(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.strip().replaceAll("\\s+", " ");
        return StringEscapeUtils.escapeHtml4(normalized);
    }

    /** E-mail: trim + minúsculas, para que "Ana@Email.com " e "ana@email.com" sejam o mesmo e-mail. */
    public String email(String value) {
        return value == null ? null : value.strip().toLowerCase(Locale.ROOT);
    }

    /** Telefone: mantém apenas dígitos e os caracteres ( ) - + e espaço. */
    public String phone(String value) {
        return value == null ? null : value.strip().replaceAll("[^0-9()+\\- ]", "");
    }
}
