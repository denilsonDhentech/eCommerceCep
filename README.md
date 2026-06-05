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

### 2. Validação de Input (Bad Request)
Tentar enviar um CEP com formato inválido (ex: com letras ou tamanho incorreto).

**Requisição:**
```bash
curl -X GET http://localhost:8081/api/v1/deliveries/routes/12345
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

---

## ⚙️ Cobertura de Testes Automatizados
O projeto conta com testes unitários e de integração validando os cenários de banco de dados, regras de negócio e chamadas HTTP mockadas via WireMock integrado.

Para rodar a suíte de testes:
```bash
mvn clean test
```

---
**Autor:** Denilson Souza