![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?style=flat-square&logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Local-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![H2 Database](https://img.shields.io/badge/H2-Tests-blue?style=flat-square&logo=sqlite&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-UI-005F0F?style=flat-square&logo=thymeleaf&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit-5-25A162?style=flat-square&logo=junit5&logoColor=white)

# Spring Payment

Feito por Daniel Landuche

Aplicação web de transferências entre contas, com cadastro de clientes, login, pagamento via Pix ou limite de crédito e extrato. Projeto do bootcamp Itaú - Java com Inteligência Artificial da DIO, desafio
Design Patterns com Java: Dos Clássicos (GoF) ao Spring Framework.

## Stack
- **Backend:** Java 21, Spring Boot 4.1 (Spring MVC, Spring Data JPA, Validation)
- **Frontend:** Thymeleaf, Bootstrap 5.3, Bootstrap Icons
- **Banco:** PostgreSQL (execução), H2 em memória (testes)
- **Segurança:** BCrypt (`spring-security-crypto`) para o hash de senha
- **Testes:** JUnit 5, Mockito, Spring Boot Test

## Padrões de projeto
- **Strategy:** cada meio de pagamento é uma implementação de `PaymentStrategy`. `PixPaymentStrategy` debita do saldo e `CreditCardPaymentStrategy` debita do limite de crédito.
- **Factory:** `PaymentStrategyFactory` entrega a strategy certa para o meio de pagamento escolhido.
- **Observer:** após um pagamento aprovado, o `PaymentService` publica um `PaymentProcessedEvent`, e o `AuditAndNotificationListener` registra a auditoria.

## Estrutura do Repositório

```text
src/main/java/com/landuche/payment
  ├─ config/          # Bean do PasswordEncoder (BCrypt)
  ├─ controller/      # Rotas web e tratamento global de erros
  ├─ domain/          # Entidades JPA (Customer, PaymentTransaction)
  ├─ dto/             # PaymentResponse e SessionUser (dados guardados na sessão)
  ├─ enums/           # PaymentMethod e TransactionStatus
  ├─ factory/         # PaymentStrategyFactory
  ├─ observer/        # Evento de pagamento e listener de auditoria
  ├─ repository/      # Repositórios Spring Data
  ├─ service/         # Regras de negócio (PaymentService, CustomerService)
  └─ strategy/        # Estratégias de pagamento (Pix e cartão)
src/main/resources
  ├─ templates/       # Páginas Thymeleaf
  └─ application.properties
src/test/java         # Testes do serviço de pagamento e carregamento do contexto
src/test/resources    # Configuração de testes com H2
```

## Como Executar

### 1. Banco de dados
Crie um banco PostgreSQL local chamado `payment_db` (porta `5432`, usuário `postgres`). As tabelas são criadas automaticamente na primeira execução.

### 2. Variável de ambiente
A senha do banco é definida via variavel `DB_PASSWORD`.

```bash
# PowerShell
$env:DB_PASSWORD="sua_senha"

# Git Bash / Linux / macOS
export DB_PASSWORD="sua_senha"
```

### 3. Rodar a aplicação

```bash
./mvnw spring-boot:run
```

- Aplicação disponível em: `http://localhost:8080`
- Ao criar um cliente, a conta começa com saldo de `R$ 1.000,00` e limite de crédito de `R$ 2.000,00`.

### 4. Testes

O projeto conta com testes unitários desenvolvida com **JUnit 5** e **Mockito**:

- **Prevenção de Valores Inválidos:** Validação contra valores negativos, zero ou com mais de 2 casas decimais.
- **Isolamento:** Testes independentes validando o comportamento do Pix e do Limite de Crédito.
- **Consistência de Domínio:** Tratamento de destinatários e verificação de lançamento de exceções.
- **Eventos Assíncronos:** Confirmação de que os eventos Observer são devidamente disparados após o sucesso das operações.
- **Execução em Memória:** Uso do H2 para testes autossuficientes, sem necessidade de configuração de banco ou variáveis de ambiente.

```bash
./mvnw test
```

## Rotas

| Método | Rota | Descrição |
|---|---|---|
| GET | `/` | Tela de login (sem sessão) ou dashboard (com sessão) |
| POST | `/web/auth/login` | Autentica por e-mail e senha |
| GET | `/web/auth/logout` | Encerra a sessão |
| GET | `/web/customers/new` | Formulário de cadastro |
| POST | `/web/customers` | Cria um cliente |
| GET | `/web/checkout` | Tela de pagamento (exige login) |
| POST | `/web/payments` | Realiza o pagamento |
| GET | `/web/history/{accountNumber}` | Extrato da conta (somente o próprio dono) |

## Regras de negócio
- O destinatário pode ser informado pelo **número da conta** ou pelo **e-mail**.
- O valor precisa ser maior que zero e ter no máximo 2 casas decimais.
- Não é possível transferir para a própria conta.
- **Pix** debita do saldo. **Limite de crédito** debita do limite. Em ambos os casos, o destinatário recebe o valor no saldo.
- No extrato, o favorecido aparece com a chave usada na transferência (e-mail ou número da conta).
- Duas operações simultâneas na mesma conta não podem gastar o mesmo saldo: a segunda recebe a mensagem para tentar novamente (controle otimista com `@Version`).

## Exemplo de Registro de Auditoria

Cada pagamento aprovado gera uma linha no log, emitida pelo `AuditAndNotificationListener`:

```text
Pagamento aprovado | transação=TX-1A2B3C4D | valor=150.00 | pagador=3f2c9a10-...
```
