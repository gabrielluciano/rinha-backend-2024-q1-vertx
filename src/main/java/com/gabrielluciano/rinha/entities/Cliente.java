package com.gabrielluciano.rinha.entities;

import com.gabrielluciano.rinha.dto.TransacaoRequest;
import io.vertx.sqlclient.Row;

public class Cliente {

  private int id;
  private int saldo;
  private int limite;

  private Cliente() {
  }

  private Cliente(int id, int saldo, int limite) {
    this.id = id;
    this.saldo = saldo;
    this.limite = limite;
  }

  public static Cliente fromRow(Row row) {
    int id = row.get(Integer.class, "id");
    int saldo = row.get(Integer.class, "saldo");
    int limite = row.get(Integer.class, "limite");

    return new Cliente(id, saldo, limite);
  }

  public boolean canUpdateSaldo(TransacaoRequest request) {
    return request.isDebito() && hasEnoughSaldo(request.getValor());
  }

  public boolean cannotUpdateSaldo(TransacaoRequest request) {
    return !canUpdateSaldo(request);
  }

  private boolean hasEnoughSaldo(int valor) {
    return (this.saldo - valor) * (-1) <= this.limite;
  }

  public void updateSaldo(TransacaoRequest request) {
    this.saldo += request.getValor() * (request.isCredito() ? 1 : -1);
  }

  public int getId() {
    return id;
  }

  public int getSaldo() {
    return saldo;
  }

  public int getLimite() {
    return limite;
  }
}
