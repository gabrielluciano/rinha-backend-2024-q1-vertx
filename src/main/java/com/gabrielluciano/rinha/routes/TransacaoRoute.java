package com.gabrielluciano.rinha.routes;

import com.gabrielluciano.rinha.dto.TransacaoRequest;
import com.gabrielluciano.rinha.entities.Cliente;
import com.gabrielluciano.rinha.exceptions.ClienteNaoEncontradoException;
import com.gabrielluciano.rinha.exceptions.SaldoInsuficienteException;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TransacaoRoute implements Handler<RoutingContext> {

  private final Pool pool;

  public TransacaoRoute(Pool pool) {
    this.pool = pool;
  }

  @Override
  public void handle(RoutingContext ctx) {
    Integer clienteId = Integer.parseInt(ctx.pathParam("id"));

    TransacaoRequest transacaoRequest;
    try {
      transacaoRequest = parseBody(ctx.body());
    } catch (Exception ex) {
      ctx.response().setStatusCode(422);
      ctx.response().end();
      return;
    }

    pool.withTransaction(connection ->
        connection
          // Obtém cliente
          .preparedQuery("SELECT id, saldo, limite FROM clientes WHERE id = $1")
          .execute(Tuple.of(clienteId))

          // Atualiza o saldo
          .flatMap(rowSet -> {
            Cliente cliente = getCliente(rowSet);
            if (cliente == null)
              return Future.failedFuture(new ClienteNaoEncontradoException());

            if (transacaoRequest.tipo == 'd' && saldoInsuficiente(transacaoRequest.valor, cliente.saldo, cliente.limite))
              return Future.failedFuture(new SaldoInsuficienteException());

            cliente.saldo += transacaoRequest.valor * (transacaoRequest.tipo == 'c' ? 1 : -1);

            return connection
              .preparedQuery("UPDATE clientes SET saldo = $1 WHERE id = $2")
              .execute(Tuple.of(cliente.saldo, cliente.id))
              .map(result -> {
                JsonObject json = new JsonObject();
                json.put("valor", transacaoRequest.valor);
                json.put("tipo", transacaoRequest.tipo);
                json.put("descricao", transacaoRequest.descricao);
                json.put("id", cliente.id);
                json.put("limite", cliente.limite);
                json.put("saldo", cliente.saldo);
                return json;
              });
          })

          // Salva transacao
          .flatMap(json -> connection
            .preparedQuery(
              """
                 INSERT INTO transacoes (cliente_id, tipo, valor, descricao, realizada_em)
                 VALUES ($1, $2, $3, $4, $5)
                """)
            .execute(Tuple.of(
              json.getInteger("id"),
              json.getString("tipo"),
              json.getInteger("valor"),
              json.getString("descricao"),
              OffsetDateTime.now(ZoneOffset.UTC)
            )).map(result -> {
              JsonObject response = new JsonObject();
              response.put("saldo", json.getInteger("saldo"));
              response.put("limite", json.getInteger("limite"));
              return response;
            })
          )
      )

      .onSuccess(response -> ctx.response()
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

  private boolean saldoInsuficiente(int valor, int saldo, int limite) {
    return (saldo - valor) * (-1) > limite;
  }

  private Cliente getCliente(RowSet<Row> rowSet) {
    if (rowSet.iterator().hasNext()) {
      Row row = rowSet.iterator().next();
      int id = row.get(Integer.class, "id");
      int saldo = row.get(Integer.class, "saldo");
      int limite = row.get(Integer.class, "limite");

      return new Cliente(id, saldo, limite);
    }
    return null;
  }

  private TransacaoRequest parseBody(RequestBody body) throws Exception {
    JsonObject json = body.asJsonObject();
    String tipoString = json.getString("tipo");
    String descricaoString = json.getString("descricao");
    String valorString = json.getString("valor");

    if (!(validaDescricao(descricaoString) && validaTipo(tipoString) && validaValor(valorString))) {
      throw new Exception();
    }

    return new TransacaoRequest(tipoString.charAt(0), Integer.parseInt(valorString), descricaoString);
  }

  private boolean validaTipo(String tipo) {
    return tipo != null && (tipo.equals("c") || tipo.equals("d"));
  }

  private boolean validaDescricao(String descricao) {
    if (descricao == null)
      return false;
    return descricao.length() > 1 && descricao.length() <= 10;
  }

  public boolean validaValor(String valor) {
    int number;
    try {
      number = Integer.parseInt(valor);
    } catch (NumberFormatException ex) {
      return false;
    }
    return number > 0;
  }
}
