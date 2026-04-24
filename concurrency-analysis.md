# 🧩 Análise Técnica – Identificação e Correção de Erros

## Contexto do Problema

O código apresentado simula uma operação de transferência entre contas bancárias em um contexto transacional.
A partir do log fornecido, é necessário identificar falhas na implementação e propor correções alinhadas a boas práticas de sistemas transacionais.

---

## Código fornecido

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    public void transfer(Long fromAccountId, Long toAccountId, double amount) {

        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + fromAccountId));

        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + toAccountId));

        if (fromAccount.getBalance() < amount) {
            throw new RuntimeException("Insufficient funds in account: " + fromAccountId);
        }

        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }
}
```

---

## Evidências (Log)

```
INFO: Starting transfer from account 1001 to account 1002 with amount 500.0  
ERROR: Insufficient funds in account: 1001  
INFO: Rolling back transaction due to error  

INFO: Starting transfer from account 1003 to account 1004 with amount 100.0  
ERROR: Could not execute statement; SQL [n/a]; constraint [null]; nested exception is  
org.hibernate.exception.ConstraintViolationException: could not execute statement
```

---

# Análise Técnica

## 1. Validação de regra de negócio

A validação de saldo insuficiente está correta e o rollback transacional ocorre conforme esperado.

Este comportamento demonstra que o boundary transacional está ativo e funcionando.

---

## 2. Falha de persistência (ConstraintViolationException)

O erro indica violação de integridade no banco de dados durante a transação.

### Possíveis causas:

* Inconsistência entre modelo de domínio e schema
* Campos obrigatórios não preenchidos
* Violação de integridade referencial
* Estado inválido sendo persistido

Esse tipo de erro evidencia ausência de validação prévia e/ou falha no mapeamento da entidade.

---

## 3. Uso inadequado de tipo numérico

```java
double amount
```

Em sistemas financeiros, `double` não é apropriado devido a:

* Perda de precisão binária
* Erros acumulativos em operações
* Divergência entre aplicação e banco

O tipo correto é `BigDecimal`.

---

## 4. Ausência de controle de concorrência

A implementação atual não considera cenários concorrentes.

### Riscos:

* Race conditions
* Lost update
* Inconsistência de saldo
* Escrita concorrente sem isolamento adequado

O uso apenas de `@Transactional` não garante isolamento suficiente dependendo do nível configurado.

---

# Proposta de Correção

## 1. Precisão numérica

Substituir:

```java
double amount
```

por:

```java
BigDecimal amount
```

---

## 2. Controle de concorrência

Aplicar locking pessimista na leitura:

```java
SELECT ... FOR UPDATE
```

ou via JPA:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

Garante exclusividade na atualização do saldo.

---

## 3. Validação de consistência

Adicionar validações antes da persistência:

* Existência das contas
* Valor positivo
* Estado válido da entidade

---

## 4. Uso de exceções de domínio

Substituir exceções genéricas por exceções de negócio:

```java
throw new BusinessException("Saldo insuficiente");
```

Melhora rastreabilidade e semântica.

---

## 5. Garantia de atomicidade

Manter `@Transactional`, assegurando:

* Commit atômico
* Rollback em caso de falha
* Consistência entre operações de débito/crédito

---

# Implementação sugerida

```java
@Transactional
public void transfer(Long fromId, Long toId, BigDecimal amount) {

    Account from = repository.findByIdForUpdate(fromId);
    Account to = repository.findByIdForUpdate(toId);

    if (from == null || to == null) {
        throw new BusinessException("Conta não encontrada");
    }

    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new BusinessException("Valor inválido");
    }

    if (from.getBalance().compareTo(amount) < 0) {
        throw new BusinessException("Saldo insuficiente");
    }

    from.setBalance(from.getBalance().subtract(amount));
    to.setBalance(to.getBalance().add(amount));

    repository.save(from);
    repository.save(to);
}
```

---

# Considerações Arquiteturais

## Transações

* Garantem propriedades ACID
* Evitam estados intermediários inconsistentes

---

## Concorrência

* Lock pessimista evita conflito de escrita
* Alternativa: optimistic locking com versionamento (`@Version`)

---

## Precisão financeira

* `BigDecimal` é obrigatório para domínio financeiro
* Evita discrepâncias entre aplicação e banco

---

## Integridade de dados

* Validação antecipada reduz falhas no commit
* Evita exceções de baixo nível (Hibernate/JDBC)

---

# Conclusão

A falha observada não está restrita à lógica de negócio, mas sim a um conjunto de problemas estruturais:

* Ausência de controle de concorrência
* Uso inadequado de tipo numérico
* Falta de validação antes da persistência
* Possível desalinhamento entre domínio e banco

A correção proposta aborda esses pontos garantindo:

* Consistência transacional
* Integridade de dados
* Segurança em cenários concorrentes
* Precisão em operações financeiras

---

Solução alinhada com boas práticas de sistemas distribuídos e aplicações financeiras.
