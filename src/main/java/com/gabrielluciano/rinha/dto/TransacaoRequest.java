package com.gabrielluciano.rinha.dto;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RequestBody;

public class TransacaoRequest {

  private char tipo;
  private int valor;
  private String descricao;

  private TransacaoRequest() {
  }

  private TransacaoRequest(char tipo, int valor, String descricao) {
    this.tipo = tipo;
    this.valor = valor;
    this.descricao = descricao;
  }

  public static TransacaoRequest fromRequestBody(RequestBody body) {
    JsonObject json = body.asJsonObject();
    String tipo = json.getString("tipo");
    String valor = json.getString("valor");
    String descricao = json.getString("descricao");

    return new TransacaoRequest(tipo.charAt(0), Integer.parseInt(valor), descricao);
  }

  public boolean isDebito() {
    return this.tipo == 'd';
  }

  public boolean isCredito() {
    return this.tipo == 'c';
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
}
