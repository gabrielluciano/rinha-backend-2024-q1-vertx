package com.gabrielluciano.rinha.routes;

import com.gabrielluciano.rinha.exceptions.ClienteNaoEncontradoException;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class ExtratoRoute implements Handler<RoutingContext> {

  private final Pool pool;

  public ExtratoRoute(Pool pool) {
    this.pool = pool;
  }

  @Override
  public void handle(RoutingContext ctx) {
    Integer clienteId = Integer.parseInt(ctx.pathParam("id"));
    JsonObject response = new JsonObject();
    JsonObject saldo = new JsonObject();

    pool.withConnection(connection -> connection
        .preparedQuery("SELECT saldo, limite FROM clientes WHERE id = $1")
        .execute(Tuple.of(clienteId))
        .flatMap(rowSet -> {
          if (!rowSet.iterator().hasNext())
            throw new ClienteNaoEncontradoException();

          Row row = rowSet.iterator().next();
          saldo.put("total", row.getInteger("saldo"));
          saldo.put("limite", row.getInteger("limite"));

          return connection
            .preparedQuery(
              """
                   SELECT tipo, valor, descricao, realizada_em
                   FROM transacoes WHERE cliente_id = $1
                   ORDER BY realizada_em DESC
                   LIMIT 10;
                """
            )
            .execute(Tuple.of(clienteId));
        })

      )
      .onSuccess(rows -> {
        JsonArray transacoes = new JsonArray();
        for (Row row : rows) {
          JsonObject transacao = new JsonObject();
          transacao.put("valor", row.getInteger("valor"));
          transacao.put("tipo", row.getString("tipo"));
          transacao.put("descricao", row.getString("descricao"));
          transacao.put("realizada_em", row.getOffsetDateTime("realizada_em").toString());
          transacoes.add(transacao);
        }
        saldo.put("data_extrato", OffsetDateTime.now(ZoneOffset.UTC).toString());
        response.put("saldo", saldo);
        response.put("ultimas_transacoes", transacoes);
        ctx.response()
          .setStatusCode(200)
          .putHeader("Content-Type", "application/json")
          .end(response.encode());
      })
      .onFailure(err -> {
        if (err instanceof ClienteNaoEncontradoException) {
          ctx.response().setStatusCode(404);
        } else {
          err.printStackTrace();
          ctx.response().setStatusCode(500);
        }
        ctx.end();
      });
  }
}
