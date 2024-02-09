package com.gabrielluciano.rinha.dto;

public class TransacaoRequest {

  public char tipo;
  public int valor;
  public String descricao;

  public TransacaoRequest() {
  }

  public TransacaoRequest(char tipo, int valor, String descricao) {
    this.tipo = tipo;
    this.valor = valor;
    this.descricao = descricao;
  }
}
