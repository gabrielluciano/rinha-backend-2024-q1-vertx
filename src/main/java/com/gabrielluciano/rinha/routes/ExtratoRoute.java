package com.gabrielluciano.rinha.routes;

import com.gabrielluciano.rinha.entities.Cliente;
import com.gabrielluciano.rinha.entities.Transacao;
import com.gabrielluciano.rinha.exceptions.ClienteNaoEncontradoException;
import com.gabrielluciano.rinha.repository.Repository;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class ExtratoRoute implements Handler<RoutingContext> {

  private final Pool pool;

  public ExtratoRoute(Pool pool) {
    this.pool = pool;
  }

  @Override
  public void handle(RoutingContext ctx) {
    Integer id = Integer.parseInt(ctx.pathParam("id"));
    JsonObject saldo = new JsonObject();

    pool.withConnection(connection -> {
        Repository repository = new Repository(connection);
        return repository
          .findClienteById(id)
          .flatMap(cliente -> setSaldo(saldo, cliente))
          .flatMap(cliente -> repository.getTransacoesByClienteId(cliente.getId()))
          .flatMap(transacoes -> createResponse(transacoes, saldo));
      })
      .onSuccess(response -> ctx.response()
        .setStatusCode(200)
        .putHeader("Content-Type", "application/json")
        .end(response.encode())
      )
      .onFailure(err -> {
        if (err instanceof ClienteNaoEncontradoException) {
          ctx.response().setStatusCode(404);
        } else {
          ctx.response().setStatusCode(500);
        }
        ctx.end();
      });
  }

  private Future<Cliente> setSaldo(JsonObject saldo, Cliente cliente) {
    saldo.put("total", cliente.getSaldo());
    saldo.put("limite", cliente.getLimite());
    return Future.succeededFuture(cliente);
  }

  private Future<JsonObject> createResponse(List<Transacao> transacoes, JsonObject saldo) {
    JsonObject response = new JsonObject();
    JsonArray ultimasTransacoes = new JsonArray();
    for (Transacao transacao : transacoes) {
      JsonObject transacaoObject = new JsonObject();
      transacaoObject.put("valor", transacao.getValor());
      transacaoObject.put("tipo", String.valueOf(transacao.getTipo()));
      transacaoObject.put("descricao", transacao.getDescricao());
      transacaoObject.put("realizada_em", transacao.getRealizadaEm().toString());
      ultimasTransacoes.add(transacaoObject);
    }
    saldo.put("data_extrato", OffsetDateTime.now(ZoneOffset.UTC).toString());
    response.put("saldo", saldo);
    response.put("ultimas_transacoes", ultimasTransacoes);
    return Future.succeededFuture(response);
  }
}
