# GreenStock

Global Solution (3º SIPF) – Microservice and Web Engineering & IT Services
Profª Aparecida F. Castello Rosa

Arquitetura de **microsserviços com comunicação síncrona**, usando Java, Spring Boot e MySQL.

## Integrantes

| Nome                           | RM     |
|--------------------------------|--------|
| Ana Carolina Calheiros de Lima | 555316 |
| Stefany Miyahara Omori         | 558792 |
| Yasmin Sayuri Iechika Muniz    | 559011 |

---

## Sumário

1. [Problema](#1-problema)
2. [Solução](#2-solução)
3. [Arquitetura](#3-arquitetura)
4. [Pré-requisitos](#4-pré-requisitos)
5. [Como executar](#5-como-executar)
6. [Swagger / OpenAPI](#6-documentação-swagger--openapi)
7. [Endpoints](#7-endpoints-versão-1)
8. [Como testar](#8-como-testar)
9. [Banco de dados e migrations](#9-banco-de-dados-e-migrations)
10. [Problemas comuns](#10-problemas-comuns)

---

## 1. Problema

Supermercados e varejistas de alimentos perdem grande volume de produtos que vencem nas prateleiras e nos estoques. Muitas vezes o produto ainda está próprio para consumo, mas ninguém percebe a tempo de vendê-lo com desconto. O resultado é desperdício de alimentos e prejuízo financeiro.

## 2. Solução

O **GreenStock** separa o problema em dois microsserviços que conversam entre si:

- **ms-inventario**: controla os **lotes** de produtos, com quantidade, data de validade e status (`ATIVO`, `CRITICO`, `EM_OFERTA`, `VENCIDO`, `ESGOTADO`).
- **ms-ofertas**: cria **ofertas de excedente** (promoção) para lotes próximos do vencimento. Status da oferta: `ATIVA`, `ESGOTADA`, `EXPIRADA`.

A comunicação entre eles é **síncrona (OpenFeign)** e altera o status **nos dois sentidos**:

| Fluxo | Quem chama | Quem é chamado | Efeito |
|---|---|---|---|
| **A** – Criar oferta | `ms-ofertas` | `ms-inventario` | Consulta o lote (`GET`), valida a regra de negócio e, ao salvar a oferta, muda o lote para `EM_OFERTA` (`PUT .../status`) |
| **B** – Lote vence ou esgota | `ms-inventario` | `ms-ofertas` | Lote `VENCIDO` → ofertas do lote viram `EXPIRADA`. Lote `ESGOTADO` → ofertas viram `ESGOTADA` |

Se o outro serviço estiver indisponível, o **Circuit Breaker** com **Fallback** responde `503 Service Unavailable` e a transação local é desfeita (rollback), mantendo os dois bancos consistentes.

---

## 3. Arquitetura

```
                       +-------------------+
                       |   Eureka Server   |  :8761
                       +---------+---------+
                                 ^  registro / descoberta
          +----------------------+-----------------------+
          |                      |                       |
  +-------+-------+     +--------+---------+    +--------+---------+
  |  API Gateway  |     |  ms-inventario   |<-->|    ms-ofertas    |
  |     :8082     |---->|  porta aleatoria |    |  porta aleatoria |
  +---------------+     +--------+---------+    +--------+---------+
                                 |   OpenFeign + LoadBalancer
                                 |   + Circuit Breaker + Fallback
                        +--------+---------+    +--------+---------+
                        | MySQL inventario |    |  MySQL ofertas   |
                        |      :3307       |    |      :3308       |
                        +------------------+    +------------------+
```

| Componente | Porta | Descrição |
|---|---|---|
| `eureka-server` | 8761 | Service discovery |
| `api-gateway` | 8082 | Entrada única; rotas automáticas via Eureka |
| `ms-inventario` | aleatória | Lotes de produtos |
| `ms-ofertas` | aleatória | Ofertas de excedente |
| MySQL do inventário | 3307 | Banco `greenstock_inventario` |
| MySQL de ofertas | 3308 | Banco `greenstock_ofertas` |

Cada microsserviço tem **banco de dados próprio e independente**. Nenhum schema ou tabela é compartilhado. Como `ms-ofertas` guarda apenas o `lote_id` (sem chave estrangeira entre bancos), a consistência entre os dois é garantida pela comunicação síncrona.

### Tecnologias

| Item | Versão / detalhe |
|---|---|
| Microsserviços (`ms-inventario`, `ms-ofertas`) | Java 25, Spring Boot 4.0.9, Spring Cloud 2025.1.4 |
| `eureka-server`, `api-gateway` | Spring Boot 3.5.14, Spring Cloud 2025.0.2 |
| Persistência | Spring Data JPA, MySQL 8.4 (Docker), Flyway |
| Comunicação | OpenFeign, Spring Cloud LoadBalancer, Resilience4j (Circuit Breaker) e Fallback |
| Validação e docs | Bean Validation, springdoc OpenAPI / Swagger UI |
| Build | Maven (wrapper `mvnw` incluído), Lombok |

### Estrutura do repositório

```
greenstock/
├── docker-compose.yml                    # os dois bancos MySQL
├── GreenStock.postman_collection.json    # testes automatizados (Postman)
├── eureka-server/
├── api-gateway/
├── ms-inventario/    # controller / service / repositories / entities / dtos / exceptions / clients
└── ms-ofertas/       # controller / service / repositories / entities / dtos / exceptions / clients
```

---

## 4. Pré-requisitos

- **JDK 25** (confirme com `java -version`)
- **Docker Desktop** em execução
- **Git**
- Maven não precisa estar instalado: cada projeto tem o wrapper (`mvnw`)
- (opcional) **Postman**, para rodar a coleção de testes

Portas livres: 8761, 8082, 3307 e 3308.

---

## 5. Como executar

Abra um terminal para cada passo. Os comandos abaixo são para Windows (PowerShell). No Linux/macOS, troque `.\mvnw.cmd` por `./mvnw`.

### Passo 1 – Clonar o repositório

```powershell
git clone https://github.com/yasminmuniz25/greenstockgs.git
cd greenstock
```

### Passo 2 – Subir os bancos MySQL

Na raiz do repositório:

```powershell
docker compose up -d --wait
```

O `--wait` só devolve o terminal quando os dois MySQL estão prontos (leva cerca de 30 segundos na primeira vez). Para conferir:

```powershell
docker compose ps
```

Os dois containers (`mysql-inventario` e `mysql-ofertas`) devem estar `running` / `healthy`.

### Passo 3 – Eureka Server (terminal 1)

```powershell
cd eureka-server
.\mvnw.cmd spring-boot:run
```

Abra http://localhost:8761 para ver o painel do Eureka.

### Passo 4 – API Gateway (terminal 2)

```powershell
cd api-gateway
.\mvnw.cmd spring-boot:run
```

### Passo 5 – ms-inventario (terminal 3)

```powershell
cd ms-inventario
.\mvnw.cmd spring-boot:run
```

Na primeira execução o Flyway cria a tabela e carrega os dados iniciais.

### Passo 6 – ms-ofertas (terminal 4)

```powershell
cd ms-ofertas
.\mvnw.cmd spring-boot:run
```

### Passo 7 (opcional) – Segunda instância do ms-ofertas

Para demonstrar múltiplas instâncias com porta aleatória, abra **outro terminal** e rode o mesmo comando do passo 6. Cada instância sobe numa porta diferente. No painel do Eureka (http://localhost:8761), o `MS-OFERTAS` passa a listar duas instâncias.

### Como descobrir a porta de cada instância

Como `ms-inventario` e `ms-ofertas` sobem em porta aleatória, use um destes caminhos:

- No painel do Eureka (http://localhost:8761), clique no link da instância na coluna **Status**.
- No console do serviço, procure a linha `Tomcat started on port XXXXX`.

### Executando pelo IntelliJ

Abra cada aplicação (`EurekaServerApplication`, `ApiGatewayApplication`, `MsInventarioApplication`, `MsOfertasApplication`) e clique na seta verde ao lado do `main`, na mesma ordem. Para duas instâncias do ofertas, em `Run > Edit Configurations`, habilite **Allow multiple instances**.

### Parar tudo

Feche os terminais dos serviços (Ctrl+C) e, na raiz:

```powershell
docker compose down
```

Para apagar também os dados dos bancos (voltar à carga inicial), use `docker compose down -v`.

---

## 6. Documentação Swagger / OpenAPI

Cada microsserviço expõe seu Swagger UI na **porta da instância** (veja como descobri-la na seção anterior):

- ms-inventario: `http://localhost:<porta>/swagger-ui.html`
- ms-ofertas: `http://localhost:<porta>/swagger-ui.html`

Todos os endpoints podem ser executados por lá (botão **Try it out**).

---

## 7. Endpoints (versão 1)

Pelo API Gateway (porta 8082), o prefixo da rota é o nome do serviço em minúsculas:

- `http://localhost:8082/ms-inventario/api/v1/...`
- `http://localhost:8082/ms-ofertas/api/v1/...`

### ms-inventario – `/api/v1/lotes`

| Método | Rota | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| GET | `/api/v1/lotes` | Lista todos os lotes | 200 | |
| GET | `/api/v1/lotes/{id}` | Busca lote por id | 200 | 400, 404 |
| POST | `/api/v1/lotes` | Cria lote | 201 + `Location` | 400, 422 |
| PUT | `/api/v1/lotes/{id}` | Atualiza o lote inteiro | 200 | 400, 404, 422 |
| PUT | `/api/v1/lotes/{id}/status` | Altera só o status (**dispara o fluxo B**) | 200 | 400, 404, 422, 503 |
| DELETE | `/api/v1/lotes/{id}` | Remove lote | 204 | 404 |

### ms-ofertas – `/api/v1/ofertas`

| Método | Rota | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| GET | `/api/v1/ofertas` | Lista todas as ofertas | 200 | |
| GET | `/api/v1/ofertas/{id}` | Busca oferta por id | 200 | 400, 404 |
| POST | `/api/v1/ofertas` | Cria oferta (**dispara o fluxo A**) | 201 + `Location` | 400, 404, 422, 503 |
| PUT | `/api/v1/ofertas/{id}` | Atualiza oferta | 200 | 400, 404, 422 |
| PUT | `/api/v1/ofertas/lote/{loteId}/status` | Altera o status das ofertas de um lote (chamado pelo inventário no fluxo B) | 200 | 400, 422 |
| DELETE | `/api/v1/ofertas/{id}` | Remove oferta | 204 | 404 |

### Corpos de requisição

Lote (`POST` e `PUT /api/v1/lotes`):

```json
{
  "nomeProduto": "Iogurte Natural 170g",
  "codigoDeBarras": "7891000100999",
  "quantidade": 40,
  "dataValidade": "2026-12-31",
  "statusLote": "ATIVO"
}
```

Status do lote (`PUT /api/v1/lotes/{id}/status`):

```json
{ "statusLote": "VENCIDO" }
```

Oferta (`POST` e `PUT /api/v1/ofertas`):

```json
{
  "loteId": 6,
  "produtoNome": "Arroz Integral 5kg",
  "quantidadeDisponivel": 10,
  "precoPromocional": 19.90,
  "statusOferta": "ATIVA"
}
```

Valores aceitos: `statusLote` = `ATIVO`, `CRITICO`, `EM_OFERTA`, `VENCIDO`, `ESGOTADO`. `statusOferta` = `ATIVA`, `ESGOTADA`, `EXPIRADA`.

Códigos de erro: **400** JSON malformado ou parâmetro com tipo errado; **404** recurso não encontrado; **422** validação de campos ou regra de negócio; **503** outro microsserviço indisponível (fallback).

---

## 8. Como testar

Você pode testar de quatro formas: **Postman** (automatizado, seção 8.1), **Swagger** (seção 6), o **roteiro manual** (seções 8.3 a 8.6) ou **pelo terminal com PowerShell** (seção 8.7). Todos os exemplos usam o API Gateway (`http://localhost:8082`). A seção 8.8 mostra como **ver nos logs** as chamadas entre os microsserviços.

### 8.1 Teste automatizado com Postman (recomendado)

1. No Postman: **Import** → selecione o arquivo `GreenStock.postman_collection.json` (na raiz do repositório).
2. Com tudo no ar (passos 1 a 6 da seção 5), clique na coleção → **Run** (Collection Runner).
3. Marque as pastas **0 a 4** e execute. Cada requisição tem testes (`pm.test`) que validam status HTTP e corpo da resposta.
4. A pasta **5 (Resiliência)** é **manual**: exige parar serviços, então rode as requisições uma a uma (veja a seção 8.5).

A coleção cria o próprio lote e a própria oferta de teste (com código de barras aleatório) e apaga tudo no final, então pode ser executada várias vezes sem reiniciar nada.

| Pasta | O que valida |
|---|---|
| 0. Pré-requisitos | Gateway, `MS-INVENTARIO` e `MS-OFERTAS` registrados no Eureka |
| 1. ms-inventario | CRUD completo, `Location` no POST, 400 / 404 / 422 |
| 2. ms-ofertas | Leitura, validações 404 / 422 |
| 3. Comunicação (Feign) | Fluxos A e B, regras de negócio e os efeitos nos dois bancos |
| 4. Limpeza | DELETE com 204 e 404 |
| 5. Resiliência (manual) | Circuit Breaker, Fallback 503 e rollback |

> Se a primeira chamada falhar por timeout, aguarde cerca de 30 segundos (registro dos serviços no Eureka) e rode de novo.

### 8.2 Antes de testar: dados iniciais e estado do ambiente

A carga inicial (Flyway `V2`) cria os dados abaixo. Os cenários manuais foram desenhados para usar **lotes diferentes** e não interferirem entre si.

**Lotes (`ms-inventario`)**

| id | Produto | Status | Usado no cenário |
|---|---|---|---|
| 1 | Leite Integral 1L | `EM_OFERTA` | 8.3 – fluxo B (vence) |
| 2 | Iogurte Natural 170g | `EM_OFERTA` | 8.3 – fluxo B (esgota) |
| 3 | Pão de Forma Integral | `VENCIDO` | 8.4 – regra de negócio |
| 4 | Queijo Minas Frescal | `ESGOTADO` | 8.4 – regra de negócio |
| 5 | Banana Prata 1kg | `CRITICO` | 8.5 – resiliência |
| 6 | Arroz Integral 5kg | `ATIVO` | 8.3 – fluxo A (cria oferta) |

**Ofertas (`ms-ofertas`)**

| id | lote_id | Produto | Status |
|---|---|---|---|
| 1 | 1 | Leite Integral 1L | `ATIVA` |
| 2 | 2 | Iogurte Natural 170g | `ATIVA` |
| 3 | 4 | Queijo Minas Frescal | `ESGOTADA` |

Para **voltar a este estado inicial** a qualquer momento: pare os microsserviços, rode `docker compose down -v`, depois `docker compose up -d --wait` e suba os microsserviços de novo.

**Confira primeiro que a infraestrutura está de pé:**

1. http://localhost:8761 lista `API-GATEWAY`, `MS-INVENTARIO` e `MS-OFERTAS` com status **UP**.
2. `GET http://localhost:8082/ms-inventario/api/v1/lotes` retorna os 6 lotes.
3. `GET http://localhost:8082/ms-ofertas/api/v1/ofertas` retorna as 3 ofertas.

### 8.3 Testando a comunicação entre os microsserviços

#### Resumo rápido (uma linha por relacionamento)

| # | O que fazer | Resultado esperado | O que isso prova |
|---|---|---|---|
| 1 | `POST /ms-ofertas/api/v1/ofertas` com `loteId` 6 | `201`; depois `GET /ms-inventario/api/v1/lotes/6` mostra `EM_OFERTA` | **ofertas → inventário**: o ofertas consultou e alterou o lote |
| 2 | `PUT /ms-inventario/api/v1/lotes/1/status` com `VENCIDO` | `200`; depois `GET /ms-ofertas/api/v1/ofertas/1` mostra `EXPIRADA` | **inventário → ofertas**: o inventário alterou a oferta |
| 3 | `PUT /ms-inventario/api/v1/lotes/2/status` com `ESGOTADO` | `200`; depois `GET /ms-ofertas/api/v1/ofertas/2` mostra `ESGOTADA` | idem, para o outro status |
| 4 | `POST /ms-ofertas/api/v1/ofertas` com `loteId` 3 (vencido) | `422` | regra de negócio aplicada com dados vindos do outro serviço |
| 5 | `POST /ms-ofertas/api/v1/ofertas` com `loteId` 999 | `404` | erro do outro serviço traduzido pelo Fallback |
| 6 | Parar um serviço e repetir 1 ou 2 | `503` e nenhum dado alterado | Circuit Breaker, Fallback e rollback |

Os detalhes de cada cenário estão logo abaixo.

#### Como funciona (para entender o que se está testando)

**Fluxo A – criar oferta (`ms-ofertas` → `ms-inventario`)**

```
Cliente        ms-ofertas                          ms-inventario
  | POST /ofertas  |                                     |
  |--------------->| 1. GET  /api/v1/lotes/{loteId}      |
  |                |------------------------------------>|  (Feign + Eureka + LoadBalancer)
  |                |<------------------------------------|  lote {status}
  |                | 2. regra: VENCIDO/ESGOTADO? -> 422  |
  |                | 3. salva a oferta (banco ofertas)   |
  |                | 4. PUT  /api/v1/lotes/{id}/status   |
  |                |------------------------------------>|  lote -> EM_OFERTA
  |   201 Created  |<------------------------------------|
  |<---------------|
```

**Fluxo B – lote vence/esgota (`ms-inventario` → `ms-ofertas`)**

```
Cliente        ms-inventario                       ms-ofertas
  | PUT /lotes/{id}/status {VENCIDO}                      |
  |--------------->| 1. atualiza o lote (banco inventário)|
  |                | 2. PUT /api/v1/ofertas/lote/{id}/status {EXPIRADA}
  |                |------------------------------------->|  ofertas do lote -> EXPIRADA
  |    200 OK      |<-------------------------------------|
  |<---------------|
```

Nenhuma chamada usa URL fixa: os serviços se encontram pelo **nome** (`ms-inventario`, `ms-ofertas`) via Eureka, e o Spring Cloud LoadBalancer escolhe a instância.

#### Cenário 1 – Fluxo A: criar oferta altera o status do lote

1. Confirme o estado antes: `GET /ms-inventario/api/v1/lotes/6` → `statusLote` = `ATIVO`.
2. Crie a oferta: `POST http://localhost:8082/ms-ofertas/api/v1/ofertas`

   ```json
   {
     "loteId": 6,
     "produtoNome": "Arroz Integral 5kg",
     "quantidadeDisponivel": 10,
     "precoPromocional": 19.90,
     "statusOferta": "ATIVA"
   }
   ```

   **Esperado:** `201 Created`, header `Location` apontando para a nova oferta, `statusOferta` = `ATIVA`.
3. Confira o efeito **no outro serviço**: `GET /ms-inventario/api/v1/lotes/6`
   **Esperado:** `statusLote` = **`EM_OFERTA`** (alterado pelo `ms-ofertas`, não por você).

#### Cenário 2 – Fluxo B: lote vencido expira as ofertas

1. Antes: `GET /ms-ofertas/api/v1/ofertas/1` → `statusOferta` = `ATIVA` (oferta do lote 1).
2. Marque o lote como vencido: `PUT http://localhost:8082/ms-inventario/api/v1/lotes/1/status`

   ```json
   { "statusLote": "VENCIDO" }
   ```

   **Esperado:** `200 OK`, `statusLote` = `VENCIDO`.
3. Confira o efeito **no outro serviço**: `GET /ms-ofertas/api/v1/ofertas/1`
   **Esperado:** `statusOferta` = **`EXPIRADA`** (alterado pelo `ms-inventario`).

#### Cenário 3 – Fluxo B: lote esgotado esgota as ofertas

1. Antes: `GET /ms-ofertas/api/v1/ofertas/2` → `ATIVA` (oferta do lote 2).
2. `PUT /ms-inventario/api/v1/lotes/2/status` com `{ "statusLote": "ESGOTADO" }` → `200 OK`.
3. `GET /ms-ofertas/api/v1/ofertas/2` → **`ESGOTADA`**.

### 8.4 Testando as regras de negócio e os erros entre os serviços

| # | Requisição | Esperado | Por quê |
|---|---|---|---|
| 1 | `POST /ms-ofertas/api/v1/ofertas` com `loteId` **3** (Pão, `VENCIDO`) | **422** | Regra: lote vencido não vira oferta |
| 2 | `POST /ms-ofertas/api/v1/ofertas` com `loteId` **4** (Queijo, `ESGOTADO`) | **422** | Regra: lote esgotado não vira oferta |
| 3 | `POST /ms-ofertas/api/v1/ofertas` com `loteId` **999** | **404** | O inventário responde 404 e o fallback preserva o 404 (não vira 503) |
| 4 | `POST /ms-ofertas/api/v1/ofertas` com `precoPromocional` 0 e `produtoNome` vazio | **422** | Validação de campos, **antes** de chamar o outro serviço |
| 5 | `PUT /ms-inventario/api/v1/lotes/5/status` com `{ "statusLote": "FOO" }` | **400** | Valor fora do enum |
| 6 | `PUT /ms-ofertas/api/v1/ofertas/lote/999/status` com `{ "statusOferta": "EXPIRADA" }` | **200** e `[]` | Lote sem ofertas: não é erro, lista vazia |

### 8.5 Testando Circuit Breaker, Fallback e consistência (rollback)

Configuração aplicada nos dois serviços (`application.properties`):

| Parâmetro | Valor | Significado |
|---|---|---|
| `sliding-window-size` | 5 | O circuito avalia as últimas 5 chamadas |
| `minimum-number-of-calls` | 3 | Só decide abrir após 3 chamadas |
| `failure-rate-threshold` | 50 | Abre se 50% ou mais falharem |
| `wait-duration-in-open-state` | 10s | Tempo com o circuito aberto antes de testar de novo |
| `timeout-duration` (TimeLimiter) | 5s | Tempo máximo por chamada |

#### Cenário 4 – `ms-inventario` fora do ar (fluxo A falha)

1. Anote quantas ofertas existem: `GET /ms-ofertas/api/v1/ofertas`.
2. **Pare o `ms-inventario`** (Ctrl+C no terminal dele).
3. Tente criar uma oferta: `POST /ms-ofertas/api/v1/ofertas` com `loteId` 5.
   **Esperado:** `503 Service Unavailable` com a mensagem *"ms-inventario indisponível no momento"* (resposta do **Fallback**).
4. **Repita o POST 3 ou mais vezes.** O circuito **abre** e as respostas passam a voltar quase instantaneamente, sem esperar timeout.
5. `GET /ms-ofertas/api/v1/ofertas` continua funcionando (não depende do inventário) e a **quantidade de ofertas não mudou**.
6. **Suba o `ms-inventario` de novo.** Aguarde cerca de 30 segundos (registro no Eureka) e os 10 s do circuito aberto. O POST volta a funcionar (`201`).

#### Cenário 5 – `ms-ofertas` fora do ar (fluxo B falha e desfaz a alteração)

1. Confira o lote 5: `GET /ms-inventario/api/v1/lotes/5` → `CRITICO`.
2. **Pare o `ms-ofertas`** (todas as instâncias).
3. `PUT /ms-inventario/api/v1/lotes/5/status` com `{ "statusLote": "VENCIDO" }`.
   **Esperado:** `503` com a mensagem *"ms-ofertas indisponível no momento"*.
4. **Confirme o rollback:** `GET /ms-inventario/api/v1/lotes/5`
   **Esperado:** continua `CRITICO`. Como o inventário não conseguiu avisar o ofertas, a mudança foi **desfeita**, e os dois bancos permanecem consistentes.
5. Suba o `ms-ofertas` de novo e repita o passo 3: agora retorna `200`.

### 8.6 Testando múltiplas instâncias e Load Balancer

1. Suba **duas instâncias** do `ms-ofertas` (passo 7 da seção 5). No Eureka, `MS-OFERTAS` mostra 2 instâncias, cada uma em porta diferente.
2. Para **enxergar** qual instância atende cada chamada, antes de subir cada uma, ative o log de requisições (PowerShell):

   ```powershell
   cd ms-ofertas
   $env:LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WEB_SERVLET_DISPATCHERSERVLET="DEBUG"
   .\mvnw.cmd spring-boot:run
   ```

3. Chame `GET http://localhost:8082/ms-ofertas/api/v1/ofertas` cerca de 6 vezes.
   **Esperado:** as linhas `GET "/api/v1/ofertas"` aparecem **alternando** entre os dois terminais, evidência do Load Balancer.
4. O mesmo vale para o fluxo B: cada `PUT .../lotes/{id}/status` com `VENCIDO` ou `ESGOTADO` faz o inventário chamar o `ms-ofertas`, e o Feign distribui entre as instâncias.

### 8.7 Testando pelo terminal (PowerShell)

Alternativa ao Postman, sem instalar nada. Abra um PowerShell e cole o bloco de funções abaixo uma vez; depois cole os cenários que quiser. Em caso de erro HTTP (4xx/5xx), a função imprime `HTTP <código>`.

```powershell
$gw = "http://localhost:8082"
function Req($m, $u, $b = $null) {
  try {
    if ($b) { Invoke-RestMethod -Method $m -Uri $u -ContentType "application/json" -Body $b }
    else    { Invoke-RestMethod -Method $m -Uri $u }
  } catch { "HTTP " + [int]$_.Exception.Response.StatusCode }
}
```

**Cenário 1 – ofertas → inventário** (lote 6 deve passar de `ATIVO` para `EM_OFERTA`)

```powershell
Req Get "$gw/ms-inventario/api/v1/lotes/6" | Select-Object id, nomeProduto, statusLote
$oferta = '{"loteId":6,"produtoNome":"Arroz Integral 5kg","quantidadeDisponivel":10,"precoPromocional":19.90,"statusOferta":"ATIVA"}'
Req Post "$gw/ms-ofertas/api/v1/ofertas" $oferta
Req Get "$gw/ms-inventario/api/v1/lotes/6" | Select-Object id, nomeProduto, statusLote
```

**Cenários 2 e 3 – inventário → ofertas** (ofertas 1 e 2 devem virar `EXPIRADA` e `ESGOTADA`)

```powershell
Req Put "$gw/ms-inventario/api/v1/lotes/1/status" '{"statusLote":"VENCIDO"}'
Req Get "$gw/ms-ofertas/api/v1/ofertas/1" | Select-Object id, loteId, statusOferta

Req Put "$gw/ms-inventario/api/v1/lotes/2/status" '{"statusLote":"ESGOTADO"}'
Req Get "$gw/ms-ofertas/api/v1/ofertas/2" | Select-Object id, loteId, statusOferta
```

**Regras de negócio e erros** (esperado: `HTTP 422`, `HTTP 422` e `HTTP 404`)

```powershell
Req Post "$gw/ms-ofertas/api/v1/ofertas" '{"loteId":3,"produtoNome":"Pao","quantidadeDisponivel":1,"precoPromocional":1.5,"statusOferta":"ATIVA"}'
Req Post "$gw/ms-ofertas/api/v1/ofertas" '{"loteId":4,"produtoNome":"Queijo","quantidadeDisponivel":1,"precoPromocional":1.5,"statusOferta":"ATIVA"}'
Req Post "$gw/ms-ofertas/api/v1/ofertas" '{"loteId":999,"produtoNome":"X","quantidadeDisponivel":1,"precoPromocional":1.5,"statusOferta":"ATIVA"}'
```

**Circuit Breaker e Fallback** (com o `ms-inventario` parado; esperado: `HTTP 503` em todas, e as últimas respostas bem mais rápidas, pois o circuito abriu)

```powershell
$oferta5 = '{"loteId":5,"produtoNome":"Banana","quantidadeDisponivel":1,"precoPromocional":1.5,"statusOferta":"ATIVA"}'
1..5 | ForEach-Object {
  $ms = (Measure-Command { Req Post "$gw/ms-ofertas/api/v1/ofertas" $oferta5 | Out-Host }).TotalMilliseconds
  "tentativa $_ : $([int]$ms) ms"
}
```

**Rollback** (com o `ms-ofertas` parado; esperado: `HTTP 503` e o lote 5 continua `CRITICO`)

```powershell
Req Put "$gw/ms-inventario/api/v1/lotes/5/status" '{"statusLote":"VENCIDO"}'
Req Get "$gw/ms-inventario/api/v1/lotes/5" | Select-Object id, statusLote
```

### 8.8 Vendo as chamadas entre os serviços nos logs (opcional)

Por padrão o Feign não registra suas chamadas. Para **enxergar** a chamada saindo de um serviço para o outro (inclusive o endereço resolvido pelo Eureka, sem URL fixa), suba o serviço que **chama** com o log do Feign ligado. No PowerShell, na pasta do serviço:

```powershell
cd ms-ofertas
$env:LOGGING_LEVEL_COM_GREENSTOCK="DEBUG"
$env:SPRING_CLOUD_OPENFEIGN_CLIENT_CONFIG_DEFAULT_LOGGER_LEVEL="full"
.\mvnw.cmd spring-boot:run
```

Repita o cenário 1 e procure no console do `ms-ofertas` linhas como:

```
[InventarioClient#buscarLote] ---> GET http://<ip-do-inventario>:<porta>/api/v1/lotes/6 HTTP/1.1
[InventarioClient#buscarLote] <--- HTTP/1.1 200 (xx ms)
[InventarioClient#atualizarStatus] ---> PUT http://<ip-do-inventario>:<porta>/api/v1/lotes/6/status HTTP/1.1
```

O endereço (`<ip>:<porta>`) foi resolvido pelo Eureka e pelo LoadBalancer em tempo de execução; ele muda a cada subida, o que comprova que não há URL fixa. Para o fluxo B, faça o mesmo na pasta `ms-inventario` e repita o cenário 2 (o client é o `OfertasClient`).

### 8.9 Resumo do que cada teste comprova

| Requisito do enunciado | Onde é comprovado |
|---|---|
| Comunicação síncrona entre os dois microsserviços | Cenários 1, 2 e 3 |
| Status alterado pela comunicação entre os serviços | Cenário 1 (lote) e Cenários 2 e 3 (oferta) |
| Eureka (8761) e API Gateway (8082) | Seção 8.2 |
| OpenFeign sem URL fixa | Fluxos A e B (chamadas por nome de serviço) |
| Load Balancer e múltiplas instâncias com porta aleatória | Seção 8.6 |
| Circuit Breaker e Fallback | Cenários 4 e 5 |
| Tratamento de exceções e códigos HTTP | Seção 8.4 |
| Chamadas por nome de serviço (sem URL fixa) | Seção 8.8 (endereço resolvido em tempo de execução) |

---

## 9. Banco de dados e migrations

Cada serviço tem sua própria migration em `src/main/resources/db/migration`:

| Serviço | Arquivo | Conteúdo |
|---|---|---|
| ms-inventario | `V1__criar_tabela_lote.sql` | Estrutura da tabela de lotes |
| ms-inventario | `V2__carga_inicial_lotes.sql` | Carga inicial de lotes |
| ms-ofertas | `V1__criar_tabela_ofertas.sql` | Estrutura da tabela de ofertas |
| ms-ofertas | `V2__carga_inicial_ofertas.sql` | Carga inicial de ofertas |

O Hibernate não cria tabelas (`ddl-auto=none`): o schema é gerenciado somente pelo Flyway.

Credenciais padrão (definidas no `docker-compose.yml` e nos `application.properties`):

| | ms-inventario | ms-ofertas |
|---|---|---|
| Host/porta | `localhost:3307` | `localhost:3308` |
| Banco | `greenstock_inventario` | `greenstock_ofertas` |
| Usuário | `greenstock` | `greenstock` |
| Senha | `greenstock` | `greenstock` |

Para usar outro banco sem alterar código, defina as variáveis de ambiente `DB_URL`, `DB_USER`, `DB_PASSWORD` e `EUREKA_URL`.

---

## 10. Problemas comuns

| Sintoma | Causa provável e solução |
|---|---|
| `Connection refused` para `localhost:8761` no log | O Eureka não está rodando. Suba o `eureka-server` primeiro. |
| `Communications link failure` / erro ao conectar no MySQL | Os containers ainda não terminaram de iniciar. Use `docker compose up -d --wait` e confira com `docker compose ps`. |
| `Port 3307 (ou 3308) is already allocated` | Outro processo usa a porta. Encerre-o ou ajuste a porta no `docker-compose.yml` e no `application.properties`. |
| `503` logo na primeira chamada | O serviço de destino ainda não se registrou no Eureka. Aguarde até 30 segundos e confira o painel. |
| `503` mesmo com os dois serviços no ar | O circuito pode estar aberto após falhas anteriores. Aguarde 10 segundos e tente de novo. |
| Cenários de teste dando resultado diferente do esperado | O estado dos dados mudou em testes anteriores. Reinicie os bancos com `docker compose down -v` (seção 8.2). |
| `Failed to delete ... target` ao rodar `clean` | Pare os serviços em execução e mantenha o projeto fora de pastas sincronizadas, como o OneDrive. |
| `java: release version 25 not supported` | O JDK ativo não é o 25. Ajuste o `JAVA_HOME`. |