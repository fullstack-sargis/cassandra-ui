package com.example.cassandraui.service;

import com.example.cassandraui.exception.BadRequestException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class QueryValidator {
    private static final Pattern SELECT_PATTERN = Pattern.compile("^\\s*select\\b", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern FORBIDDEN_PATTERN = Pattern.compile("\\b(insert|update|delete|drop|truncate|alter|create)\\b", Pattern.CASE_INSENSITIVE);
    private static final Set<String> FORBIDDEN_PREFIXES = Set.of("insert", "update", "delete", "drop", "truncate", "alter", "create");

    public String validateSelectOnly(String query) {
        String trimmed = query == null ? "" : query.trim();
        if (trimmed.isBlank()) {
            throw new BadRequestException("Query cannot be blank.");
        }

        String singleStatement = stripSingleTrailingSemicolon(trimmed);
        if (singleStatement.contains(";")) {
            throw new BadRequestException("Only a single SELECT statement is allowed.");
        }

        String normalized = stripStringLiterals(singleStatement).toLowerCase(Locale.ROOT);
        if (!SELECT_PATTERN.matcher(normalized).find()) {
            throw new BadRequestException("Only SELECT queries are allowed.");
        }
        if (FORBIDDEN_PATTERN.matcher(normalized).find() || FORBIDDEN_PREFIXES.stream().anyMatch(normalized::startsWith)) {
            throw new BadRequestException("Mutation and schema-changing queries are not allowed.");
        }

        return singleStatement;
    }

    private String stripSingleTrailingSemicolon(String query) {
        String trimmed = query.trim();
        return trimmed.endsWith(";") ? trimmed.substring(0, trimmed.length() - 1).trim() : trimmed;
    }

    private String stripStringLiterals(String query) {
        StringBuilder sanitized = new StringBuilder(query.length());
        boolean inSingleQuote = false;
        for (int i = 0; i < query.length(); i++) {
            char ch = query.charAt(i);
            if (ch == '\'' && (i == 0 || query.charAt(i - 1) != '\\')) {
                inSingleQuote = !inSingleQuote;
                sanitized.append(' ');
            } else {
                sanitized.append(inSingleQuote ? ' ' : ch);
            }
        }
        return sanitized.toString();
    }
}
