package com.gabrielluciano.rinha.repository;

import com.gabrielluciano.rinha.entities.Cliente;
import com.gabrielluciano.rinha.entities.Transacao;
import com.gabrielluciano.rinha.exceptions.ClienteNaoEncontradoException;
import com.gabrielluciano.rinha.sql.Query;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.List;

public class Repository {

  private final SqlConnection connection;

  public Repository(SqlConnection connection) {
    this.connection = connection;
  }

  public Future<Cliente> findClienteById(Integer id) {
    return connection
      .preparedQuery(Query.SELECT_CLIENTE_BY_ID)
      .execute(Tuple.of(id))
      .flatMap(this::clienteFromRowSet);
  }

  public Future<Void> saveCliente(Cliente cliente) {
    return connection
      .preparedQuery(Query.UPDATE_CLIENTE)
      .execute(Tuple.of(cliente.getSaldo(), cliente.getId()))
      .mapEmpty();
  }

  public Future<Void> saveTransacao(Transacao transacao) {
    return connection
      .preparedQuery(Query.INSERT_TRANSACAO)
      .execute(Tuple.of(
        transacao.getClienteId(),
        transacao.getTipo(),
        transacao.getValor(),
        transacao.getRealizadaEm()
      ))
      .mapEmpty();
  }

  public Future<List<Transacao>> getTransacoesByClienteId(Integer id) {
    return connection
      .preparedQuery(Query.SELECT_TRANSACOES_BY_CLIENTE_ID)
      .execute(Tuple.of(id))
      .flatMap(this::transacoesFromRowSet);
  }

  private Future<Cliente> clienteFromRowSet(RowSet<Row> rowSet) {
    if (!rowSet.iterator().hasNext())
      return Future.failedFuture(new ClienteNaoEncontradoException());
    Cliente cliente = Cliente.fromRow(rowSet.iterator().next());
    return Future.succeededFuture(cliente);
  }

  private Future<List<Transacao>> transacoesFromRowSet(RowSet<Row> rowSet) {
    List<Transacao> transacoes = new ArrayList<>();
    for (Row row : rowSet) {
      Transacao transacao = Transacao.builder()
        .tipo(row.getString("tipo").charAt(0))
        .valor(row.getInteger("valor"))
        .descricao(row.getString("descricao"))
        .realizadaEm(row.getOffsetDateTime("realizada_em"))
        .build();
      transacoes.add(transacao);
    }
    return Future.succeededFuture(transacoes);
  }
}
