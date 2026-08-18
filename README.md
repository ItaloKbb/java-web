# API de Usuários com Clean Architecture

Projeto didático desenvolvido com Java e Spring Boot para demonstrar a criação de uma API REST simples utilizando princípios de **Clean Architecture**, separação de responsabilidades e inversão de dependência.

A aplicação possui, neste momento, um único caso de uso: **criar um usuário**. Apesar de pequeno, o projeto separa domínio, aplicação e infraestrutura para tornar explícito o papel de cada camada.

> Este projeto prioriza clareza para ensino. Algumas decisões foram mantidas deliberadamente simples e estão documentadas na seção [Limitações e próximos passos](#limitações-e-próximos-passos).

## Sumário

- [Objetivos do projeto](#objetivos-do-projeto)
- [Tecnologias utilizadas](#tecnologias-utilizadas)
- [Pré-requisitos](#pré-requisitos)
- [Como executar](#como-executar)
- [Como usar a API](#como-usar-a-api)
- [Validações da entrada](#validações-da-entrada)
- [Arquitetura](#arquitetura)
- [Estrutura de pacotes](#estrutura-de-pacotes)
- [Responsabilidade de cada classe](#responsabilidade-de-cada-classe)
- [Fluxo completo de uma requisição](#fluxo-completo-de-uma-requisição)
- [Regras de dependência](#regras-de-dependência)
- [Decisões arquiteturais](#decisões-arquiteturais)
- [Persistência e banco H2](#persistência-e-banco-h2)
- [Tratamento de dados e mapeamentos](#tratamento-de-dados-e-mapeamentos)
- [Testes](#testes)
- [Como depurar e explorar o projeto](#como-depurar-e-explorar-o-projeto)
- [Limitações e próximos passos](#limitações-e-próximos-passos)
- [Glossário](#glossário)

## Objetivos do projeto

O projeto procura ensinar como:

- criar uma rota REST com Spring MVC;
- validar um corpo JSON com Bean Validation;
- representar regras e dados de negócio sem depender do framework;
- definir um caso de uso por meio de uma porta de entrada;
- abstrair a persistência por meio de uma porta de saída;
- implementar a persistência com Spring Data JPA sem levar JPA para o domínio;
- converter objetos entre as camadas;
- configurar dependências usando injeção de dependência;
- testar a regra de aplicação sem iniciar Spring ou banco de dados;
- manter o sentido das dependências apontando para as regras centrais.

O objetivo principal não é apenas fazer um `POST` funcionar. É demonstrar como organizar o código para que as regras centrais permaneçam independentes de detalhes como HTTP, JSON, JPA e banco de dados.

## Tecnologias utilizadas

- **Java 17**: linguagem e versão configurada pelo Gradle Toolchain.
- **Spring Boot 4.1.0**: inicialização e configuração da aplicação.
- **Spring Web MVC**: criação do controller e da rota HTTP.
- **Jakarta Bean Validation**: validação do JSON recebido.
- **Spring Data JPA**: implementação do acesso aos dados.
- **Hibernate**: implementação JPA utilizada pelo Spring Boot.
- **H2 Database**: banco relacional em memória para desenvolvimento e ensino.
- **Gradle Wrapper**: compilação, testes e execução com uma versão controlada do Gradle.
- **JUnit 5**: testes automatizados.

O Lombok está presente nas dependências do projeto, mas as classes desta implementação usam código Java explícito. Isso torna construtores, atributos e métodos visíveis para quem está estudando.

## Pré-requisitos

É necessário ter:

- Java 17 ou uma versão compatível com o Toolchain configurado;
- terminal PowerShell, Prompt de Comando ou outro shell;
- acesso à internet na primeira execução, caso as dependências Gradle ainda não estejam no cache local.

Não é necessário instalar:

- Gradle globalmente, pois o projeto inclui o Gradle Wrapper;
- PostgreSQL, MySQL ou outro servidor de banco, pois é utilizado H2 em memória;
- servidor de aplicação externo, pois o Spring Boot possui servidor embarcado.

Para verificar o Java instalado:

```powershell
java -version
```

## Como executar

### Windows com PowerShell

Na raiz do projeto, execute:

```powershell
.\gradlew.bat bootRun
```

### Linux ou macOS

```bash
./gradlew bootRun
```

Por padrão, a aplicação ficará disponível em:

```text
http://localhost:8080
```

Para interromper a aplicação, pressione `Ctrl+C` no terminal em que ela está executando.

### Gerar o artefato executável

No Windows:

```powershell
.\gradlew.bat clean build
```

Depois, execute o arquivo JAR gerado em `build/libs`:

```powershell
java -jar build\libs\web-0.0.1-SNAPSHOT.jar
```

### Alterar a porta da aplicação

Sem editar arquivos:

```powershell
.\gradlew.bat bootRun --args="--server.port=8081"
```

Ou adicione ao arquivo `src/main/resources/application.properties`:

```properties
server.port=8081
```

## Como usar a API

### Criar usuário

Cria e persiste um novo usuário.

```http
POST /usuarios
Content-Type: application/json
```

Corpo da requisição:

```json
{
  "nome": "Maria da Silva",
  "email": "maria@exemplo.com"
}
```

Resposta esperada:

```http
HTTP/1.1 201 Created
Content-Type: application/json
```

```json
{
  "id": 1,
  "nome": "Maria da Silva",
  "email": "maria@exemplo.com"
}
```

O identificador é gerado pelo banco. Por isso ele não é enviado pelo cliente e aparece somente na resposta.

### Exemplo com curl

```bash
curl -i -X POST http://localhost:8080/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nome":"Maria da Silva","email":"maria@exemplo.com"}'
```

No PowerShell, `curl` pode ser um alias de `Invoke-WebRequest`. Para garantir o uso do executável curl:

```powershell
curl.exe -i -X POST http://localhost:8080/usuarios `
  -H "Content-Type: application/json" `
  -d '{"nome":"Maria da Silva","email":"maria@exemplo.com"}'
```

### Exemplo com Invoke-RestMethod

```powershell
$corpo = @{
    nome = "Maria da Silva"
    email = "maria@exemplo.com"
} | ConvertTo-Json

Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/usuarios" `
    -ContentType "application/json" `
    -Body $corpo
```

### Exemplo no Postman ou Insomnia

1. Crie uma requisição do tipo `POST`.
2. Informe a URL `http://localhost:8080/usuarios`.
3. Selecione o corpo do tipo JSON.
4. Envie `nome` e `email` conforme o exemplo.
5. Confirme que o status retornado é `201 Created`.

## Validações da entrada

O objeto `CriarUsuarioRequest` define as validações da fronteira HTTP:

| Campo | Regra | Exemplo válido |
|---|---|---|
| `nome` | obrigatório e não pode ser vazio | `Maria da Silva` |
| `email` | obrigatório, não vazio e com formato de e-mail | `maria@exemplo.com` |

Exemplo de requisição inválida:

```json
{
  "nome": "",
  "email": "email-invalido"
}
```

Nesse caso, o Spring retorna uma resposta `400 Bad Request`, pois o parâmetro do controller está anotado com `@Valid`.

As mensagens declaradas atualmente são:

- `O nome é obrigatório`;
- `O e-mail é obrigatório`;
- `O e-mail deve ser válido`.

O formato exato da resposta de erro ainda é o formato padrão do Spring Boot. Em uma aplicação real, normalmente seria criado um tratamento global com `@RestControllerAdvice` para produzir um contrato de erro estável e amigável.

## Arquitetura

### Visão geral

A Clean Architecture organiza o sistema de modo que as regras mais importantes não dependam de detalhes externos.

Neste projeto, o centro contém o domínio e o caso de uso. HTTP, Spring MVC, JPA e H2 ficam nas bordas como detalhes substituíveis.

```text
┌─────────────────────────────────────────────────────────────┐
│ Infraestrutura                                              │
│                                                             │
│  HTTP / Spring MVC                  JPA / H2                 │
│  UsuarioController                  UsuarioRepositoryAdapter │
│          │                                  │                │
│          ▼                                  ▼                │
│  CriarUsuarioUseCase              UsuarioRepositoryPort      │
│          │                                  ▲                │
│          └──────► CriarUsuarioService ──────┘                │
│                         │                                   │
│                         ▼                                   │
│                      Usuario                                │
│                  Domínio da aplicação                       │
└─────────────────────────────────────────────────────────────┘
```

A seta representa conhecimento ou dependência. O domínio não conhece controller, JPA, Spring ou banco. O serviço conhece apenas o domínio e as interfaces necessárias para executar o caso de uso.

### Camadas adotadas

#### Domínio

Contém os conceitos centrais do negócio.

Neste projeto, o domínio é representado por `Usuario`. Essa classe é Java puro e não possui anotações de JPA, Spring ou serialização JSON.

#### Aplicação

Contém os casos de uso e as portas que definem como o núcleo conversa com o exterior.

A aplicação responde à pergunta: **o que o sistema permite fazer?** Neste caso, permite criar um usuário.

#### Infraestrutura

Contém detalhes técnicos e adaptadores:

- entrada HTTP;
- request e response JSON;
- persistência JPA;
- repositório Spring Data;
- conversão entre entidade JPA e domínio.

#### Configuração

Conecta as implementações concretas às abstrações. `UsuarioConfig` é o ponto de composição do caso de uso.

## Estrutura de pacotes

```text
br.senai.aula.web
│
├── domain
│   └── usuario
│       └── Usuario.java
│
├── application
│   └── usuario
│       ├── port
│       │   ├── in
│       │   │   └── CriarUsuarioUseCase.java
│       │   └── out
│       │       └── UsuarioRepositoryPort.java
│       └── service
│           └── CriarUsuarioService.java
│
├── infrastructure
│   ├── persistence
│   │   └── usuario
│   │       ├── entity
│   │       │   └── UsuarioJpaEntity.java
│   │       ├── repository
│   │       │   └── UsuarioJpaRepository.java
│   │       ├── mapper
│   │       │   └── UsuarioPersistenceMapper.java
│   │       └── adapter
│   │           └── UsuarioRepositoryAdapter.java
│   └── web
│       └── usuario
│           ├── controller
│           │   └── UsuarioController.java
│           ├── request
│           │   └── CriarUsuarioRequest.java
│           └── response
│               └── UsuarioResponse.java
│
├── config
│   └── UsuarioConfig.java
│
└── WebApplication.java
```

Essa organização é por camada e, dentro das camadas, por funcionalidade. Em sistemas maiores, outras funcionalidades podem seguir o mesmo padrão, como `produto`, `pedido` e `autenticacao`.

## Responsabilidade de cada classe

### `WebApplication`

É o ponto de entrada da aplicação Spring Boot.

`@SpringBootApplication` habilita, entre outras funcionalidades:

- configuração automática;
- descoberta de componentes dentro de `br.senai.aula.web`;
- inicialização do contexto Spring;
- inicialização do servidor web embarcado.

Essa classe não contém regra de negócio.

### `Usuario`

Representa um usuário no domínio.

Possui:

- `id`: identificador, inicialmente nulo para um usuário ainda não persistido;
- `nome`: nome do usuário;
- `email`: endereço de e-mail;
- método de fábrica `novo`, que expressa a criação de um usuário ainda sem identificador.

O domínio não é uma entidade JPA. Essa separação evita que decisões de persistência controlem o modelo de negócio.

Em um sistema mais rico, validações e comportamentos genuinamente pertencentes ao conceito de usuário poderiam ser colocados nessa classe. Exemplos: normalização de e-mail, alteração de nome e regras de estado.

### `CriarUsuarioUseCase`

É a **porta de entrada** da aplicação.

```java
Usuario criar(String nome, String email);
```

Ela define o contrato oferecido pelo núcleo: criar um usuário. O controller depende dessa interface, não da implementação concreta.

Benefícios:

- o mecanismo de entrada pode mudar sem mudar o caso de uso;
- um consumidor HTTP, uma fila ou uma interface de linha de comando poderiam utilizar o mesmo contrato;
- o controller pode ser testado com uma implementação simulada;
- a intenção da aplicação fica explícita.

### `UsuarioRepositoryPort`

É a **porta de saída** para persistência.

```java
Usuario salvar(Usuario usuario);
```

O caso de uso precisa salvar um usuário, mas não precisa saber:

- qual banco é utilizado;
- se os dados ficam em memória;
- se existe Hibernate;
- se a persistência ocorre por SQL, arquivo ou serviço externo.

Essa interface pertence à camada de aplicação porque é o núcleo que declara aquilo de que precisa.

### `CriarUsuarioService`

Implementa `CriarUsuarioUseCase` e orquestra a criação:

1. cria o objeto de domínio com `Usuario.novo`;
2. solicita a persistência pela porta `UsuarioRepositoryPort`;
3. devolve o usuário persistido.

O serviço não possui `@Service`. Isso é intencional: ele continua sendo uma classe Java sem dependência do Spring. A criação do bean ocorre em `UsuarioConfig`.

### `UsuarioJpaEntity`

Representa a tabela `usuarios` para o JPA.

Anotações principais:

- `@Entity`: informa que a classe é gerenciada pelo JPA;
- `@Table(name = "usuarios")`: define o nome da tabela;
- `@Id`: marca a chave primária;
- `@GeneratedValue(strategy = GenerationType.IDENTITY)`: delega a geração do ID ao banco.

O construtor sem argumentos é exigido pelo JPA e possui visibilidade `protected` para reduzir o uso indevido pela aplicação.

### `UsuarioJpaRepository`

Estende `JpaRepository<UsuarioJpaEntity, Long>`.

O Spring Data cria uma implementação em tempo de execução, fornecendo operações como:

- `save`;
- `findById`;
- `findAll`;
- `deleteById`.

Somente `save` é utilizado pelo caso de uso atual.

Esse repositório é um detalhe de infraestrutura. Ele não é injetado diretamente no serviço de aplicação.

### `UsuarioPersistenceMapper`

Converte objetos entre dois modelos:

- `Usuario` para `UsuarioJpaEntity` antes de salvar;
- `UsuarioJpaEntity` para `Usuario` depois de salvar.

Essa conversão parece pequena no exemplo, mas impede o acoplamento entre domínio e persistência. Quando os modelos evoluírem de formas diferentes, o mapper será o local explícito dessa tradução.

A classe é utilitária, possui construtor privado e métodos estáticos porque não mantém estado.

### `UsuarioRepositoryAdapter`

É o **adaptador de saída** que implementa `UsuarioRepositoryPort` usando Spring Data JPA.

Seu fluxo é:

1. recebe um `Usuario` do núcleo;
2. converte para `UsuarioJpaEntity`;
3. chama `UsuarioJpaRepository.save`;
4. recebe a entidade persistida com ID;
5. converte novamente para `Usuario`;
6. devolve o resultado ao caso de uso.

A anotação `@Repository` permite que o Spring descubra e injete essa implementação.

### `CriarUsuarioRequest`

Representa exclusivamente o JSON de entrada da rota.

É um `record`, adequado para um objeto imutável de transporte de dados. Contém anotações de validação porque pertence à fronteira web.

Não se utiliza `UsuarioJpaEntity` como entrada HTTP. Assim, o cliente não controla campos internos de persistência e o contrato da API pode evoluir separadamente do banco.

### `UsuarioResponse`

Representa o JSON devolvido ao cliente.

Também é um `record` e possui o método de fábrica `de`, que converte um `Usuario` para o formato público da API.

Separar response e domínio permite, por exemplo:

- ocultar atributos internos;
- renomear campos na API;
- adicionar links ou dados calculados;
- criar versões diferentes da API;
- evitar serialização acidental de todo o modelo de domínio.

### `UsuarioController`

É o adaptador de entrada HTTP.

Responsabilidades:

- mapear `POST /usuarios`;
- desserializar o JSON em `CriarUsuarioRequest`;
- solicitar validação com `@Valid`;
- chamar `CriarUsuarioUseCase`;
- converter o resultado em `UsuarioResponse`;
- devolver o status `201 Created`.

O controller não cria entidades JPA, não acessa repositórios Spring Data e não implementa regras de negócio.

### `UsuarioConfig`

É o ponto de composição da funcionalidade.

O método anotado com `@Bean` ensina ao Spring como construir a implementação da porta de entrada:

```java
@Bean
CriarUsuarioUseCase criarUsuarioUseCase(UsuarioRepositoryPort usuarioRepository) {
    return new CriarUsuarioService(usuarioRepository);
}
```

O Spring encontra `UsuarioRepositoryAdapter`, reconhece que ele implementa `UsuarioRepositoryPort` e o fornece ao construtor de `CriarUsuarioService`.

## Fluxo completo de uma requisição

Considere o envio:

```json
{
  "nome": "Maria",
  "email": "maria@exemplo.com"
}
```

O fluxo é:

```text
Cliente HTTP
    │
    │ POST /usuarios + JSON
    ▼
UsuarioController
    │ converte JSON em CriarUsuarioRequest e valida
    │
    │ criar(nome, email)
    ▼
CriarUsuarioUseCase
    │ implementação configurada
    ▼
CriarUsuarioService
    │ cria Usuario sem ID
    │
    │ salvar(usuario)
    ▼
UsuarioRepositoryPort
    │ implementação concreta
    ▼
UsuarioRepositoryAdapter
    │ converte domínio em entidade JPA
    ▼
UsuarioJpaRepository
    │ INSERT na tabela usuarios
    ▼
Banco H2
    │ devolve entidade com ID gerado
    ▼
UsuarioPersistenceMapper
    │ converte entidade em domínio
    ▼
CriarUsuarioService
    │ devolve Usuario persistido
    ▼
UsuarioController
    │ converte para UsuarioResponse
    ▼
Cliente recebe 201 + JSON
```

### Passo a passo detalhado

1. O servidor recebe uma requisição HTTP em `/usuarios`.
2. O Spring MVC identifica `UsuarioController.criar` pelo `@PostMapping`.
3. O conversor JSON cria um `CriarUsuarioRequest`.
4. `@Valid` executa as restrições `@NotBlank` e `@Email`.
5. Se houver erro, o Spring encerra o fluxo e responde `400 Bad Request`.
6. Se a entrada for válida, o controller chama a porta `CriarUsuarioUseCase`.
7. O bean concreto dessa porta é `CriarUsuarioService`.
8. O serviço cria o objeto de domínio sem ID.
9. O serviço chama `UsuarioRepositoryPort`.
10. O Spring injeta `UsuarioRepositoryAdapter` como implementação dessa porta.
11. O adapter traduz o domínio para uma entidade JPA.
12. O Spring Data executa a persistência.
13. O H2 gera o ID.
14. O adapter traduz o resultado de volta para domínio.
15. O controller cria `UsuarioResponse`.
16. `@ResponseStatus(HttpStatus.CREATED)` define o status HTTP 201.
17. O Spring serializa o response como JSON.

## Regras de dependência

Uma regra central da Clean Architecture é: **dependências de código devem apontar para dentro, em direção às regras mais estáveis**.

Neste projeto:

| Origem | Pode conhecer | Não deve conhecer diretamente |
|---|---|---|
| Domínio | Java e conceitos do domínio | Spring, HTTP, JPA, banco |
| Aplicação | domínio e suas próprias portas | controller, entidade JPA, Spring Data |
| Web | porta de entrada, domínio para conversão | implementação JPA, banco |
| Persistência | porta de saída, domínio, JPA | controller e request HTTP |
| Configuração | abstrações e implementações | regras novas de negócio |

### Inversão de dependência

Sem uma porta de saída, o serviço poderia depender diretamente de `UsuarioJpaRepository`:

```text
Serviço de negócio ──depende──► Spring Data JPA
```

Isso faria a regra central depender de um detalhe técnico.

Com a porta, o núcleo define a abstração e a infraestrutura fornece a implementação:

```text
CriarUsuarioService ──depende──► UsuarioRepositoryPort
                                        ▲
                                        │ implementa
                              UsuarioRepositoryAdapter
```

Esse é o princípio da inversão de dependência: módulos de alto nível e baixo nível se relacionam por abstrações adequadas, e o contrato necessário ao núcleo não é definido pela tecnologia externa.

## Decisões arquiteturais

### Por que não colocar tudo no controller?

Um controller que valida, cria entidade e salva diretamente pode parecer mais curto, mas mistura:

- protocolo HTTP;
- regra de aplicação;
- persistência;
- conversão de dados;
- decisão de resposta.

Isso dificulta testes, reutilização e evolução. No projeto, o controller é apenas um adaptador entre HTTP e o caso de uso.

### Por que há duas representações de usuário?

`Usuario` representa o domínio. `UsuarioJpaEntity` representa a persistência.

Embora tenham os mesmos campos hoje, possuem motivos diferentes para mudar:

- o domínio muda quando as regras do negócio mudam;
- a entidade muda quando o esquema ou a tecnologia de persistência muda.

Mantê-las separadas protege o domínio e torna esses motivos explícitos.

### Por que request e response também são separados?

O contrato público da API não deve ser acidentalmente igual à estrutura interna ou à tabela.

Com modelos separados:

- o cliente não envia `id` na criação;
- alterações internas não vazam automaticamente para a API;
- validações HTTP permanecem na borda;
- a API devolve somente os campos planejados.

### Por que o serviço não usa `@Service`?

Para demonstrar que o caso de uso não precisa conhecer Spring. `UsuarioConfig` constrói o serviço e registra seu objeto como bean.

Usar `@Service` seria possível e comum em projetos Spring, mas acoplaria a classe de aplicação a uma anotação do framework. A escolha atual deixa a independência mais visível para fins didáticos.

### Por que injeção por construtor?

A injeção por construtor:

- deixa dependências obrigatórias explícitas;
- permite atributos imutáveis;
- facilita instanciação em testes sem Spring;
- evita objetos parcialmente configurados;
- dispensa `@Autowired` quando há um único construtor.

### Por que usar um método de fábrica `Usuario.novo`?

`Usuario.novo(nome, email)` comunica melhor a intenção do que espalhar `new Usuario(null, ...)` pela aplicação. Também oferece um local natural para evoluir as regras de criação do domínio.

### Por que usar records nos DTOs?

Requests e responses são transportadores imutáveis de dados. Records reduzem código repetitivo e geram construtor, acessores, `equals`, `hashCode` e `toString`.

O domínio permanece como classe comum para facilitar a futura inclusão de comportamento e encapsulamento.

### Por que H2?

O H2 elimina a necessidade de configurar um servidor externo durante a aula. A aplicação inicia com um banco vazio, cria a tabela e está pronta para receber requisições.

Ele não representa necessariamente a escolha recomendada para produção. Seu papel aqui é reduzir atrito no ambiente educacional.

### Por que o Spring Security não está habilitado?

A autenticação não faz parte do caso de uso atual. Habilitar o starter de segurança bloquearia o `POST` por autenticação e CSRF padrão, adicionando conceitos não relacionados ao objetivo da aula.

Segurança deve ser adicionada conscientemente em uma evolução própria, com contrato de autenticação, autorização, configuração de CORS/CSRF e testes adequados. A ausência atual não significa que APIs de produção devam operar sem segurança.

## Persistência e banco H2

A configuração está em `src/main/resources/application.properties`:

```properties
spring.application.name=web
spring.datasource.url=jdbc:h2:mem:aula
spring.jpa.hibernate.ddl-auto=create-drop
```

### Significado das propriedades

`spring.datasource.url=jdbc:h2:mem:aula` cria um banco H2 chamado `aula` na memória do processo Java.

`spring.jpa.hibernate.ddl-auto=create-drop` orienta o Hibernate a:

1. criar o esquema quando o contexto inicia;
2. remover o esquema quando o contexto é encerrado.

### Consequências do banco em memória

- os dados existem somente enquanto a aplicação está executando;
- reiniciar a aplicação apaga os usuários cadastrados;
- ambientes e execuções de teste ficam isolados;
- não é necessário criar tabelas manualmente;
- o comportamento é conveniente para demonstração, mas não para produção.

### SQL conceitualmente executado

O Hibernate gera comandos equivalentes a:

```sql
create table usuarios (
    id bigint generated by default as identity,
    nome varchar(255),
    email varchar(255),
    primary key (id)
);
```

Ao cadastrar:

```sql
insert into usuarios (nome, email) values (?, ?);
```

O SQL exato pode variar de acordo com a versão do Hibernate e o dialeto detectado.

### Exibir SQL durante a aula

Para observar os comandos gerados, acrescente temporariamente:

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Não é recomendável depender desse log como estratégia de auditoria ou ativá-lo indiscriminadamente em produção, pois valores e volume de consultas podem ser sensíveis.

## Tratamento de dados e mapeamentos

Existem três representações principais no fluxo:

| Representação | Camada | Finalidade |
|---|---|---|
| `CriarUsuarioRequest` | web | receber e validar JSON |
| `Usuario` | domínio | representar o conceito usado pelo caso de uso |
| `UsuarioJpaEntity` | persistência | mapear a tabela do banco |
| `UsuarioResponse` | web | definir o JSON público de saída |

As conversões são deliberadas:

```text
JSON
  ↓ Spring MVC
CriarUsuarioRequest
  ↓ Controller
Usuario
  ↓ UsuarioPersistenceMapper
UsuarioJpaEntity
  ↓ JPA / banco / JPA
UsuarioJpaEntity com ID
  ↓ UsuarioPersistenceMapper
Usuario
  ↓ UsuarioResponse.de
UsuarioResponse
  ↓ Spring MVC
JSON
```

O custo de escrever mappers é compensado pela clareza de fronteiras. Em exemplos muito pequenos pode parecer repetição; em sistemas reais, modelos de API, domínio e banco frequentemente divergem.

## Testes

### Executar todos os testes

No Windows:

```powershell
.\gradlew.bat test
```

No Linux ou macOS:

```bash
./gradlew test
```

O relatório HTML é gerado em:

```text
build/reports/tests/test/index.html
```

### Teste de contexto

`WebApplicationTests` utiliza `@SpringBootTest` e verifica que o contexto completo consegue iniciar.

Esse teste detecta problemas como:

- beans ausentes;
- dependências circulares;
- configuração de JPA inválida;
- falhas na criação do datasource;
- erros no mapeamento das entidades.

### Teste do caso de uso

`CriarUsuarioServiceTests` instancia o serviço diretamente e usa uma implementação em memória da porta:

```java
UsuarioRepositoryPort repositorioEmMemoria = usuario -> {
    assertNull(usuario.getId());
    return new Usuario(1L, usuario.getNome(), usuario.getEmail());
};
```

Esse teste não inicia Spring, servidor HTTP ou banco. Ele demonstra um benefício direto da arquitetura: o caso de uso depende de uma interface simples e pode ser verificado de forma rápida e isolada.

O teste confirma que:

- o usuário chega ao repositório sem ID;
- o repositório atribui um ID simulado;
- nome e e-mail são preservados;
- o resultado persistido é devolvido pelo serviço.

### Testes recomendados para evolução

- teste do controller com MockMvc;
- teste de validação para nome vazio;
- teste de validação para e-mail inválido;
- teste de integração do adapter com H2;
- teste de conflito para e-mail duplicado;
- teste arquitetural com ArchUnit;
- teste de contrato do formato de erro;
- teste ponta a ponta do `POST /usuarios`.

## Como depurar e explorar o projeto

### Pontos de interrupção sugeridos

Para acompanhar uma requisição, coloque breakpoints nesta ordem:

1. `UsuarioController.criar`;
2. `CriarUsuarioService.criar`;
3. `UsuarioRepositoryAdapter.salvar`;
4. `UsuarioPersistenceMapper.paraEntidade`;
5. `UsuarioPersistenceMapper.paraDominio`;
6. `UsuarioResponse.de`.

Envie então um `POST /usuarios` e observe:

- a criação do request pelo Spring;
- o ID nulo antes da persistência;
- a conversão para entidade;
- o ID preenchido depois de `repository.save`;
- a conversão da resposta.

### Experimentos didáticos

Alguns exercícios úteis:

1. Substitua temporariamente o adapter JPA por um repositório baseado em `Map`.
2. Observe que `CriarUsuarioService` não precisa mudar.
3. Adicione um campo ao response sem alterar a tabela.
4. Adicione uma regra ao domínio e teste sem iniciar Spring.
5. Crie um segundo adaptador de entrada, como um `CommandLineRunner`, usando o mesmo caso de uso.
6. Adicione uma operação de busca com novas portas de entrada e saída.

## Limitações e próximos passos

A implementação é propositalmente básica. Antes de utilizá-la como referência de produção, considere as evoluções abaixo.

### 1. E-mail único

Atualmente, usuários com o mesmo e-mail podem ser cadastrados várias vezes.

Uma evolução adequada incluiria:

- restrição `unique` no banco;
- método `existePorEmail` ou consulta equivalente na porta de saída;
- erro de domínio ou aplicação específico;
- resposta HTTP `409 Conflict`;
- teste para condição de concorrência.

A regra não deve depender apenas de uma consulta prévia, pois duas requisições simultâneas ainda podem competir. A restrição no banco é a garantia final de integridade.

### 2. Normalização e regras do domínio

Nome e e-mail são persistidos como recebidos. Poderiam ser adicionados:

- remoção de espaços externos;
- normalização do e-mail para minúsculas, conforme a regra definida;
- limites de tamanho;
- objetos de valor como `Email`;
- regras explícitas de alteração.

### 3. Tratamento global de erros

Um `@RestControllerAdvice` poderia padronizar respostas como:

```json
{
  "status": 400,
  "erro": "Dados inválidos",
  "campos": {
    "email": "O e-mail deve ser válido"
  }
}
```

O contrato deveria ser documentado e testado.

### 4. Localização do recurso criado

Uma API REST mais completa pode devolver o header `Location`:

```http
Location: /usuarios/1
```

Para isso, também faria sentido implementar `GET /usuarios/{id}`.

### 5. Banco de produção e migrações

Para persistência real:

- substituir H2 por PostgreSQL, MySQL ou banco escolhido;
- fornecer configurações por perfil e variáveis de ambiente;
- utilizar Flyway ou Liquibase;
- evitar `ddl-auto=create-drop`;
- configurar pool de conexões e observabilidade;
- proteger credenciais.

### 6. Transações

O caso atual executa uma única gravação. Casos de uso com múltiplas operações precisam de uma fronteira transacional conscientemente definida.

Em Clean Architecture, a localização de `@Transactional` é uma decisão de projeto: pode ficar em um adapter/decorador de infraestrutura ou, com um compromisso pragmático, no serviço. O importante é reconhecer o acoplamento e garantir atomicidade no nível correto.

### 7. Segurança

Uma API de produção normalmente precisará de:

- autenticação;
- autorização por operação;
- armazenamento seguro de senhas, caso existam;
- política de CORS;
- decisão explícita sobre CSRF;
- limitação de requisições;
- logs e auditoria sem dados sensíveis;
- validação de ameaças e atualização de dependências.

O cadastro público ou protegido depende dos requisitos do produto, não apenas de uma configuração técnica padrão.

### 8. Observabilidade

Podem ser adicionados:

- logs estruturados;
- identificador de correlação;
- métricas com Micrometer;
- endpoints de saúde com Actuator;
- tracing distribuído;
- alertas para erros e latência.

### 9. Novos casos de uso

Possíveis operações:

| Método | Rota | Caso de uso |
|---|---|---|
| `GET` | `/usuarios/{id}` | buscar usuário por ID |
| `GET` | `/usuarios` | listar usuários com paginação |
| `PUT` | `/usuarios/{id}` | atualizar usuário |
| `DELETE` | `/usuarios/{id}` | remover ou desativar usuário |

Cada operação pode possuir sua própria porta de entrada. As portas de saída podem ser ampliadas conforme as necessidades reais dos casos de uso, evitando criar interfaces genéricas apenas para imitar o `JpaRepository`.

### 10. Documentação do contrato

Pode-se adicionar OpenAPI/Swagger para fornecer:

- descrição navegável dos endpoints;
- schemas de request e response;
- exemplos;
- códigos de status;
- contrato de erros;
- mecanismo de autenticação.

O OpenAPI documenta o contrato externo, enquanto este README explica também as decisões internas e pedagógicas.

## Glossário

### Adapter

Componente que traduz entre uma tecnologia externa e uma porta do núcleo. O controller adapta HTTP para o caso de uso; o repository adapter adapta a porta de persistência para JPA.

### Bean

Objeto criado e gerenciado pelo contêiner do Spring.

### Caso de uso

Ação oferecida pela aplicação para cumprir um objetivo do usuário ou de outro sistema. Neste projeto: criar um usuário.

### Clean Architecture

Estilo arquitetural que separa regras centrais de detalhes externos e orienta as dependências em direção ao núcleo.

### Domínio

Conceitos, regras e comportamentos relacionados ao problema que o software resolve.

### DTO

Data Transfer Object. Objeto destinado ao transporte de dados entre fronteiras, como `CriarUsuarioRequest` e `UsuarioResponse`.

### Entidade JPA

Objeto mapeado para persistência relacional pelo Jakarta Persistence.

### Injeção de dependência

Técnica em que um objeto recebe suas dependências externamente, em vez de criá-las internamente.

### Mapper

Componente responsável por converter uma representação de dados em outra.

### Porta de entrada

Interface que representa uma operação oferecida pelo núcleo da aplicação. Exemplo: `CriarUsuarioUseCase`.

### Porta de saída

Interface que representa uma capacidade externa necessária ao núcleo. Exemplo: `UsuarioRepositoryPort`.

### Repository

Abstração de acesso a dados. Neste projeto há a porta definida pela aplicação, o adapter que a implementa e o repository Spring Data utilizado como detalhe técnico.

## Estado atual

O projeto oferece uma base pequena, executável e testada para estudar Clean Architecture com Spring Boot.

Funcionalidades disponíveis:

- criação de usuário por HTTP;
- validação de nome e e-mail;
- persistência em H2;
- geração automática de ID;
- resposta HTTP `201 Created`;
- separação entre domínio, aplicação, web e persistência;
- teste isolado do caso de uso;
- teste de inicialização do contexto Spring.

Para validar o estado atual:

```powershell
.\gradlew.bat test
```

Para executar e experimentar:

```powershell
.\gradlew.bat bootRun
```
