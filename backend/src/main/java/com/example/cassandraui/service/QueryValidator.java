package com.example.cassandraui.service;

import com.example.cassandraui.exception.BadRequestException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class QueryValidator {
  private static final int FIRST_CHARACTER_INDEX = 0;
  private static final int LAST_CHARACTER_OFFSET = 1;
  private static final char EMPTY_LITERAL_PLACEHOLDER = ' ';
  private static final char ESCAPE_CHARACTER = '\\';
  private static final char SEMICOLON = ';';
  private static final char SINGLE_QUOTE = '\'';
  private static final String BLANK_QUERY = "";
  private static final String BLANK_QUERY_ERROR = "Query cannot be blank.";
  private static final String FORBIDDEN_QUERY_ERROR =
      "Mutation and schema-changing queries are not allowed.";
  private static final String MULTIPLE_STATEMENTS_ERROR =
      "Only a single SELECT statement is allowed.";
  private static final String SELECT_QUERY_ERROR = "Only SELECT queries are allowed.";
  private static final String SELECT_PATTERN_SOURCE = "^\\s*select\\b";
  private static final String FORBIDDEN_PATTERN_SOURCE =
      "\\b(insert|update|delete|drop|truncate|alter|create)\\b";
  private static final String INSERT = "insert";
  private static final String UPDATE = "update";
  private static final String DELETE = "delete";
  private static final String DROP = "drop";
  private static final String TRUNCATE = "truncate";
  private static final String ALTER = "alter";
  private static final String CREATE = "create";
  private static final Pattern SELECT_PATTERN =
      Pattern.compile(SELECT_PATTERN_SOURCE, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern FORBIDDEN_PATTERN =
      Pattern.compile(FORBIDDEN_PATTERN_SOURCE, Pattern.CASE_INSENSITIVE);
  private static final Set<String> FORBIDDEN_PREFIXES =
      Set.of(INSERT, UPDATE, DELETE, DROP, TRUNCATE, ALTER, CREATE);

  public String validateSelectOnly(String query) {
    var trimmed = query == null ? BLANK_QUERY : query.trim();
    if (trimmed.isBlank()) {
      throw new BadRequestException(BLANK_QUERY_ERROR);
    }

    var singleStatement = stripSingleTrailingSemicolon(trimmed);
    if (singleStatement.indexOf(SEMICOLON) >= FIRST_CHARACTER_INDEX) {
      throw new BadRequestException(MULTIPLE_STATEMENTS_ERROR);
    }

    var normalized = stripStringLiterals(singleStatement).toLowerCase(Locale.ROOT);
    if (!SELECT_PATTERN.matcher(normalized).find()) {
      throw new BadRequestException(SELECT_QUERY_ERROR);
    }
    if (FORBIDDEN_PATTERN.matcher(normalized).find()
        || FORBIDDEN_PREFIXES.stream().anyMatch(normalized::startsWith)) {
      throw new BadRequestException(FORBIDDEN_QUERY_ERROR);
    }

    return singleStatement;
  }

  private String stripSingleTrailingSemicolon(String query) {
    var trimmed = query.trim();
    return trimmed.charAt(trimmed.length() - LAST_CHARACTER_OFFSET) == SEMICOLON
        ? trimmed.substring(FIRST_CHARACTER_INDEX, trimmed.length() - LAST_CHARACTER_OFFSET).trim()
        : trimmed;
  }

  private String stripStringLiterals(String query) {
    var sanitized = new StringBuilder(query.length());
    var inSingleQuote = false;
    for (var i = 0; i < query.length(); i++) {
      var ch = query.charAt(i);
      if (ch == SINGLE_QUOTE
          && (i == FIRST_CHARACTER_INDEX
              || query.charAt(i - LAST_CHARACTER_OFFSET) != ESCAPE_CHARACTER)) {
        inSingleQuote = !inSingleQuote;
        sanitized.append(EMPTY_LITERAL_PLACEHOLDER);
      } else {
        sanitized.append(inSingleQuote ? EMPTY_LITERAL_PLACEHOLDER : ch);
      }
    }
    return sanitized.toString();
  }
}
