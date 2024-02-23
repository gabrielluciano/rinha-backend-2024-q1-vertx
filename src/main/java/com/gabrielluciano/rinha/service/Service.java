package com.gabrielluciano.rinha.service;

import com.gabrielluciano.rinha.dto.TransacaoRequest;
import com.gabrielluciano.rinha.exceptions.ClienteNaoEncontradoException;
import com.gabrielluciano.rinha.sql.Query;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class Service {

  private final SqlClient sqlClient;

  public Service(SqlClient sqlClient) {
    this.sqlClient = sqlClient;
  }

  public Future<JsonObject> saveTransacao(TransacaoRequest transacaoRequest) {
    return sqlClient
      .preparedQuery(Query.UPDATE_SALDO_CLIENTE)
      .execute(Tuple.of(
        transacaoRequest.getClienteId(),
        Integer.parseInt(transacaoRequest.getValor()),
        transacaoRequest.getTipo(),
        transacaoRequest.getDescricao()
      )).map(result -> {
        Row row = result.iterator().next();
        return new JsonObject()
          .put("limite", row.getInteger("limite"))
          .put("saldo", row.getInteger("new_saldo"));
      });
  }

  public Future<JsonObject> getExtratoByClienteId(Integer id) {
    JsonObject response = new JsonObject();
    return sqlClient
      .preparedQuery(Query.SELECT_CLIENTE_BY_ID)
      .execute(Tuple.of(id))
      .flatMap(rs -> setSaldoFromRowSet(response, rs))
      .flatMap(res -> sqlClient
        .preparedQuery(Query.SELECT_TRANSACOES_BY_CLIENTE_ID)
        .execute(Tuple.of(id))
        .flatMap(rs -> setTransacoesFromRowSet(response, rs))
      )
      .map(res -> response);
  }

  private Future<Void> setSaldoFromRowSet(JsonObject response, RowSet<Row> rs) {
    if (!rs.iterator().hasNext())
      return Future.failedFuture(new ClienteNaoEncontradoException());
    Row row = rs.iterator().next();
    response.put("saldo", new JsonObject()
      .put("total", row.getInteger("saldo"))
      .put("data_extrato", OffsetDateTime.now(ZoneOffset.UTC).toString())
      .put("limite", row.getInteger("limite")));
    return Future.succeededFuture(null);
  }

  private Future<Void> setTransacoesFromRowSet(JsonObject response, RowSet<Row> rs) {
    JsonArray transacoes = new JsonArray();
    for (Row row : rs) {
      transacoes.add(new JsonObject()
        .put("valor", row.getInteger("valor"))
        .put("tipo", row.getString("tipo"))
        .put("descricao", row.getString("descricao"))
        .put("realizada_em", row.getOffsetDateTime("realizada_em").toString()));
    }
    response.put("ultimas_transacoes", transacoes);
    return Future.succeededFuture(null);
  }
}
