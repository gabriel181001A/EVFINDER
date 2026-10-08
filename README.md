# ⚡ EVFinder Backend

> Backend RESTful da plataforma **EVFinder**, desenvolvida para localização, gerenciamento e recomendação de estações de carregamento para veículos elétricos.

O serviço fornece uma API RESTful construída com **Spring Boot**, integrada a **PostgreSQL**, **Open Charge Map** e funcionalidades relacionadas à **Web3/Solana**.

---

## 🚀 Tecnologias

[![CI](https://github.com/gabriel181001A/EVFINDER/actions/workflows/ci.yml/badge.svg)](https://github.com/gabriel181001A/EVFINDER/actions/workflows/ci.yml)

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Container-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Solana](https://img.shields.io/badge/Solana-Web3-9945FF?style=for-the-badge&logo=solana&logoColor=white)

---

## 📋 Sobre o projeto

O **EVFinder** tem como objetivo facilitar a localização e descoberta de pontos de carregamento para veículos elétricos.

O backend é responsável por:

- 🔌 Cadastro de estações de carregamento
- 📍 Consulta de estações por localização
- 🌎 Integração com a API do Open Charge Map
- 🚗 Gerenciamento de veículos
- 🔋 Compatibilidade entre veículos e conectores
- 🧠 Sistema de recomendações
- 👛 Associação de veículos a carteiras Solana
- 🪙 Sistema de recompensas baseado em check-in
- ⚡ Integração com funcionalidades Web3

---

# 🏗️ Arquitetura

```text
                         ┌─────────────────────┐
                         │      Frontend       │
                         │      EVFinder       │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │     REST API        │
                         │    Spring Boot      │
                         └──────────┬──────────┘
                                    │
                  ┌─────────────────┼─────────────────┐
                  │                 │                 │
                  ▼                 ▼                 ▼
           ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
           │ PostgreSQL  │   │ Open Charge │   │   Solana    │
           │   Database  │   │     Map     │   │    Web3     │
           └─────────────┘   └─────────────┘   └─────────────┘
```

---

# 💻 Como executar

## Pré-requisitos

Antes de iniciar o projeto, certifique-se de possuir:

- Java 21
- Docker com Docker Compose (já incluso no Docker Desktop; no Windows, o Docker Desktop exige o WSL 2)
- Git
- Maven Wrapper incluso no projeto

---

## 📦 1. Acesse o backend

```bash
cd EVFINDER.back
```

---

## 🐘 2. Inicie o PostgreSQL

O banco está definido no [`docker-compose.yml`](EVFINDER.back/docker-compose.yml), com as mesmas credenciais do `application.properties`:

```bash
docker compose up -d
```

Verifique se o container está rodando e saudável (`healthy`):

```bash
docker compose ps
```

Os dados ficam no volume `evfinder-postgres-data` e sobrevivem a reinícios. Comandos úteis:

| Comando | O que faz |
|---|---|
| `docker compose stop` | Para o banco, mantendo os dados |
| `docker compose down` | Remove o container, mantendo os dados |
| `docker compose down -v` | Remove o container **e apaga todos os dados** |

> Se você já tinha criado o container `evfinder-postgres` com `docker run`, remova-o antes com `docker rm -f evfinder-postgres`.

> **Já tem um PostgreSQL instalado no computador usando a porta 5432?** Defina `DB_PORT=5433` no `.env` (veja o passo 3) **antes** do `docker compose up -d`. O Docker Compose e a aplicação leem a mesma variável, então os dois passam a usar a porta 5433. Sem isso, a aplicação conecta no PostgreSQL errado e falha com erro de autenticação.

---

## 🔑 3. Configure a chave do Open Charge Map

A busca de estações reais usa a API do [Open Charge Map](https://openchargemap.org/site/develop/api), que exige uma chave.
A chave **não fica no código**: ela é lida da variável de ambiente `OCM_API_KEY` ou de um arquivo `.env` dentro de `EVFINDER.back` (o `.env` está no `.gitignore`).

```bash
cp .env.example .env
```

Depois, edite o `.env` e preencha a sua chave:

```text
OCM_API_KEY=sua-chave-aqui
```

Sem a chave, a aplicação não inicia.

---

## ▶️ 4. Execute a aplicação

No Linux/macOS:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

Por padrão, a API estará disponível em:

```text
http://localhost:8080
```

---

# 🧪 Testando a API

Você pode utilizar ferramentas como:

- cURL
- Postman
- Insomnia
- Bruno
- Swagger, caso configurado no projeto

---

## 📍 Criar uma estação

### Request

```bash
curl -X POST http://localhost:8080/api/v1/stations \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Supercharger Central",
    "latitude": -22.9068,
    "longitude": -47.0616,
    "connectorType": "Type 2",
    "powerKw": 150
  }'
```

### Body

```json
{
  "name": "Supercharger Central",
  "latitude": -22.9068,
  "longitude": -47.0616,
  "connectorType": "Type 2",
  "powerKw": 150
}
```

---

# 📚 Documentação da API

Todas as rotas seguem o padrão RESTful e utilizam o prefixo:

```text
/api/v1
```

## Formato de erro

Todos os erros retornam o mesmo formato JSON:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Campos inválidos: latitude é obrigatório; name é obrigatório",
  "timestamp": "2026-10-08T14:30:00"
}
```

| Status | Quando acontece |
|---|---|
| `400` | Campo obrigatório ausente ou inválido, parâmetro com tipo errado, JSON malformado |
| `404` | Recurso não encontrado (ex.: estação ou veículo com ID inexistente) ou rota inexistente |
| `500` | Erro inesperado no servidor (registrado no log) |

---

# 🔌 Stations

Gerenciamento dos postos de carregamento.

Também permite consultar estações reais utilizando a API do **Open Charge Map**.

### Criar estação

```http
POST /api/v1/stations
```

Cria uma nova estação na base de dados local.

#### Request

```json
{
  "name": "Supercharger Central",
  "latitude": -22.9068,
  "longitude": -47.0616,
  "connectorType": "Type 2",
  "powerKw": 150
}
```

#### Validações

| Campo | Regra |
|---|---|
| `name` | Obrigatório |
| `latitude` | Obrigatório, entre -90 e 90 |
| `longitude` | Obrigatório, entre -180 e 180 |
| `connectorType` | Opcional |
| `powerKw` | Opcional, maior que zero |

---

### Listar estações

```http
GET /api/v1/stations
```

Retorna todas as estações cadastradas localmente.

---

### Buscar estação por ID

```http
GET /api/v1/stations/{id}
```

Retorna os detalhes de uma estação específica.

#### Exemplo

```http
GET /api/v1/stations/1
```

---

### Buscar estações em tempo real

```http
GET /api/v1/stations/search-live
```

Consulta estações reais utilizando o **Open Charge Map**.

#### Query Parameters

| Parâmetro | Obrigatório | Descrição | Exemplo |
|---|---|---|---|
| `lat` | ✅ | Latitude | `-22.9068` |
| `lng` | ✅ | Longitude | `-47.0616` |
| `distance` | ❌ | Raio de busca em km | `10` |

#### Exemplo

```http
GET /api/v1/stations/search-live?lat=-22.9068&lng=-47.0616&distance=10
```

---

# 🚗 Vehicles

Gerenciamento dos veículos dos usuários.

Os veículos podem ser associados a carteiras da blockchain **Solana**.

---

### Criar veículo

```http
POST /api/v1/vehicles
```

Registra um novo veículo.

#### Request

```json
{
  "make": "BYD",
  "model": "Dolphin",
  "batteryCapacityKwh": 44,
  "connectorType": "Type 2",
  "ownerWalletAddress": "A1B2C3D4E5"
}
```

#### Validações

| Campo | Regra |
|---|---|
| `make` | Obrigatório |
| `model` | Obrigatório |
| `connectorType` | Obrigatório (usado para filtrar as estações compatíveis na recomendação) |
| `batteryCapacityKwh` | Opcional, maior que zero |
| `ownerWalletAddress` | Opcional |

---

### Listar veículos

```http
GET /api/v1/vehicles
```

Retorna todos os veículos cadastrados.

---

### Buscar veículos por carteira

```http
GET /api/v1/vehicles/wallet/{walletAddress}
```

Retorna todos os veículos associados a uma carteira Solana.

#### Exemplo

```http
GET /api/v1/vehicles/wallet/A1B2C3D4E5
```

---

# 🧠 Recommendations

O módulo de recomendações realiza o cruzamento entre:

```text
Veículo do usuário
        +
Localização atual
        +
Estações disponíveis
        +
Tipo de conector
        ↓
Recomendações compatíveis
```

---

### Buscar recomendações

```http
GET /api/v1/recommendations
```

Retorna estações reais próximas ao usuário que sejam compatíveis com o veículo selecionado.

#### Query Parameters

| Parâmetro | Obrigatório | Descrição | Exemplo |
|---|---|---|---|
| `vehicleId` | ✅ | ID do veículo | `1` |
| `lat` | ✅ | Latitude atual | `-22.9068` |
| `lng` | ✅ | Longitude atual | `-47.0616` |
| `radiusKm` | ❌ | Raio de busca em km | `15` |

#### Exemplo

```http
GET /api/v1/recommendations?vehicleId=1&lat=-22.9068&lng=-47.0616&radiusKm=15
```

---

# 🪙 Rewards — Charge-to-Earn

Módulo responsável pelo sistema de recompensas do EVFinder.

A funcionalidade utiliza o conceito de **Charge-to-Earn**, permitindo recompensar usuários por interações com estações de carregamento.

> Atualmente, a emissão da recompensa utiliza um hash de transação simulado para representar a integração com a rede Solana.

---

### Check-in em uma estação

```http
POST /api/v1/rewards/check-in
```

Processa o check-in do usuário em uma estação.

#### Request

```json
{
  "stationId": 123,
  "userWalletAddress": "A1B2C3D4E5"
}
```

#### Validações

`stationId` e `userWalletAddress` são obrigatórios.

---

# 🔄 Fluxo principal

```text
                    ┌───────────────┐
                    │    Usuário    │
                    └───────┬───────┘
                            │
                            ▼
                    ┌───────────────┐
                    │ Cadastra carro│
                    └───────┬───────┘
                            │
                            ▼
                    ┌───────────────┐
                    │ Localização   │
                    │     atual     │
                    └───────┬───────┘
                            │
                            ▼
                  ┌───────────────────┐
                  │ Recommendation    │
                  │      Engine       │
                  └─────────┬─────────┘
                            │
                            ▼
                  ┌───────────────────┐
                  │ Open Charge Map   │
                  └─────────┬─────────┘
                            │
                            ▼
                  ┌───────────────────┐
                  │ Estações          │
                  │ compatíveis       │
                  └─────────┬─────────┘
                            │
                            ▼
                  ┌───────────────────┐
                  │      Check-in     │
                  └─────────┬─────────┘
                            │
                            ▼
                  ┌───────────────────┐
                  │   Reward / Token  │
                  │      Solana       │
                  └───────────────────┘
```

---

# 🗂️ Estrutura dos principais recursos

```text
EVFINDER/
├── .github/workflows/ci.yml        # CI: build e testes a cada PR
├── README.md
└── EVFINDER.back/
    ├── src/
    │   ├── main/
    │   │   ├── java/com/evfinder/
    │   │   │   ├── controller/     # Rotas REST
    │   │   │   ├── service/        # Regras de negócio (ex.: recomendação)
    │   │   │   ├── repository/     # Acesso ao banco (Spring Data JPA)
    │   │   │   ├── entity/         # Tabelas: Station e Vehicle
    │   │   │   ├── dto/            # Entrada e saída da API, com as validações
    │   │   │   ├── integration/    # Cliente da API do Open Charge Map
    │   │   │   ├── blockchain/     # Recompensas Solana (simuladas por enquanto)
    │   │   │   ├── exception/      # Tratamento global de erros
    │   │   │   └── config/         # CORS
    │   │   └── resources/
    │   │       └── application.properties
    │   └── test/                   # Testes dos controllers
    ├── .env.example                # Modelo das variáveis de ambiente (copie para .env)
    ├── docker-compose.yml          # PostgreSQL local
    ├── mvnw / mvnw.cmd             # Maven Wrapper
    └── pom.xml
```

---

# 🌐 Integrações

## Open Charge Map

Utilizado para consultar estações de carregamento reais próximas às coordenadas fornecidas pelo usuário.

```text
EVFinder
    │
    ▼
Recommendation Engine
    │
    ▼
Open Charge Map API
    │
    ▼
Estações disponíveis
```

---

## Solana / Web3

O projeto possui integração conceitual com a blockchain Solana para:

- Identificação de usuários através de wallet
- Associação de veículos às wallets
- Recompensas
- Charge-to-Earn
- Futuras transações on-chain

---

# 🛠️ Desenvolvimento

Para contribuir com o projeto:

### 1. Clone o repositório

```bash
git clone <URL_DO_REPOSITORIO>
```

### 2. Entre no diretório

```bash
cd EVFINDER.back
```

### 3. Inicie o banco

```bash
docker compose up -d
```

### 4. Configure o `.env`

Copie o `.env.example` para `.env` e preencha a `OCM_API_KEY`, como explicado em [Configure a chave do Open Charge Map](#-3-configure-a-chave-do-open-charge-map).

### 5. Execute a aplicação

```bash
./mvnw spring-boot:run
```

### 6. Rode os testes

Os testes não precisam de banco de dados nem da chave do Open Charge Map:

```bash
./mvnw test
```

Eles também rodam automaticamente no GitHub Actions ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)) a cada pull request e a cada push na `main`.

---

# 📌 Status do projeto

🚧 **Em desenvolvimento**

Funcionalidades atualmente disponíveis:

- [x] CRUD de estações
- [x] Consulta de estações
- [x] Integração com Open Charge Map
- [x] Cadastro de veículos
- [x] Associação com wallet Solana
- [x] Sistema de recomendações
- [x] Check-in
- [x] Estrutura de recompensas
- [x] Validação dos dados de entrada
- [ ] Integração completa com blockchain
- [ ] Emissão real de tokens
- [ ] Autenticação e autorização
- [ ] Testes automatizados completos

---

# 👨‍💻 Projeto

**EVFinder**

Sistema desenvolvido para facilitar a localização de estações de carregamento e criar uma experiência integrada entre **mobilidade elétrica, geolocalização e Web3**.

---

<p align="center">
  Desenvolvido com ⚡ para o futuro da mobilidade elétrica.
</p>
