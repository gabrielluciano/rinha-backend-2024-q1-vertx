package com.gabrielluciano.rinha.util;

import java.util.regex.Pattern;

public class RegexPatterns {

  public static final Pattern VALID_UINT_REGEX = Pattern.compile("^[0-9]*$");

  private RegexPatterns() {
  }
}
