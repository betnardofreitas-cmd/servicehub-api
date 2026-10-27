package com.servicehub.graphql;

import com.servicehub.exception.EmailAlreadyExistsException;
import com.servicehub.exception.ResourceNotFoundException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolationException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

/** Traduz as exceções de negócio para erros GraphQL (equivalente ao GlobalExceptionHandler do REST). */
@Component
public class GraphQLExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        ErrorType type;
        if (ex instanceof ResourceNotFoundException) {
            type = ErrorType.NOT_FOUND;
        } else if (ex instanceof EmailAlreadyExistsException || ex instanceof ConstraintViolationException) {
            type = ErrorType.BAD_REQUEST;
        } else {
            return null; // deixa os outros resolvers (ex.: segurança -> FORBIDDEN/UNAUTHORIZED) tratarem
        }
        return GraphqlErrorBuilder.newError(env)
                .errorType(type)
                .message(ex.getMessage())
                .build();
    }
}
