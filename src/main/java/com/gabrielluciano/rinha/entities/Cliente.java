package com.gabrielluciano.rinha.entities;

public class Cliente {

  public int id;
  public int saldo;
  public int limite;

  public Cliente() {
  }

  public Cliente(int id, int saldo, int limite) {
    this.id = id;
    this.saldo = saldo;
    this.limite = limite;
  }
}
