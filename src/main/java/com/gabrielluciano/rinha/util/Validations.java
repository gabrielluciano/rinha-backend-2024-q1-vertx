package com.gabrielluciano.rinha.util;

public class Validations {

  private Validations() {
  }

  public static boolean isValidTipo(String tipo) {
    return tipo != null && (tipo.equals("c") || tipo.equals("d"));
  }

  public static boolean isValidDescricao(String descricao) {
    return descricao != null && descricao.length() > 1 && descricao.length() <= 10;
  }

  public static boolean isValidValor(String valor) {
    return RegexPatterns.VALID_UINT_REGEX.matcher(valor).matches();
  }
}
