package com.gabrielluciano.rinha.repository;

import com.gabrielluciano.rinha.entities.Transacao;
import com.gabrielluciano.rinha.exceptions.ClienteNaoEncontradoException;
import com.gabrielluciano.rinha.sql.Query;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class Repository {

  private final Pool pool;

  public Repository(Pool pool) {
    this.pool = pool;
  }

  public Future<JsonObject> doTransacao(Transacao transacao) {
    return pool.withConnection(connection -> connection
      .preparedQuery(Query.UPDATE_SALDO_CLIENTE)
      .execute(Tuple.of(
        transacao.getClienteId(),
        Integer.parseInt(transacao.getValor()),
        transacao.getTipo(),
        transacao.getDescricao()
      )).flatMap(result -> {
        Row row = result.iterator().next();
        return Future.succeededFuture(
          new JsonObject()
            .put("limite", row.getInteger("limite"))
            .put("saldo", row.getInteger("new_saldo"))
        );
      }));
  }

  public Future<JsonObject> getTransacoesByClienteId(Integer id) {
    JsonObject response = new JsonObject();
    return pool.withConnection(connection -> connection
      .preparedQuery(Query.SELECT_CLIENTE_BY_ID)
      .execute(Tuple.of(id))
      .flatMap(rs -> setSaldoFromRowSet(response, rs))
      .flatMap(res -> connection
        .preparedQuery(Query.SELECT_TRANSACOES_BY_CLIENTE_ID)
        .execute(Tuple.of(id))
        .flatMap(rs -> setTransacoesFromRowSet(response, rs))
      )
      .map(res -> response));
  }

  private Future<Void> setTransacoesFromRowSet(JsonObject response, RowSet<Row> rs) {
    JsonArray transacoes = new JsonArray();
    for (Row row : rs) {
      JsonObject transacao = new JsonObject();
      transacao.put("valor", row.getInteger("valor"));
      transacao.put("tipo", row.getString("tipo"));
      transacao.put("descricao", row.getString("descricao"));
      transacao.put("realizada_em", row.getOffsetDateTime("realizada_em").toString());
      transacoes.add(transacao);
    }
    response.put("ultimas_transacoes", transacoes);
    return Future.succeededFuture(null);
  }

  private Future<Void> setSaldoFromRowSet(JsonObject response, RowSet<Row> rs) {
    if (!rs.iterator().hasNext())
      return Future.failedFuture(new ClienteNaoEncontradoException());
    Row row = rs.iterator().next();
    JsonObject saldo = new JsonObject();
    saldo.put("total", row.getInteger("saldo"));
    saldo.put("data_extrato", OffsetDateTime.now(ZoneOffset.UTC).toString());
    saldo.put("limite", row.getInteger("limite"));
    response.put("saldo", saldo);
    return Future.succeededFuture(null);
  }
}
