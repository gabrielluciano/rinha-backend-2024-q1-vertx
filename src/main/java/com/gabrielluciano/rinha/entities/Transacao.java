package com.gabrielluciano.rinha.entities;

import com.gabrielluciano.rinha.util.Validations;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RequestBody;

public class Transacao {

  private final int clienteId;
  private final String tipo;
  private final String valor;
  private final String descricao;

  private Transacao(int clienteId, String tipo, String valor, String descricao) {
    this.clienteId = clienteId;
    this.tipo = tipo;
    this.valor = valor;
    this.descricao = descricao;
  }

  public static Transacao fromRequestBodyAndClienteId(RequestBody body, int clienteId) {
    JsonObject json = body.asJsonObject();
    String tipo = json.getString("tipo");
    String valor = json.getString("valor");
    String descricao = json.getString("descricao");

    return new Transacao(clienteId, tipo, valor, descricao);
  }

  public boolean isValid() {
    return Validations.isValidTipo(this.tipo)
      && Validations.isValidValor(this.valor)
      && Validations.isValidDescricao(this.descricao);
  }

  public boolean isNotValid() {
    return !isValid();
  }

  public int getClienteId() {
    return clienteId;
  }

  public String getTipo() {
    return tipo;
  }

  public String getValor() {
    return valor;
  }

  public String getDescricao() {
    return descricao;
  }
}
