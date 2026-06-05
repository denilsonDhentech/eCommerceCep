# 🚚 Logistics Route Validator

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.14-brightgreen.svg)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.0.2-blue.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)
![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)

## 📖 Sobre o Projeto
Uma aplicação robusta de validação de rotas logísticas para e-commerce. A API recebe um CEP de destino, consulta uma malha logística externa (simulada via WireMock) para verificar restrições de entrega e retorna os dados formatados.

Para fins de auditoria e análise de malha, **todas as consultas são registradas em um banco de dados relacional de forma assíncrona**, garantindo zero impacto na latência da resposta para o cliente final.

## 🎯 Diferenciais Arquiteturais e Padrões Aplicados
- **Clean Architecture & SOLID:** Separação estrita de responsabilidades entre as camadas de `api`, `domain` e `infrastructure`.
- **Resiliência (Circuit Breaker):** Implementação de Fallback utilizando **Resilience4j**. Caso a API externa falhe ou fique indisponível, o sistema não quebra (evitando erro 500) e retorna um status de contingência.
- **Alta Performance (Async Logging):** O salvamento dos logs no banco de dados é feito em background utilizando `Spring Application Events` e `@Async`.
- **Tratamento de Erros Padronizado:** Validação rigorosa de inputs com respostas baseadas na RFC 7807 (Problem Details).
- **Cloud Native & Mensageria (AWS SQS):** Evolução do processamento assíncrono para uma arquitetura baseada em eventos escalável. Utilização do **LocalStack** para simular o ambiente AWS localmente, publicando e consumindo logs de consulta via fila SQS (`consultation-log-queue`).

## 🛠️ Tecnologias Utilizadas
- **Linguagem:** Java 17
- **Framework Core:** Spring Boot 3.5.14
- **Integração Externa:** Spring Cloud OpenFeign
- **Resiliência:** Spring Cloud Circuit Breaker (Resilience4j)
- **Persistência:** Spring Data JPA + Hibernate
- **Banco de Dados:** PostgreSQL (Produção/Local) e H2 Database (Testes)
- **Testes:** JUnit 5, Mockito, Spring Cloud Contract WireMock
- **Infraestrutura:** Docker & Docker Compose

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- Docker e Docker Compose instalados.
- Java 17 (JDK) e Maven instalados.
- *Portas `8080` (WireMock), `8081` (App) e `5432` (Postgres) disponíveis.*

### Passo a Passo

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/seu-usuario/eCommerceCep.git
   cd eCommerceCep
   ```

2. **Suba a infraestrutura via Docker:**
   Isso iniciará o banco de dados PostgreSQL e o servidor WireMock contendo os stubs da API externa.
   ```bash
   docker-compose up -d
   ```

3. **Inicie a aplicação Spring Boot:**
   ```bash
   mvn spring-boot:run
   ```

---

## 🧪 Como Testar a API

A aplicação estará rodando em `http://localhost:8081`.

### 1. Consulta com Sucesso (Caminho Feliz)
**Requisição:**
```bash
curl -X GET http://localhost:8081/api/v1/deliveries/routes/01001000
```

**Resposta (200 OK):**
```json
{
  "zipCode": "01001-000",
  "street": "Praça da Sé",
  "neighborhood": "Sé",
  "city": "São Paulo",
  "state": "SP",
  "deliveryStatus": "Available"
}
```

### 1.1 Simulação de Cenários (WireMock Stubs)
Para validar o comportamento do sistema sem depender da estabilidade da API real da ViaCep, utilizamos o **WireMock**. Você pode injetar cenários de erro via API administrativa (porta `8080`) através do Insomnia ou Postman:

**Exemplo: Simular Erro 500 (API Instável):**
1. Crie um **POST** para: `http://localhost:8080/__admin/mappings`
2. No corpo (**Body**), envie este JSON:
```json
{
  "request": { "method": "GET", "urlPathPattern": "/ws/.*" },
  "response": { "status": 500 }
}
```
*Após o envio, a próxima consulta disparará o Circuit Breaker e retornará o Fallback de contingência.*

**Para restaurar o funcionamento normal:**
1. Crie um **POST** para: `http://localhost:8080/__admin/mappings/reset`
2. Envie com o corpo vazio.

### 1.2 Dica: Stub Genérico (Opcional)
Se desejar que o WireMock sempre responda com dados simulados para qualquer CEP, sem precisar configurar cada um individualmente, você pode adicionar este arquivo em `wiremock/mappings/stub-generico.json`:

**(arquivo de exemplo se encontra em stubs-library/stub-generico.json)**
```json
{
   "request": { "method": "GET", "urlPathPattern": "/ws/.*" },
   "response": {
      "status": 200,
      "transformers": ["response-template"],
      "headers": { "Content-Type": "application/json" },
      "body": "{\"cep\": \"{{request.pathSegments.[1]}}\", \"logradouro\": \"Rua do Teste Automatizado\", \"bairro\": \"Bairro Teste\", \"localidade\": \"São Paulo\", \"uf\": \"SP\"}"
   }
}
```

### 2. Validação de Input (Bad Request)
Tentar enviar um CEP com formato inválido (ex: com letras ou tamanho incorreto).

**Requisição:**
```bash
curl -X GET http://localhost:8081/api/v1/deliveries/routes/01311200
```

**Resposta (400 Bad Request - RFC 7807):**
```json
{
  "type": "about:blank",
  "title": "Invalid Input Parameter",
  "status": 400,
  "detail": "getRouteDetails.zipCode: Zip code must contain exactly 8 digits",
  "instance": "/api/v1/deliveries/routes/12345"
}
```

### 3. Testando a Resiliência (Fallback)
Para testar o Circuit Breaker, pare o container do WireMock simulando uma queda da API externa:
```bash
docker stop wiremock_svc
```

Ao realizar a consulta novamente, a aplicação não retornará erro, mas sim o status de contingência:

**Resposta (200 OK - Contingência):**
```json
{
  "zipCode": "01001000",
  "street": "Indisponível",
  "neighborhood": "Indisponível",
  "city": "Indisponível",
  "state": "Indisponível",
  "deliveryStatus": "Unavailable - API Externa Offline"
}
```

## ☁️ Simulação de Nuvem AWS (LocalStack)

Para demonstrar proficiência em arquiteturas orientadas a eventos para cenários de altíssimo volume, este projeto utiliza o **LocalStack** para emular o serviço **AWS SQS (Simple Queue Service)** localmente na porta `4566`.

### Como a Mensageria Funciona:
1. Ao consultar um CEP válido, o `RouteValidationUseCase` envia instantaneamente um payload JSON para a fila SQS.
2. A biblioteca `Spring Cloud AWS (awspring)` gerencia a conexão. Caso a fila não exista na inicialização, ela é provisionada automaticamente.
3. O `ConsultationSqsListener` escuta a fila em background. Ao detectar a mensagem, realiza o *dequeue* e a persiste de forma segura no PostgreSQL.

### Comprovando o Fluxo de Eventos:
Ao realizar uma requisição via Insomnia ou `curl`, observe o console da aplicação (Spring Boot). Você verá o exato momento em que o "Worker" da AWS captura e processa a mensagem de forma desacoplada:

```text
INFO  ... : Received message from SQS Queue for Zip Code: 01001000
INFO  ... : Log successfully saved to the database via SQS worker.
```

Para visualizar a atividade interna da AWS (Criação da Fila, Envio e Deleção da Mensagem), você pode checar os logs do container do LocalStack executando:
```bash
docker logs localstack_svc
```
---

## ⚙️ Cobertura de Testes Automatizados
O projeto conta com testes unitários e de integração validando os cenários de banco de dados, regras de negócio e chamadas HTTP mockadas via WireMock integrado.

Para rodar a suíte de testes:
```bash
mvn clean test
```

---
**Autor:** Denilson Souza