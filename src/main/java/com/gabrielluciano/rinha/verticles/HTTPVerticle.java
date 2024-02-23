package com.gabrielluciano.rinha.verticles;

import com.gabrielluciano.rinha.routes.ExtratoRoute;
import com.gabrielluciano.rinha.routes.TransacaoRoute;
import com.gabrielluciano.rinha.service.Service;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.sqlclient.SqlClient;

public class HTTPVerticle extends AbstractVerticle {

  private final SqlClient sqlClient;
  private final Logger logger = LoggerFactory.getLogger(HTTPVerticle.class);
  private final int port = Integer.parseInt(System.getenv("HTTP_PORT"));

  public HTTPVerticle(SqlClient sqlClient) {
    super();
    this.sqlClient = sqlClient;
  }

  @Override
  public void start() throws Exception {
    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);

    Route transacaoRoute = router.route(HttpMethod.POST, "/clientes/:id/transacoes");
    Route extratoRoute = router.route(HttpMethod.GET, "/clientes/:id/extrato");

    Service service = new Service(sqlClient);

    transacaoRoute.handler(BodyHandler.create()).handler(new TransacaoRoute(service));
    extratoRoute.handler(new ExtratoRoute(service));

    server.requestHandler(router).listen(port)
      .onComplete(ar -> {
        if (ar.succeeded()) {
          logger.info("Application started on port: " + port);
        } else {
          logger.error("Error starting HTTP server");
        }
      });
  }
}
