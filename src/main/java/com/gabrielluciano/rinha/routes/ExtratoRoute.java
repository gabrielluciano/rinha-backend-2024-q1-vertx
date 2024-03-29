package com.gabrielluciano.rinha.routes;

import com.gabrielluciano.rinha.exceptions.ClienteNaoEncontradoException;
import com.gabrielluciano.rinha.service.Service;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

public class ExtratoRoute implements Handler<RoutingContext> {

  private static final Logger logger = LoggerFactory.getLogger(ExtratoRoute.class);

  private final Service service;

  public ExtratoRoute(Service service) {
    this.service = service;
  }

  @Override
  public void handle(RoutingContext ctx) {
    Integer id = Integer.parseInt(ctx.pathParam("id"));

    service.getExtratoByClienteId(id)
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
    if (err instanceof ClienteNaoEncontradoException)
      return 404;
    return 500;
  }
}
