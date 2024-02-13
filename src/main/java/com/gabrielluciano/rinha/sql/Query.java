package com.gabrielluciano.rinha.sql;

public class Query {

  public static final String SELECT_CLIENTE_BY_ID = "SELECT id, saldo, limite FROM clientes WHERE id = $1";

  public static final String SELECT_TRANSACOES_BY_CLIENTE_ID = """
       SELECT tipo, valor, descricao, realizada_em
       FROM transacoes WHERE cliente_id = $1
       ORDER BY realizada_em DESC
       LIMIT 10;
    """;

  public static final String UPDATE_SALDO_CLIENTE = "SELECT new_saldo, limite FROM update_saldo_cliente($1, $2, $3, $4)";

  private Query() {
  }
}
