-- Carga inicial de lotes. As validades sao relativas a data de execucao,
-- assim os dados ficam coerentes em qualquer dia em que o projeto for executado.
INSERT INTO tb_lote (nome_produto, codigo_de_barras, quantidade, data_validade, status_lote) VALUES
('Leite Integral 1L',      '7891000100103',  50, DATE_ADD(CURDATE(), INTERVAL 20 DAY), 'EM_OFERTA'),
('Iogurte Natural 170g',   '7891000200202',  15, DATE_ADD(CURDATE(), INTERVAL 4 DAY),  'EM_OFERTA'),
('Pão de Forma Integral',  '7891000300301',   5, DATE_SUB(CURDATE(), INTERVAL 3 DAY),  'VENCIDO'),
('Queijo Minas Frescal',   '7891000400400',   0, DATE_ADD(CURDATE(), INTERVAL 10 DAY), 'ESGOTADO'),
('Banana Prata 1kg',       '7891000500509',  40, DATE_ADD(CURDATE(), INTERVAL 3 DAY),  'CRITICO'),
('Arroz Integral 5kg',     '7891000600608', 100, DATE_ADD(CURDATE(), INTERVAL 90 DAY), 'ATIVO');
