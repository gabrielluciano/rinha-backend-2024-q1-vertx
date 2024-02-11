package com.gabrielluciano.rinha;

import com.gabrielluciano.rinha.verticles.HTTPVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

public class RinhaApplication {

  private static final Logger logger = LoggerFactory.getLogger(RinhaApplication.class);

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    PgConnectOptions connectOptions = new PgConnectOptions()
      .setCachePreparedStatements(true)
      .setHost(System.getenv("PGHOST"))
      .setPort(5432)
      .setDatabase("rinha")
      .setUser("vertx")
      .setPassword("secret");

    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(Integer.parseInt(System.getenv("POOL_SIZE")));

    Pool pool = PgBuilder
      .pool()
      .with(poolOptions)
      .connectingTo(connectOptions)
      .using(vertx)
      .build();

    vertx.deployVerticle(new HTTPVerticle(pool))
      .onSuccess(handler -> logger.info("HTTP Verticle deployed"));
  }
}
