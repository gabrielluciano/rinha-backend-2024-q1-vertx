package com.gabrielluciano.rinha.routes;

import com.gabrielluciano.rinha.dto.TransacaoRequest;
import com.gabrielluciano.rinha.service.Service;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgException;

public class TransacaoRoute implements Handler<RoutingContext> {

  private static final String CLIENT_NOT_FOUND_ERROR_CODE = "P0002";
  private static final String NOT_ENOUGH_SALDO_ERROR_CODE = "P0000";
  private static final Logger logger = LoggerFactory.getLogger(TransacaoRoute.class);

  private final Service service;

  public TransacaoRoute(Service service) {
    this.service = service;
  }

  @Override
  public void handle(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    TransacaoRequest transacaoRequest = TransacaoRequest.fromRequestBodyAndClienteId(ctx.body(), id);

    if (transacaoRequest.isNotValid()) {
      ctx.response().setStatusCode(422).end();
      return;
    }

    service.saveTransacao(transacaoRequest)
      .onSuccess(response -> ctx.response()
        .setStatusCode(200)
        .putHeader("Content-Type", "application/json")
        .end(response.encode())
      )
      .onFailure(err -> {
        int errorStatusCode = getErrorStatusCode(err);
        if (errorStatusCode == 500)
          logger.error("Error processing request", err);
        ctx.response().setStatusCode(errorStatusCode).end();
      });
  }

  private int getErrorStatusCode(Throwable err) {
    if (err instanceof PgException exception) {
      String state = exception.getSqlState();
      return switch (state) {
        case CLIENT_NOT_FOUND_ERROR_CODE -> 404;
        case NOT_ENOUGH_SALDO_ERROR_CODE -> 422;
        default -> 500;
      };
    }
    return 500;
  }
}
