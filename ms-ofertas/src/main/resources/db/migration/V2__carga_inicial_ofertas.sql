-- Junto com a carga inicial de ms-inventario
--   lote 1 (Leite)   -> EM_OFERTA   -> oferta ATIVA
--   lote 2 (Iogurte) -> EM_OFERTA   -> oferta ATIVA
--   lote 4 (Queijo)  -> ESGOTADO    -> oferta ESGOTADA
INSERT INTO tb_ofertas (lote_id, produto_nome, quantidade_disponivel, preco_promocional, status_oferta) VALUES
(1, 'Leite Integral 1L',     20,  3.49, 'ATIVA'),
(2, 'Iogurte Natural 170g',  15,  1.99, 'ATIVA'),
(4, 'Queijo Minas Frescal',   0, 12.90, 'ESGOTADA');
