package com.example.cassandraui.service;

import com.example.cassandraui.exception.BadRequestException;
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
  private static final String MULTIPLE_STATEMENTS_ERROR = "Only a single statement is allowed.";

  public String validateAny(String query) {
    var trimmed = query == null ? BLANK_QUERY : query.trim();
    if (trimmed.isBlank()) {
      throw new BadRequestException(BLANK_QUERY_ERROR);
    }

    var singleStatement = stripSingleTrailingSemicolon(trimmed);
    if (stripStringLiterals(singleStatement).indexOf(SEMICOLON) >= FIRST_CHARACTER_INDEX) {
      throw new BadRequestException(MULTIPLE_STATEMENTS_ERROR);
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
      if (inSingleQuote
          && ch == SINGLE_QUOTE
          && i + LAST_CHARACTER_OFFSET < query.length()
          && query.charAt(i + LAST_CHARACTER_OFFSET) == SINGLE_QUOTE) {
        sanitized.append(EMPTY_LITERAL_PLACEHOLDER);
        sanitized.append(EMPTY_LITERAL_PLACEHOLDER);
        i++;
        continue;
      }
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
