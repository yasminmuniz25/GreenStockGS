-- Estrutura inicial do banco do microsservico de Ofertas.
-- lote_id referencia um lote do ms-inventario (outro banco), por isso nao ha FOREIGN KEY:
-- a consistencia entre os servicos e garantida pela comunicacao sincrona (OpenFeign).
CREATE TABLE tb_ofertas (
    id                    BIGINT         NOT NULL AUTO_INCREMENT,
    lote_id               BIGINT         NOT NULL,
    produto_nome          VARCHAR(255)   NOT NULL,
    quantidade_disponivel INT            NOT NULL,
    preco_promocional     DECIMAL(10, 2) NOT NULL,
    status_oferta         VARCHAR(30)    NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_ofertas_lote_id ON tb_ofertas (lote_id);
