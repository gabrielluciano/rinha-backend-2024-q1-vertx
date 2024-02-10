package com.gabrielluciano.rinha.routes;

import com.gabrielluciano.rinha.dto.TransacaoRequest;
import com.gabrielluciano.rinha.entities.Cliente;
import com.gabrielluciano.rinha.entities.Transacao;
import com.gabrielluciano.rinha.exceptions.ClienteNaoEncontradoException;
import com.gabrielluciano.rinha.exceptions.SaldoInsuficienteException;
import com.gabrielluciano.rinha.repository.Repository;
import com.gabrielluciano.rinha.util.Validations;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TransacaoRoute implements Handler<RoutingContext> {

  private final Pool pool;

  public TransacaoRoute(Pool pool) {
    this.pool = pool;
  }

  @Override
  public void handle(RoutingContext ctx) {
    Integer id = Integer.parseInt(ctx.pathParam("id"));

    if (isInvalidRequest(ctx.body())) {
      ctx.response().setStatusCode(422).end();
      return;
    }

    TransacaoRequest transacaoRequest = TransacaoRequest.fromRequestBody(ctx.body());
    JsonObject response = new JsonObject();

    pool.withTransaction(connection -> {
        Repository repository = new Repository(connection);
        return repository
          .findClienteById(id)
          .flatMap(cliente -> updateSaldoCliente(cliente, transacaoRequest))
          .flatMap(cliente -> setResponse(response, cliente))
          .flatMap(repository::saveCliente)
          .flatMap(res -> createTransacao(transacaoRequest, id))
          .flatMap(repository::saveTransacao);
      })
      .onSuccess(res -> ctx.response()
        .setStatusCode(200)
        .putHeader("Content-Type", "application/json")
        .end(response.encode())
      )
      .onFailure(err -> {
        if (err instanceof ClienteNaoEncontradoException) {
          ctx.response().setStatusCode(404);
        } else if (err instanceof SaldoInsuficienteException) {
          ctx.response().setStatusCode(422);
        } else {
          ctx.response().setStatusCode(500);
        }
        ctx.response().end();
      });
  }

  private boolean isValidRequest(RequestBody body) {
    JsonObject json = body.asJsonObject();
    String tipo = json.getString("tipo");
    String valor = json.getString("valor");
    String descricao = json.getString("descricao");

    return Validations.isValidTipo(tipo) && Validations.isValidValor(valor)
      && Validations.isValidDescricao(descricao);
  }

  private boolean isInvalidRequest(RequestBody body) {
    return !isValidRequest(body);
  }

  private Future<Cliente> updateSaldoCliente(Cliente cliente, TransacaoRequest transacaoRequest) {
    if (cliente.cannotUpdateSaldo(transacaoRequest))
      return Future.failedFuture(new SaldoInsuficienteException());
    cliente.updateSaldo(transacaoRequest);
    return Future.succeededFuture(cliente);
  }

  private Future<Cliente> setResponse(JsonObject response, Cliente cliente) {
    response.put("saldo", cliente.getSaldo());
    response.put("limite", cliente.getLimite());
    return Future.succeededFuture(cliente);
  }

  private Future<Transacao> createTransacao(TransacaoRequest transacaoRequest, Integer clienteId) {
    Transacao transacao = Transacao.builder()
      .clienteId(clienteId)
      .tipo(transacaoRequest.getTipo())
      .valor(transacaoRequest.getValor())
      .descricao(transacaoRequest.getDescricao())
      .realizadaEm(OffsetDateTime.now(ZoneOffset.UTC))
      .build();
    return Future.succeededFuture(transacao);
  }
}
