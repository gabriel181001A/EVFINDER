# EVFINDER

Para rodar o back-end pelo codespace utilize os seguites comandos: 

cd EVFINDER.back
docker run --name evfinder-postgres -e POSTGRES_DB=evfinder_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres
./mvnw spring-boot:run

para testar os endpoints abra um novo terminal BASH e utilize comandos curl.
Ex:  "curl -X POST http://localhost:8080/api/v1/stations \
-H "Content-Type: application/json" \
-d '{
  "name": "Supercharger Central",
  "latitude": -22.9068,
  "longitude": -47.0616,
  "connectorType": "Type 2",
  "powerKw": 150
}'"

==============================================

🔌 Documentação da API (Endpoints)
A API do EVFinder está estruturada de forma RESTful sob o prefixo /api/v1. Abaixo estão os endpoints disponíveis e os respetivos formatos de comunicação.

📍 Estações (Stations)
Gerir postos de carregamento locais e pesquisar postos reais via Open Charge Map.

POST /api/v1/stations

Descrição: Regista um novo posto de carregamento na base de dados local.

Corpo do Pedido (JSON):

JSON
{
  "name": "Supercharger Central",
  "latitude": -22.9068,
  "longitude": -47.0616,
  "connectorType": "Type 2",
  "powerKw": 150
}
GET /api/v1/stations

Descrição: Lista todos os postos guardados na base de dados local.

GET /api/v1/stations/{id}

Descrição: Obtém os detalhes de um posto específico pelo seu ID.

GET /api/v1/stations/search-live

Descrição: Pesquisa postos reais na API do Open Charge Map com base nas coordenadas fornecidas.

Parâmetros da Query:

lat (Obrigatório): Latitude (ex: -21.4223).

lng (Obrigatório): Longitude (ex: -42.4231).

distance (Opcional): Raio de pesquisa em km (padrão: 10).

🚗 Veículos (Vehicles)
Gerir os veículos dos utilizadores e associá-los às carteiras Solana.

POST /api/v1/vehicles

Descrição: Regista um novo veículo para um utilizador.

Corpo do Pedido (JSON):

JSON
{
  "make": "BYD",
  "model": "Dolphin",
  "batteryCapacityKwh": 44,
  "connectorType": "Type 2",
  "ownerWalletAddress": "A1B2C3D4E5"
}
GET /api/v1/vehicles

Descrição: Lista todos os veículos registados no sistema.

GET /api/v1/vehicles/wallet/{walletAddress}

Descrição: Devolve a lista de veículos associados a uma carteira específica da blockchain Solana.

🧠 Recomendações (Recommendations)
O motor de cruzamento de dados entre a garagem do utilizador e o mundo real.

GET /api/v1/recommendations

Descrição: Recomenda postos de carregamento reais na zona do utilizador, filtrando automaticamente para mostrar apenas as estações que possuem conectores compatíveis com a ficha do veículo especificado.

Parâmetros da Query:

vehicleId (Obrigatório): ID do veículo na base de dados local.

lat (Obrigatório): Latitude atual.

lng (Obrigatório): Longitude atual.

radiusKm (Opcional): Raio de pesquisa em km (padrão: 15).

💰 Recompensas (Rewards / Charge-to-Earn)
Integração com a Web3 para recompensar utilizadores pela utilização da plataforma.

POST /api/v1/rewards/check-in

Descrição: Processa o check-in num posto e emite tokens de recompensa para a carteira do utilizador (atualmente a simular o hash de transação da rede Solana).

Corpo do Pedido (JSON):

JSON
{
  "stationId": 123,
  "userWalletAddress": "A1B2C3D4E5"
}