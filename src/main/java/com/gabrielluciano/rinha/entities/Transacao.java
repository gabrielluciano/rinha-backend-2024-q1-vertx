package com.gabrielluciano.rinha.entities;

import java.time.OffsetDateTime;

public class Transacao {

  private final int clienteId;
  private final char tipo;
  private final int valor;
  private final String descricao;
  private final OffsetDateTime realizadaEm;

  public int getClienteId() {
    return clienteId;
  }

  public char getTipo() {
    return tipo;
  }

  public int getValor() {
    return valor;
  }

  public String getDescricao() {
    return descricao;
  }

  public OffsetDateTime getRealizadaEm() {
    return realizadaEm;
  }

  private Transacao(int clienteId, char tipo, int valor, String descricao, OffsetDateTime realizadaEm) {
    this.clienteId = clienteId;
    this.tipo = tipo;
    this.valor = valor;
    this.descricao = descricao;
    this.realizadaEm = realizadaEm;
  }

  public static Transacao.Builder builder() {
    return new Transacao.Builder();
  }

  public static class Builder {
    private int clienteId;
    private char tipo;
    private int valor;
    private String descricao;
    private OffsetDateTime realizadaEm;

    public Transacao build() {
      return new Transacao(
        this.clienteId,
        this.tipo,
        this.valor,
        this.descricao,
        this.realizadaEm
      );
    }

    public Builder clienteId(int clienteId) {
      this.clienteId = clienteId;
      return this;
    }

    public Builder tipo(char tipo) {
      this.tipo = tipo;
      return this;
    }

    public Builder valor(int valor) {
      this.valor = valor;
      return this;
    }

    public Builder descricao(String descricao) {
      this.descricao = descricao;
      return this;
    }

    public Builder realizadaEm(OffsetDateTime realizadaEm) {
      this.realizadaEm = realizadaEm;
      return this;
    }
  }
}
