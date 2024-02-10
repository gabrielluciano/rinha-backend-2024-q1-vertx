package com.gabrielluciano.rinha;

import java.util.regex.Pattern;

public class Test {

  private static final Pattern regex = Pattern.compile("^[0-9]*$");

  public static void main(String[] args) {
    // Exception
    long start = System.currentTimeMillis();
    for (int i = 0; i < 100000; i++) {
      validIntException("1");
    }
    long stop = System.currentTimeMillis();
    System.out.println("Duracao exception " + (stop - start));

    // Regex
    start = System.currentTimeMillis();
    for (int i = 0; i < 100000; i++) {
      validIntRegex("1");
    }
    stop = System.currentTimeMillis();
    System.out.println("Duracao regex " + (stop - start));
  }

  private static boolean validIntException(String s) {
    try {
      Integer.parseInt(s);
      return true;
    } catch (NumberFormatException ex) {
      return false;
    }
  }

  private static boolean validIntRegex(String s) {
    return regex.matcher(s).matches();
  }
}
