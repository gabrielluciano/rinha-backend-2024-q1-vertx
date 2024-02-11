package com.gabrielluciano.rinha.repository;

import com.gabrielluciano.rinha.entities.Cliente;
import com.gabrielluciano.rinha.entities.Transacao;
import com.gabrielluciano.rinha.exceptions.ClienteNaoEncontradoException;
import com.gabrielluciano.rinha.sql.Query;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

public class Repository {

  private final SqlConnection connection;

  public Repository(SqlConnection connection) {
    this.connection = connection;
  }

  public Future<Cliente> findClienteById(Integer id) {
    return connection
      .preparedQuery(Query.SELECT_CLIENTE_BY_ID)
      .execute(Tuple.of(id))
      .flatMap(this::clienteFromRowSet);
  }

  public Future<Cliente> findClienteByIdForUpdate(Integer id) {
    return connection
      .preparedQuery(Query.SELECT_CLIENTE_BY_ID_FOR_UPDATE)
      .execute(Tuple.of(id))
      .flatMap(this::clienteFromRowSet);
  }

  public Future<Void> saveCliente(Cliente cliente) {
    return connection
      .preparedQuery(Query.UPDATE_CLIENTE)
      .execute(Tuple.of(cliente.getSaldo(), cliente.getId()))
      .mapEmpty();
  }

  public Future<Void> saveTransacao(Transacao transacao) {
    return connection
      .preparedQuery(Query.INSERT_TRANSACAO)
      .execute(Tuple.of(
        transacao.getClienteId(),
        String.valueOf(transacao.getTipo()),
        transacao.getValor(),
        transacao.getDescricao(),
        transacao.getRealizadaEm()
      ))
      .mapEmpty();
  }

  public Future<JsonArray> getTransacoesByClienteId(Integer id) {
    return connection
      .preparedQuery(Query.SELECT_TRANSACOES_BY_CLIENTE_ID)
      .execute(Tuple.of(id))
      .flatMap(this::transacoesFromRowSet);
  }

  private Future<Cliente> clienteFromRowSet(RowSet<Row> rowSet) {
    if (!rowSet.iterator().hasNext())
      return Future.failedFuture(new ClienteNaoEncontradoException());
    Cliente cliente = Cliente.fromRow(rowSet.iterator().next());
    return Future.succeededFuture(cliente);
  }

  private Future<JsonArray> transacoesFromRowSet(RowSet<Row> rowSet) {
    JsonArray transacoes = new JsonArray();
    for (Row row : rowSet) {
      JsonObject transacao = new JsonObject();
      transacao.put("valor", row.getInteger("valor"));
      transacao.put("tipo", row.getString("tipo"));
      transacao.put("descricao", row.getString("descricao"));
      transacao.put("realizada_em", row.getOffsetDateTime("realizada_em").toString());
      transacoes.add(transacao);
    }
    return Future.succeededFuture(transacoes);
  }
}
