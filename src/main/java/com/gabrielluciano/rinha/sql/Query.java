package com.gabrielluciano.rinha.sql;

public class Query {

  public static final String SELECT_CLIENTE_BY_ID = "SELECT id, saldo, limite FROM clientes WHERE id = $1";

  public static final String UPDATE_CLIENTE = "UPDATE clientes SET saldo = $1 WHERE id = $2";

  public static final String INSERT_TRANSACAO = """
     INSERT INTO transacoes (cliente_id, tipo, valor, descricao, realizada_em)
     VALUES ($1, $2, $3, $4, $5)
    """;

  public static final String SELECT_TRANSACOES_BY_CLIENTE_ID = """
       SELECT tipo, valor, descricao, realizada_em
       FROM transacoes WHERE cliente_id = $1
       ORDER BY realizada_em DESC
       LIMIT 10;
    """;

  private Query() {
  }
}
