package com.servicehub.exception;

/** Regra de negócio violada pela requisição (ex.: tentar se auto-cadastrar como ADMIN). Gera 400. */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
