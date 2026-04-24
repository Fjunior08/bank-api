# 💳 Bank API - Backend Financeiro (Java + Spring Boot)

API REST para gerenciamento de contas bancárias com suporte a transações financeiras, concorrência, idempotência e testes completos.

---

## Tecnologias

* Java 21
* Spring Boot
* Spring Data JPA
* PostgreSQL
* Docker & Docker Compose
* JUnit 5 + Mockito
* Maven

---

## Funcionalidades

* Criar conta
* Depositar valor
* Sacar valor
* Transferir entre contas
* Consultar extrato
* Controle de concorrência (lock pessimista)
* Idempotência (evita duplicação de requisições)
* Logging estruturado
* Testes unitários, integração e concorrência

---

## Arquitetura

```
controller → service → repository → database
```

* **Controller**: entrada da API (REST)
* **Service**: regras de negócio
* **Repository**: acesso ao banco
* **DTOs**: isolamento da camada de API
* **Entity**: modelo de dados

---

## Padrão de Resposta

Todas as respostas seguem o padrão:

```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2026-04-24T17:00:00"
}
```

---

## Como rodar com Docker

### 1. Build do projeto

```bash
mvn clean package
```

### 2. Subir aplicação + banco

```bash
docker compose up --build
```

---

## Acesso

* API: http://localhost:8080

---

## Endpoints principais

### Criar conta

```
POST /accounts
```

```json
{
  "owner": "Franciss"
}
```

---

### Depositar

```
POST /accounts/{id}/deposit?amount=100
```

Header opcional:

```
Idempotency-Key: abc123
```

---

### Sacar

```
POST /accounts/{id}/withdraw?amount=50
```

---

### Transferir

```
POST /accounts/transfer
```

```json
{
  "from": 1,
  "to": 2,
  "amount": 50
}
```

---

### Extrato

```
GET /accounts/{id}/statement
```

---

## Concorrência

* Uso de **lock pessimista (FOR UPDATE)**
* Evita inconsistência de saldo em operações simultâneas

---

## Idempotência

* Implementada via header `Idempotency-Key`
* Evita duplicação de transações (ex: retry de requisição)

---

## Testes

* ✔ Unitários (Service)
* ✔ Integração (MockMvc)
* ✔ Concorrência (multithread)

Executar:

```bash
mvn test
```

---

## Logging

Exemplo:

```
Depósito realizado accountId=1 amount=100
Saque realizado accountId=1 amount=50
Transferência realizada from=1 to=2 amount=30
```

---

## Estrutura do Projeto

```
com.bank.api
├── controller
├── service
├── repository
├── entity
├── dto
├── exception
```

---

## Diferenciais

* Arquitetura limpa e organizada
* Tratamento global de exceções
* API padronizada
* Testes cobrindo cenários reais
* Preparado para produção (Docker)

---

## Autor

**Franciss**
Full Stack Developer | Backend & DevOps

---

## Observações

Este projeto foi desenvolvido com foco em boas práticas de backend e simulação de cenários reais de sistemas financeiros.

---
