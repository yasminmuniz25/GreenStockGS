-- Estrutura inicial do banco do microsservico de Inventario
CREATE TABLE tb_lote (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    nome_produto     VARCHAR(255) NOT NULL,
    codigo_de_barras VARCHAR(255) NOT NULL,
    quantidade       INT          NOT NULL,
    data_validade    DATE         NOT NULL,
    status_lote      VARCHAR(30)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_lote_codigo_de_barras UNIQUE (codigo_de_barras)
);
