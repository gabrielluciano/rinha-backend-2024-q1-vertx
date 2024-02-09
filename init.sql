DROP TABLE IF EXISTS clientes;
DROP TABLE IF EXISTS transacoes;

CREATE TABLE IF NOT EXISTS clientes (
    id INTEGER PRIMARY KEY,
    limite INTEGER NOT NULL,
    saldo INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS transacoes (
    id SERIAL PRIMARY KEY,
    cliente_id INTEGER NOT NULL REFERENCES clientes(id),
    tipo CHAR(1) NOT NULL,
    valor INTEGER NOT NULL,
    descricao VARCHAR(10) NOT NULL,
    realizada_em TIMESTAMPTZ NOT NULL
);

INSERT INTO clientes
    (id, limite, saldo)
VALUES
    (1, 100000,     0),
    (2, 80000,      0),
    (3, 1000000,    0),
    (4, 10000000,   0),
    (5, 500000,     0);
