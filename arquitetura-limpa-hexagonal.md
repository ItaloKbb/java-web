# Arquitetura Limpa e Hexagonal

### Um guia longo, do zero, para quem nunca separou código em camadas

> Os exemplos estão em TypeScript porque é uma linguagem legível mesmo para quem não a usa.
> Tudo aqui vale igual em C#, Java, Python, PHP, Go ou Kotlin. Onde a linguagem muda algo
> importante, há uma nota.

---

## Sumário

| # | Parte | O que você entende aqui |
|---|-------|--------------------------|
| 0 | [Como ler este documento](#parte-0--como-ler-este-documento) | Ordem de leitura e expectativa |
| 1 | [O problema que arquitetura resolve](#parte-1--o-problema-que-arquitetura-resolve) | Por que o código apodrece |
| 2 | [A intuição da Hexagonal](#parte-2--a-intuição-da-arquitetura-hexagonal) | Ports, Adapters, dentro e fora |
| 3 | [A Regra da Dependência](#parte-3--a-regra-da-dependência) | A única regra que importa |
| 4 | [Camada Domain](#parte-4--camada-domain) | Entidades, Value Objects, Agregados |
| 5 | [Camada Application](#parte-5--camada-application) | Casos de uso e ports |
| 6 | [Camada Infrastructure](#parte-6--camada-infrastructure) | Adapters, banco, HTTP, filas |
| 7 | [Camada de Entrada](#parte-7--camada-de-entrada-driving-adapters) | Controllers, CLI, consumers |
| 8 | [Fluxo completo de uma requisição](#parte-8--o-fluxo-completo-de-uma-requisição) | O caminho do dado, passo a passo |
| 9 | [Exemplo end-to-end](#parte-9--exemplo-completo-end-to-end) | Um sistema inteiro, arquivo por arquivo |
| 10 | [Testes por camada](#parte-10--testes-em-cada-camada) | O que testar e como |
| 11 | [Clean × Hexagonal × Onion × MVC](#parte-11--clean--hexagonal--onion--mvc--ddd) | As diferenças reais |
| 12 | [Anti-padrões](#parte-12--anti-padrões-e-armadilhas) | Os erros que todo mundo comete |
| 13 | ["Onde eu coloco esse código?"](#parte-13--onde-eu-coloco-esse-código-árvore-de-decisão) | Árvore de decisão prática |
| 14 | [Quando NÃO usar](#parte-14--quando-não-usar-isso) | O custo honesto |
| 15 | [Glossário](#parte-15--glossário) | Todos os termos |
| 16 | [Checklist de revisão](#parte-16--checklist-de-revisão-de-código) | Para usar em PR |

---

## Parte 0 — Como ler este documento

Se você nunca ouviu falar de hexagonal, leia as Partes 1, 2 e 3 com calma e sem pular.
Elas são curtas e contêm a ideia inteira. As Partes 4 a 7 detalham cada camada. A Parte 9
é um sistema completo que amarra tudo, e é onde a ficha costuma cair de vez.

Uma advertência antes de começar: quase tudo que você vai ler parece burocracia
desnecessária na primeira leitura. Interfaces que só têm uma implementação, objetos que
copiam dados de um lugar para outro, pastas demais. Essa sensação é legítima e a Parte 14
trata dela honestamente. Mas ela some quando você vive a segunda mudança grande de um
sistema que dura três anos.

---

## Parte 1 — O problema que arquitetura resolve

### A história de todo sistema que dá errado

Começa assim. Você precisa cadastrar um usuário. Escreve um controller:

```ts
app.post('/usuarios', async (req, res) => {
  const { email, nome } = req.body;

  if (!email.includes('@')) {
    return res.status(400).json({ erro: 'e-mail inválido' });
  }

  const existente = await db.query('SELECT * FROM usuarios WHERE email = $1', [email]);
  if (existente.rows.length > 0) {
    return res.status(409).json({ erro: 'e-mail já cadastrado' });
  }

  await db.query('INSERT INTO usuarios (email, nome) VALUES ($1, $2)', [email, nome]);
  await sendgrid.send({ to: email, template: 'bem-vindo' });

  res.status(201).json({ ok: true });
});
```

Isso funciona. É rápido de escrever, fácil de ler, e para um projeto pequeno pode ser a
escolha certa. O problema não é o código de hoje. É o código de daqui a dezoito meses.

Porque aí chegam os pedidos:

1. "Precisamos cadastrar usuário também por importação de CSV."
2. "O app mobile precisa de um endpoint diferente que também cadastra."
3. "Trocamos SendGrid por Amazon SES."
4. "Agora o e-mail só pode ter domínio corporativo, exceto para o plano free."
5. "Vamos migrar de Postgres para DynamoDB nesse módulo."
6. "Precisamos de um teste automatizado disso, mas sem mandar e-mail de verdade."

Repare no que cada pedido faz com aquele bloco de código.

O pedido 1 e o 2 forçam você a copiar e colar a regra de validação para outros dois lugares.
Agora existem três cópias da mesma regra. Um dia alguém altera duas delas e esquece a
terceira, e nasce um bug que ninguém acha.

O pedido 3 exige abrir todos os arquivos que mencionam `sendgrid`. São dezenove.

O pedido 4 é uma regra de negócio real, e ela vai morar dentro de um handler HTTP, misturada
com `req.body` e `res.status`. Se amanhã a regra precisar valer também na importação de CSV,
volta o problema do pedido 1.

O pedido 5 é praticamente um reescreve.

O pedido 6 é o mais revelador de todos. Para testar a regra "e-mail precisa ter @", você
precisa subir um servidor HTTP, subir um Postgres e interceptar chamadas ao SendGrid. Uma
regra que é literalmente uma linha de `if` exige três peças de infraestrutura para ser
verificada.

### O diagnóstico

O problema tem um nome: **acoplamento**. Especificamente, a regra de negócio está acoplada
a três coisas que não têm nada a ver com ela:

- **ao protocolo de entrada** (HTTP, `req` e `res`)
- **à tecnologia de persistência** (SQL, Postgres, nomes de coluna)
- **a um fornecedor externo** (SendGrid)

A regra "e-mail precisa ser único" é verdade sobre o negócio. Ela seria verdade se o sistema
fosse um formulário de papel. Já "`SELECT * FROM usuarios WHERE email = $1`" é um detalhe de
como decidimos guardar os dados nesta terça-feira. São coisas de natureza diferente e com
velocidades de mudança diferentes, e elas estão na mesma função.

### A tese central

> **Regras de negócio são o ativo. Tecnologia é detalhe substituível.**
> A arquitetura deve proteger o primeiro do segundo.

Toda a Clean Architecture e toda a Hexagonal são desdobramentos dessa única frase. Se você
entender só isso e esquecer o resto do documento, já valeu.

Uma forma prática de sentir isso: pense no seu sistema e pergunte quanto tempo cada coisa
dura.

| Coisa | Vida útil típica |
|-------|------------------|
| Regras de negócio da empresa | 10 a 30 anos |
| Modelo de dados conceitual | 5 a 15 anos |
| Framework web escolhido | 3 a 7 anos |
| Versão específica do ORM | 1 a 3 anos |
| Formato do payload de uma API externa | Muda sem avisar |

Faz sentido que o item de cima dependa dos de baixo? Não. Mas é exatamente isso que o
código do começo desta seção faz: a regra de negócio depende do Express, do driver do
Postgres e do SDK do SendGrid. Se qualquer um desses três mudar, a regra é afetada.

Arquitetura, aqui, significa inverter isso.

---

## Parte 2 — A intuição da Arquitetura Hexagonal

A Arquitetura Hexagonal foi descrita por Alistair Cockburn em 2005. O nome oficial dela é
mais informativo que o apelido: **Ports and Adapters** (Portas e Adaptadores).

### A metáfora da tomada

Pense num notebook. Ele tem uma entrada USB-C. O notebook não sabe nada sobre a rede
elétrica do país onde você está. Ele não sabe se a energia veio de usina hidrelétrica,
painel solar ou de um powerbank. Ele definiu **um contrato**: "me entregue energia neste
formato, por este encaixe".

- A **entrada USB-C do notebook** é a **Port** (porta). É o contrato, definido pelo notebook,
  no vocabulário do notebook.
- A **fonte que você pluga** é o **Adapter** (adaptador). Ela sabe sobre 110V, 220V, tomada
  britânica, tomada brasileira, e traduz tudo aquilo para o contrato que o notebook exige.

Duas consequências importantes:

1. **Quem define o contrato é o lado de dentro**, não o de fora. O notebook não se adaptou
   à tomada brasileira; a tomada brasileira é que ganhou um adaptador. Isso vai voltar como
   a regra mais importante da Parte 3.
2. **Você pode trocar o adaptador sem abrir o notebook.** Viajou para o Japão? Troca a
   fonte. O notebook não sabe que algo mudou.

Agora substitua as palavras:

| Metáfora | Software |
|----------|----------|
| Notebook | Seu núcleo de negócio (domain + application) |
| Entrada USB-C | Interface `RepositorioDeUsuarios` |
| Fonte de energia | Classe `RepositorioPostgres` |
| Trocar de fonte | Trocar Postgres por Mongo sem tocar no negócio |
| Fonte de bancada de teste | `RepositorioEmMemoria` usado nos testes |

### Por que "hexágono"?

Por nada de especial. Cockburn escolheu um hexágono no desenho apenas porque precisava de
uma figura com vários lados para desenhar várias portas. Não existem seis camadas, não
existem seis tipos de porta, o número seis não significa nada. Se ele tivesse desenhado um
octógono, hoje falaríamos em arquitetura octogonal. Não perca tempo com o formato.

O que o desenho comunica é só isto: **existe um dentro e existe um fora**, e a fronteira
entre eles é feita de portas.

### O desenho

```
                 LADO ESQUERDO                              LADO DIREITO
              (quem chama o sistema)                  (quem o sistema chama)
              DRIVING / PRIMÁRIOS                      DRIVEN / SECUNDÁRIOS

   ┌──────────────┐                                            ┌──────────────┐
   │ Controller   │──┐                                    ┌───▶│  Postgres    │
   │   HTTP       │  │                                    │    └──────────────┘
   └──────────────┘  │      ╔══════════════════════╗      │
                     │      ║                      ║      │    ┌──────────────┐
   ┌──────────────┐  │      ║     APPLICATION      ║      ├───▶│  SendGrid    │
   │  CLI / job   │──┼─────▶║   (casos de uso)     ║──────┤    └──────────────┘
   └──────────────┘  │      ║  ┌────────────────┐  ║      │
                     │      ║  │                │  ║      │    ┌──────────────┐
   ┌──────────────┐  │      ║  │     DOMAIN     │  ║      ├───▶│   RabbitMQ   │
   │ Consumidor   │──┤      ║  │   (o núcleo)   │  ║      │    └──────────────┘
   │  de fila     │  │      ║  │                │  ║      │
   └──────────────┘  │      ║  └────────────────┘  ║      │    ┌──────────────┐
                     │      ║                      ║      └───▶│  API de CEP  │
   ┌──────────────┐  │      ╚══════════════════════╝           └──────────────┘
   │  Teste       │──┘               ▲    ▲
   │ automatizado │                  │    │
   └──────────────┘            porta de   porta de
                                entrada    saída
```

### As duas famílias de porta

Essa distinção confunde muita gente no começo, e é a coisa mais útil da hexagonal. A
pergunta que separa as duas é: **quem inicia a conversa?**

**Portas de entrada (driving / primárias / lado esquerdo).**
O mundo externo chama o seu sistema. Um usuário clica em "Comprar", uma mensagem chega numa
fila, um cron dispara às 3h. Aqui, o ator externo dirige. A porta de entrada é, na prática,
a assinatura pública dos seus casos de uso: "este sistema sabe fazer X". Quem implementa
essa porta é o próprio núcleo. Quem consome é o controller.

**Portas de saída (driven / secundárias / lado direito).**
O seu sistema chama o mundo externo. Ele precisa salvar algo, buscar algo, mandar um e-mail,
saber que horas são. Aqui, o núcleo dirige. A porta de saída é uma **interface declarada
dentro do núcleo** descrevendo o que ele precisa, no vocabulário dele. Quem implementa essa
porta é a infraestrutura.

Um jeito de nunca mais errar:

> Se o fluxo de controle **entra** no seu sistema, é porta de entrada.
> Se o fluxo de controle **sai** do seu sistema, é porta de saída.
> Em ambos os casos, **a dependência de código aponta para dentro**.

Essa última linha parece contraditória no caso da porta de saída, e o truque que resolve
isso é o assunto da próxima parte.

### Ports e Adapters, lado a lado

| | Porta de entrada | Porta de saída |
|---|---|---|
| Quem inicia | O mundo externo | O núcleo |
| Exemplo de porta | `CadastrarUsuario` (caso de uso) | `RepositorioDeUsuarios` (interface) |
| Onde a porta é declarada | Camada application | Camada application (ou domain) |
| Quem implementa a porta | A camada application | A camada infrastructure |
| Exemplo de adapter | `UsuarioController` (Express) | `UsuarioRepositoryPostgres` |
| Trocar o adapter significa | Expor o sistema por GraphQL, CLI, gRPC | Trocar de banco, de provedor de e-mail |
| No teste, o adapter vira | O próprio arquivo de teste | Um fake em memória |

Note a última linha. Num teste, **o teste é o adapter de entrada** e os **fakes são os
adapters de saída**. Isso não é uma gambiarra de teste: é exatamente o mesmo mecanismo que
permite trocar Postgres por Mongo. Testabilidade não é um objetivo separado da arquitetura,
é um subproduto automático dela. Se testar está difícil, o acoplamento está errado.

---

## Parte 3 — A Regra da Dependência

Se a Parte 2 deu a intuição, esta parte dá o mecanismo. É a parte mais importante do
documento inteiro.

### O enunciado

Robert C. Martin resumiu a Clean Architecture numa frase:

> **Dependências de código-fonte apontam apenas para dentro.**
> Nada num círculo interno pode saber nada sobre algo num círculo externo.

```
        ┌───────────────────────────────────────────────────┐
        │  INFRASTRUCTURE  (frameworks, banco, HTTP, filas)  │
        │   ┌───────────────────────────────────────────┐   │
        │   │  APPLICATION  (casos de uso, ports)       │   │
        │   │    ┌─────────────────────────────────┐    │   │
        │   │    │  DOMAIN  (entidades, regras)    │    │   │
        │   │    └─────────────────────────────────┘    │   │
        │   └───────────────────────────────────────────┘   │
        └───────────────────────────────────────────────────┘

              As setas de import SEMPRE apontam para dentro:

              infrastructure ──▶ application ──▶ domain

              E NUNCA o contrário. Nem uma vez. Nem "só nesse caso".
```

Traduzindo para uma regra que dá para verificar mecanicamente, abrindo qualquer arquivo:

- Um arquivo em `domain/` **não pode ter nenhum `import`** de `application/` nem de
  `infrastructure/`, e nenhum import de biblioteca de framework.
- Um arquivo em `application/` **pode importar** de `domain/`. Não pode importar de
  `infrastructure/`.
- Um arquivo em `infrastructure/` **pode importar** de qualquer lugar.

Se você conseguir escrever um script de lint que verifica essas três linhas, você tem
arquitetura de verdade e não apenas pastas com nomes bonitos. Isso é literalmente possível
e recomendável (veja a Parte 16).

### O paradoxo, e a solução

Aqui está o nó que trava todo mundo:

> "Beleza, o domínio não pode conhecer o banco. Mas meu caso de uso **precisa** salvar
> no banco. Como ele salva sem conhecer quem salva?"

A resposta é o **Princípio da Inversão de Dependência** (o D do SOLID), e é o único truque
técnico que você precisa dominar aqui. Vale a pena ver o antes e o depois.

#### Antes: dependência apontando para fora

```ts
// application/CadastrarUsuario.ts   ❌ ERRADO
import { UsuarioRepositoryPostgres } from '../infrastructure/UsuarioRepositoryPostgres';
//     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^ application está olhando para infrastructure

export class CadastrarUsuario {
  private repo = new UsuarioRepositoryPostgres(); // e ainda instancia sozinho

  async executar(email: string) {
    await this.repo.salvar(email);
  }
}
```

Problemas concretos, não filosóficos:

- Para testar `CadastrarUsuario` você precisa de um Postgres rodando.
- Para trocar de banco você edita a camada de aplicação.
- Compilar a aplicação exige compilar a infraestrutura.
- A seta aponta para fora, violando a regra.

#### Depois: dependência invertida

Duas mudanças. Primeiro, **o núcleo declara o contrato que ele quer**:

```ts
// application/ports/RepositorioDeUsuarios.ts   ✅
import { Usuario } from '../../domain/Usuario';

export interface RepositorioDeUsuarios {
  buscarPorEmail(email: Email): Promise<Usuario | null>;
  salvar(usuario: Usuario): Promise<void>;
}
```

Repare no vocabulário: `buscarPorEmail`, `salvar`, `Usuario`. Não aparece `SELECT`, não
aparece `tabela`, não aparece `connection pool`. O contrato foi escrito na língua do
negócio, porque quem o escreveu foi o negócio.

Segundo, **o caso de uso recebe a implementação de fora** em vez de criá-la:

```ts
// application/CadastrarUsuario.ts   ✅
import { RepositorioDeUsuarios } from './ports/RepositorioDeUsuarios';

export class CadastrarUsuario {
  constructor(private readonly repo: RepositorioDeUsuarios) {}
  //                                 ^^^^^^^^^^^^^^^^^^^^^ interface, não classe concreta

  async executar(email: Email) {
    await this.repo.salvar(Usuario.criar(email));
  }
}
```

E a infraestrutura implementa o contrato alheio:

```ts
// infrastructure/persistence/UsuarioRepositoryPostgres.ts   ✅
import { RepositorioDeUsuarios } from '../../application/ports/RepositorioDeUsuarios';
//     ^^^ infrastructure importando de application: seta apontando PARA DENTRO ✔

export class UsuarioRepositoryPostgres implements RepositorioDeUsuarios {
  constructor(private readonly db: Pool) {}

  async salvar(usuario: Usuario): Promise<void> {
    await this.db.query(
      'INSERT INTO usuarios (id, email, nome) VALUES ($1, $2, $3)',
      [usuario.id.valor, usuario.email.valor, usuario.nome],
    );
  }
  // ...
}
```

### O que exatamente foi invertido

Vale desenhar, porque a palavra "inversão" é abstrata demais:

```
ANTES                                   DEPOIS

application                             application
    │                                       │  declara
    │ importa                               ▼
    ▼                                   [ interface ]
infrastructure                              ▲
                                            │ implementa
Fluxo de controle:  →                   infrastructure
Dependência:        →
                                        Fluxo de controle:  →   (continua igual!)
                                        Dependência:        ←   (inverteu!)
```

Em tempo de execução, nada mudou: o caso de uso continua chamando o Postgres. O que mudou
foi **a direção da dependência de compilação**. O caso de uso agora depende de uma abstração
que ele mesmo definiu, e a implementação concreta é que se curva a ela.

É a fonte USB-C de novo. O notebook define o encaixe; a fonte se adapta.

### O ponto onde tudo é ligado

Se ninguém instancia mais suas próprias dependências, alguém precisa fazer isso. Esse
alguém é o **Composition Root**: um único lugar, na borda mais externa do sistema
(normalmente `main.ts`, `Program.cs`, `bootstrap.py`), que monta o grafo de objetos.

```ts
// main.ts — a borda de tudo
const pool  = new Pool({ connectionString: process.env.DATABASE_URL });
const repo  = new UsuarioRepositoryPostgres(pool);
const email = new EmailSendGrid(process.env.SENDGRID_KEY);
const relogio = new RelogioDoSistema();

const cadastrarUsuario = new CadastrarUsuario(repo, email, relogio);

app.post('/usuarios', new UsuarioController(cadastrarUsuario).handle);
```

Esse arquivo é o único do sistema que conhece todo mundo, e é por isso que ele pode ser feio.
Um container de injeção de dependência (NestJS, Spring, .NET DI, Guice) automatiza esse
arquivo, mas conceitualmente é isso: **o mundo externo escolhe as peças e as entrega prontas
para o núcleo, que só conhece contratos.**

Note também que trocar de banco agora é editar uma linha deste arquivo. Esse é o retorno
concreto de todo o investimento.

### Por que interfaces mesmo com uma implementação só

A objeção mais comum: "eu só tenho Postgres, nunca vou trocar, essa interface é overhead
inútil".

Três respostas.

1. **Você já tem duas implementações**: a real e a de teste. Um `RepositorioEmMemoria` faz
   sua suíte de testes rodar em milissegundos, sem Docker, sem limpar tabela entre testes.
2. **A interface não existe principalmente para permitir troca.** Ela existe para **inverter
   a seta**. O valor está em o domínio não conhecer o Postgres, não em você um dia usar
   Mongo. A troca é um efeito colateral agradável.
3. **A interface documenta o que o núcleo precisa.** Ler `RepositorioDeUsuarios` diz em dez
   linhas tudo que o sistema faz com usuários no armazenamento. Ler a classe Postgres exige
   passar por SQL, mapeamento e tratamento de erro de driver.

Dito isso, a Parte 14 é honesta sobre quando isso não compensa.

---

## Parte 4 — Camada Domain

O núcleo. O anel mais interno. A camada que não importa nada de ninguém.

### O teste definitivo do domínio

Antes de qualquer definição formal, guarde esta pergunta e aplique-a a todo arquivo que
você pensar em colocar em `domain/`:

> **Essa regra continuaria verdadeira se a empresa operasse com fichas de papel, sem
> computador nenhum?**

"Um cliente inadimplente não pode fazer novo pedido" — verdade no papel. É domínio.
"O campo `status` da tabela `pedidos` é um `VARCHAR(20)`" — não existe no papel. Não é
domínio.

Uma variação técnica da mesma pergunta: **se eu deletar a pasta `infrastructure/` inteira,
a pasta `domain/` ainda compila?** Se a resposta for não, há vazamento.

### O que mora aqui

| Elemento | Papel |
|---|---|
| **Entidade** | Objeto com identidade própria que muda ao longo do tempo |
| **Value Object** | Objeto definido pelos seus valores, imutável, sem identidade |
| **Agregado** | Conjunto de objetos tratado como uma unidade de consistência |
| **Domain Service** | Regra que envolve várias entidades e não pertence a nenhuma |
| **Domain Event** | Registro de que algo relevante para o negócio aconteceu |
| **Erros de domínio** | Exceções que expressam violação de regra de negócio |
| **Enums e tipos do negócio** | `StatusPedido`, `TipoDePlano` |
| **Interfaces de repositório** | Opcional aqui; veja a nota na Parte 5 |

### O que NUNCA mora aqui

- `import express`, `import { Entity } from 'typeorm'`, `@Table`, `@Column`, `@Injectable`
- SQL, nomes de tabela, nomes de coluna
- JSON de API externa, DTOs de request/response
- `console.log`, chamadas HTTP, leitura de arquivo, `new Date()` espalhado
- Qualquer `async` que exista porque algo é I/O

Aquele último item merece destaque. **Métodos de domínio são, quase sempre, síncronos.**
Se um método de entidade é `async`, provavelmente ele está fazendo I/O, e I/O é sempre
infraestrutura. Regra de negócio pura é cálculo e decisão sobre dados que já estão na mão.

---

### 4.1 Value Object

Um Value Object é um objeto que **não tem identidade**: ele é o que ele vale. Duas notas de
R$ 50 são intercambiáveis. Dois `Email` com o mesmo texto são o mesmo email. Não faz sentido
perguntar "qual é o id deste CPF".

Três características obrigatórias:

1. **Imutável.** Não tem setter. Para "mudar", você cria outro.
2. **Comparado por valor**, não por referência.
3. **Auto-validado.** Não é possível existir um `Email` inválido. O construtor impede.

```ts
// domain/value-objects/Email.ts
export class Email {
  private constructor(public readonly valor: string) {}

  static criar(entrada: string): Email {
    const normalizado = entrada.trim().toLowerCase();
    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(normalizado)) {
      throw new EmailInvalido(entrada);
    }
    return new Email(normalizado);
  }

  igualA(outro: Email): boolean {
    return this.valor === outro.valor;
  }
}
```

O construtor é privado e a criação passa por um método estático. Isso garante que **não
existe caminho no sistema inteiro que produza um Email inválido**. Uma vez que você tem um
objeto do tipo `Email` na mão, ele é válido. Ponto.

Compare o ganho:

```ts
// Sem Value Object
function transferir(de: string, para: string, valor: number, moeda: string) {}
transferir(valor, moeda, de, para); // compila. e está errado.

// Com Value Object
function transferir(de: Conta, para: Conta, quantia: Dinheiro) {}
```

Na primeira versão, quatro parâmetros primitivos em que trocar a ordem passa despercebido
pelo compilador e explode em produção. Isso tem nome: **Obsessão por Primitivos**. Na
segunda, o compilador vira seu revisor.

Um segundo exemplo, mais rico, porque `Dinheiro` mostra por que imutabilidade importa:

```ts
// domain/value-objects/Dinheiro.ts
export class Dinheiro {
  private constructor(
    public readonly centavos: number,
    public readonly moeda: 'BRL' | 'USD',
  ) {}

  static reais(valor: number): Dinheiro {
    return new Dinheiro(Math.round(valor * 100), 'BRL');
  }

  somar(outro: Dinheiro): Dinheiro {
    this.garantirMesmaMoeda(outro);
    return new Dinheiro(this.centavos + outro.centavos, this.moeda); // devolve NOVO
  }

  multiplicar(fator: number): Dinheiro {
    return new Dinheiro(Math.round(this.centavos * fator), this.moeda);
  }

  maiorQue(outro: Dinheiro): boolean {
    this.garantirMesmaMoeda(outro);
    return this.centavos > outro.centavos;
  }

  private garantirMesmaMoeda(outro: Dinheiro): void {
    if (this.moeda !== outro.moeda) throw new MoedasIncompativeis(this.moeda, outro.moeda);
  }
}
```

Três coisas acontecendo aqui, todas de negócio:

- Dinheiro é guardado em centavos inteiros, porque `0.1 + 0.2 !== 0.3` em ponto flutuante e
  isso é um bug contábil, não um detalhe técnico.
- Somar reais com dólares é impossível por construção.
- `somar` devolve um objeto novo. Nenhum código em outro lugar do sistema consegue alterar
  um `Dinheiro` que você está segurando. Isso elimina uma classe inteira de bugs de aliasing.

> **Regra prática:** sempre que um conceito do negócio tiver validação, formatação ou
> comportamento próprio, ele merece um Value Object. CPF, CNPJ, CEP, Email, Telefone,
> Dinheiro, Periodo, Quantidade, Percentual, Coordenada. Se você já escreveu
> `validarCpf(cpf: string)` em três arquivos, você estava precisando de um `Cpf`.

---

### 4.2 Entidade

Uma Entidade **tem identidade** e essa identidade sobrevive a mudanças de estado. O usuário
João é o mesmo João depois que ele muda de e-mail, de nome e de endereço. Duas entidades são
iguais se têm o mesmo id, mesmo que todos os outros campos difiram.

```ts
// domain/entities/Usuario.ts
export class Usuario {
  private constructor(
    public readonly id: UsuarioId,
    private _email: Email,
    private _nome: string,
    private _status: StatusUsuario,
    private readonly _criadoEm: Date,
  ) {}

  // ---------- criação ----------
  static registrar(id: UsuarioId, email: Email, nome: string, agora: Date): Usuario {
    if (nome.trim().length < 2) throw new NomeInvalido(nome);
    return new Usuario(id, email, nome.trim(), StatusUsuario.PendenteDeConfirmacao, agora);
  }

  /** Usado apenas pela infraestrutura ao reidratar do banco. */
  static reconstituir(props: UsuarioProps): Usuario {
    return new Usuario(props.id, props.email, props.nome, props.status, props.criadoEm);
  }

  // ---------- comportamento ----------
  confirmarEmail(): void {
    if (this._status !== StatusUsuario.PendenteDeConfirmacao) {
      throw new ConfirmacaoInvalida(this._status);
    }
    this._status = StatusUsuario.Ativo;
  }

  bloquear(motivo: string): void {
    if (this._status === StatusUsuario.Bloqueado) return; // idempotente
    this._status = StatusUsuario.Bloqueado;
  }

  podeFazerLogin(): boolean {
    return this._status === StatusUsuario.Ativo;
  }

  // ---------- leitura ----------
  get email(): Email { return this._email; }
  get nome(): string { return this._nome; }
  get status(): StatusUsuario { return this._status; }
}
```

Três decisões de projeto aqui merecem explicação.

**Campos privados com getters, sem setters.** Se existisse `usuario.status = 'ATIVO'`,
qualquer lugar do sistema poderia colocar o usuário num estado inválido. Com
`confirmarEmail()`, a transição de estado é uma operação com regra. O objeto protege as
próprias invariantes. Isso é encapsulamento de verdade, não o encapsulamento decorativo de
um getter/setter para cada campo.

**Dois construtores estáticos.** `registrar` é o nascimento de um usuário novo e aplica todas
as validações. `reconstituir` é a volta do banco: aquele usuário já existiu e já foi válido,
então revalidar não faz sentido (e às vezes é até impossível, se a regra mudou desde então).
Essa distinção evita um problema clássico de ORM.

**`agora: Date` chega como parâmetro.** A entidade não chama `new Date()`. Se chamasse,
seria impossível testar "o que acontece com um usuário criado há 90 dias" sem manipular o
relógio do sistema. Tempo é uma dependência externa como qualquer outra. Isso vira um port
chamado `Relogio`, que você verá na Parte 5.

#### Modelo Anêmico: o erro mais comum

Compare a entidade acima com isto:

```ts
// ❌ Modelo anêmico: um saco de dados com getters e setters
export class Usuario {
  id: string;
  email: string;
  nome: string;
  status: string;
}

// e a regra vive espalhada em algum "service"
class UsuarioService {
  confirmar(u: Usuario) {
    if (u.status !== 'PENDENTE') throw new Error('inválido');
    u.status = 'ATIVO';
  }
}
```

Isso é o que Martin Fowler chamou de **Anemic Domain Model**. Funciona, é comum e é o
padrão de fato em muitos projetos Spring, Rails e Laravel. O custo é que o objeto `Usuario`
não protege nada: em qualquer canto do sistema alguém pode fazer `u.status = 'ATIVO'` sem
passar pela regra. Com o tempo, aparecem cinco lugares diferentes que mudam status, com
três interpretações diferentes da regra.

Uma heurística: **se suas classes de domínio só têm getters e setters, seu domínio está na
verdade dentro dos services**, e você tem procedural com sintaxe de objeto. Nem sempre isso
é errado (Parte 14), mas saiba que é a escolha que você fez.

---

### 4.3 Agregado e Raiz de Agregado

Este é o conceito mais sutil da parte, e o que mais evita bugs de concorrência.

Um **Agregado** é um grupo de entidades e value objects que precisam ser mantidos
consistentes **juntos**, tratados como uma unidade. Um deles é a **Raiz de Agregado**
(*Aggregate Root*), e ela é a única porta de entrada: nada de fora pode segurar referência
direta aos objetos internos.

Exemplo clássico: um `Pedido` com seus `ItemDePedido`.

```ts
// domain/entities/Pedido.ts  — raiz de agregado
export class Pedido {
  private constructor(
    public readonly id: PedidoId,
    public readonly clienteId: ClienteId,
    private _itens: ItemDePedido[],
    private _status: StatusPedido,
  ) {}

  static abrir(id: PedidoId, clienteId: ClienteId): Pedido {
    return new Pedido(id, clienteId, [], StatusPedido.Rascunho);
  }

  adicionarItem(produtoId: ProdutoId, quantidade: number, precoUnitario: Dinheiro): void {
    if (this._status !== StatusPedido.Rascunho) {
      throw new PedidoNaoEditavel(this.id, this._status);
    }
    if (this._itens.length >= 50) {
      throw new LimiteDeItensExcedido(50);
    }

    const existente = this._itens.find(i => i.produtoId.igualA(produtoId));
    if (existente) {
      existente.aumentarQuantidade(quantidade);
    } else {
      this._itens.push(ItemDePedido.criar(produtoId, quantidade, precoUnitario));
    }
  }

  finalizar(): void {
    if (this._itens.length === 0) throw new PedidoVazio(this.id);
    if (this.total().maiorQue(Dinheiro.reais(50_000))) {
      throw new PedidoAcimaDoLimite(this.total());
    }
    this._status = StatusPedido.AguardandoPagamento;
  }

  total(): Dinheiro {
    return this._itens.reduce(
      (acc, item) => acc.somar(item.subtotal()),
      Dinheiro.reais(0),
    );
  }

  /** Cópia defensiva: ninguém de fora mexe na lista interna. */
  get itens(): ReadonlyArray<ItemDePedido> {
    return [...this._itens];
  }
}
```

O que a raiz de agregado garante: **é impossível adicionar item a um pedido já finalizado**.
Não é impossível "se todo mundo lembrar de checar"; é impossível porque o único caminho para
adicionar um item passa por `Pedido.adicionarItem`, que checa. Se `itens` fosse público e
mutável, `pedido.itens.push(...)` furaria a regra e nenhuma revisão de código pegaria isso
para sempre.

**As quatro regras de agregado**, na prática:

1. **Referência externa só à raiz.** Nada guarda um `ItemDePedido` solto. Se você precisa
   mexer num item, você pede ao `Pedido`.
2. **Uma transação, um agregado.** Cada `save()` grava um agregado inteiro. Se uma operação
   precisa alterar dois agregados de forma atômica, isso é um sinal de que ou os limites
   estão errados, ou você deveria usar consistência eventual (evento de domínio).
3. **Referência entre agregados é por id, não por objeto.** Note `clienteId: ClienteId`, e
   não `cliente: Cliente`. Se `Pedido` segurasse o objeto `Cliente` inteiro, carregar um
   pedido carregaria o cliente, que carregaria os endereços, que carregariam... e você
   acabou de trazer meio banco para a memória. Isso também mantém os limites de transação
   claros.
4. **A raiz é a unidade de bloqueio e de consistência.** Duas requisições simultâneas no
   mesmo `Pedido` competem; requisições em pedidos diferentes não.

Como decidir o que entra num agregado? Pergunte: **essas duas coisas precisam estar corretas
no mesmo instante, ou uma pode ficar alguns segundos atrás?** Item e Pedido precisam
(o total tem que bater). Pedido e Estoque não precisam (dá para reservar estoque de forma
assíncrona). Portanto Item entra no agregado Pedido, e Estoque é outro agregado.

Um erro frequente é criar agregados grandes demais, tipo `Cliente` contendo todos os pedidos
de todos os tempos. Carregar isso é inviável e a contenção de escrita fica absurda. Na
dúvida, **prefira agregados pequenos**.

---

### 4.4 Domain Service

Às vezes uma regra é claramente de negócio, mas não pertence a nenhuma entidade. Se você
forçar, fica arbitrário: transferência entre contas pertence à conta de origem ou à de
destino?

Nesse caso, crie um **Domain Service**: um objeto sem estado, que opera sobre entidades.

```ts
// domain/services/PoliticaDeDesconto.ts
export class PoliticaDeDesconto {
  calcular(pedido: Pedido, cliente: Cliente): Dinheiro {
    let percentual = 0;
    if (cliente.ehVip())                          percentual += 0.10;
    if (pedido.total().maiorQue(Dinheiro.reais(500))) percentual += 0.05;
    if (cliente.temMaisDeAnosDeCasa(3))           percentual += 0.03;
    return pedido.total().multiplicar(Math.min(percentual, 0.15));
  }
}
```

Cuidado com a armadilha: **Domain Service não é o mesmo que Application Service**. A
diferença:

| | Domain Service | Application Service (caso de uso) |
|---|---|---|
| Contém | Regra de negócio | Orquestração |
| Faz I/O | Nunca | Sim (via ports) |
| Conhece transação | Não | Sim |
| Exemplo | `PoliticaDeDesconto` | `FinalizarPedido` |
| Camada | `domain/` | `application/` |

Se o seu "domain service" chama repositório, ele não é domain service, é caso de uso mal
colocado. Use Domain Service com parcimônia: se você tem vinte deles e entidades anêmicas,
o comportamento fugiu do lugar.

---

### 4.5 Domain Events

Um evento de domínio registra que **algo relevante para o negócio aconteceu**, no passado.
O nome é sempre no particípio: `PedidoFinalizado`, `UsuarioBloqueado`, `PagamentoRecusado`.

```ts
// domain/events/PedidoFinalizado.ts
export class PedidoFinalizado {
  readonly ocorridoEm: Date;
  constructor(
    public readonly pedidoId: PedidoId,
    public readonly clienteId: ClienteId,
    public readonly total: Dinheiro,
    ocorridoEm: Date,
  ) { this.ocorridoEm = ocorridoEm; }
}
```

A entidade apenas **registra** o evento; ela não o publica (publicar é I/O):

```ts
export abstract class AggregateRoot {
  private _eventos: object[] = [];
  protected registrarEvento(e: object) { this._eventos.push(e); }
  puxarEventos(): object[] { const e = this._eventos; this._eventos = []; return e; }
}

export class Pedido extends AggregateRoot {
  finalizar(agora: Date): void {
    // ...validações...
    this._status = StatusPedido.AguardandoPagamento;
    this.registrarEvento(new PedidoFinalizado(this.id, this.clienteId, this.total(), agora));
  }
}
```

Quem publica é o caso de uso, depois de a transação ter sido confirmada:

```ts
await this.repo.salvar(pedido);
await this.publicador.publicar(pedido.puxarEventos());
```

Por que isso vale a pena: sem eventos, `FinalizarPedido` acaba conhecendo e-mail, nota
fiscal, estoque, antifraude e programa de pontos. Cada nova reação ao pedido finalizado
edita o mesmo caso de uso, e ele cresce sem fim. Com eventos, ele só anuncia o fato, e cada
reação vive num handler separado que ninguém precisa tocar quando as outras mudam.

Não comece com eventos no primeiro dia. Introduza quando um caso de uso começar a acumular
efeitos colaterais não relacionados entre si.

---

### 4.6 Erros de domínio

Regra violada não é `Error('erro')`. Crie tipos que o resto do sistema possa distinguir:

```ts
// domain/errors/DomainError.ts
export abstract class DomainError extends Error {
  abstract readonly codigo: string;
}

export class SaldoInsuficiente extends DomainError {
  readonly codigo = 'SALDO_INSUFICIENTE';
  constructor(readonly disponivel: Dinheiro, readonly solicitado: Dinheiro) {
    super(`Saldo insuficiente: disponível ${disponivel}, solicitado ${solicitado}`);
  }
}
```

Repare no que **não** está aqui: nenhum status HTTP. O domínio não sabe que existe HTTP.
Mapear `SaldoInsuficiente` para `422` é trabalho do controller, e um sistema CLI mapearia o
mesmo erro para um código de saída. Colocar `statusCode = 422` na classe de erro é um
vazamento clássico e muito tentador.

---

### 4.7 Resumo da camada Domain

- Não importa nada de fora. Zero dependências de framework.
- Value Objects tornam estados inválidos impossíveis de representar.
- Entidades protegem invariantes com métodos, não com setters.
- Agregados definem fronteiras de consistência e de transação.
- Domain Services guardam regras que não cabem numa entidade só.
- Eventos desacoplam consequências.
- Erros são tipados e ignoram protocolo.
- Se está difícil testar sem mock, algo de fora entrou aqui.

---

## Parte 5 — Camada Application

Se o domínio responde "**quais são as regras**", a aplicação responde "**o que este sistema
faz**". Abrir a pasta `application/` de um projeto bem feito deve ser como ler o índice do
manual do produto:

```
application/
└── use-cases/
    ├── AbrirPedido.ts
    ├── AdicionarItemAoPedido.ts
    ├── FinalizarPedido.ts
    ├── CancelarPedido.ts
    └── ConsultarPedido.ts
```

Alguém que nunca viu o sistema entende o que ele faz em dez segundos. Compare com
`services/PedidoService.ts` com 1.200 linhas e dezoito métodos públicos.

### O papel exato: orquestração

Um caso de uso é um **maestro**. Ele não toca instrumento; ele coordena. O roteiro é quase
sempre o mesmo:

```
1. Receber dados de entrada (já como tipos simples, sem HTTP)
2. Carregar os agregados necessários pelos ports de saída
3. Verificar existência e autorização
4. Chamar os métodos de domínio  ← aqui, e só aqui, mora a regra
5. Persistir o resultado
6. Publicar eventos / disparar efeitos
7. Devolver um DTO de saída
```

O passo 4 é o único que decide algo sobre o negócio, e ele é uma chamada de método. Se o
seu caso de uso tem `if` sobre regra de negócio, essa regra provavelmente devia estar numa
entidade.

### Um caso de uso completo

```ts
// application/use-cases/FinalizarPedido.ts
import { RepositorioDePedidos } from '../ports/RepositorioDePedidos';
import { RepositorioDeClientes } from '../ports/RepositorioDeClientes';
import { GatewayDePagamento } from '../ports/GatewayDePagamento';
import { PublicadorDeEventos } from '../ports/PublicadorDeEventos';
import { Relogio } from '../ports/Relogio';
import { PoliticaDeDesconto } from '../../domain/services/PoliticaDeDesconto';

export interface FinalizarPedidoInput {
  pedidoId: string;
  tokenDoCartao: string;
}

export interface FinalizarPedidoOutput {
  pedidoId: string;
  total: number;
  desconto: number;
  statusPagamento: 'APROVADO' | 'RECUSADO';
}

export class FinalizarPedido {
  constructor(
    private readonly pedidos: RepositorioDePedidos,
    private readonly clientes: RepositorioDeClientes,
    private readonly pagamento: GatewayDePagamento,
    private readonly eventos: PublicadorDeEventos,
    private readonly relogio: Relogio,
    private readonly politica: PoliticaDeDesconto,
  ) {}

  async executar(input: FinalizarPedidoInput): Promise<FinalizarPedidoOutput> {
    // 2. carregar
    const pedido = await this.pedidos.porId(PedidoId.de(input.pedidoId));
    if (!pedido) throw new PedidoNaoEncontrado(input.pedidoId);

    const cliente = await this.clientes.porId(pedido.clienteId);
    if (!cliente) throw new ClienteNaoEncontrado(pedido.clienteId.valor);

    // 4. regra de negócio — delegada ao domínio
    const desconto = this.politica.calcular(pedido, cliente);
    pedido.finalizar(this.relogio.agora());

    // 4/5. efeito externo
    const cobranca = await this.pagamento.cobrar({
      valor: pedido.total().subtrair(desconto),
      token: input.tokenDoCartao,
    });

    if (cobranca.recusada) {
      pedido.reverterFinalizacao(cobranca.motivo);
    } else {
      pedido.registrarPagamento(cobranca.id, this.relogio.agora());
    }

    // 5. persistir
    await this.pedidos.salvar(pedido);

    // 6. publicar
    await this.eventos.publicar(pedido.puxarEventos());

    // 7. devolver DTO
    return {
      pedidoId: pedido.id.valor,
      total: pedido.total().emReais(),
      desconto: desconto.emReais(),
      statusPagamento: cobranca.recusada ? 'RECUSADO' : 'APROVADO',
    };
  }
}
```

Repare no que **não** aparece: `req`, `res`, status HTTP, SQL, nome de tabela, `axios`,
`new Date()`, `process.env`. O arquivo é 100% legível por alguém que nunca viu o projeto, e
100% testável sem infraestrutura nenhuma.

### Ports: as interfaces que o núcleo declara

```ts
// application/ports/RepositorioDePedidos.ts
export interface RepositorioDePedidos {
  porId(id: PedidoId): Promise<Pedido | null>;
  salvar(pedido: Pedido): Promise<void>;
  proximoId(): PedidoId;
}

// application/ports/GatewayDePagamento.ts
export interface GatewayDePagamento {
  cobrar(cmd: { valor: Dinheiro; token: string }): Promise<ResultadoDaCobranca>;
}

// application/ports/Relogio.ts
export interface Relogio {
  agora(): Date;
}

// application/ports/PublicadorDeEventos.ts
export interface PublicadorDeEventos {
  publicar(eventos: object[]): Promise<void>;
}
```

Quatro observações importantes sobre ports.

**Port fala a língua do domínio, não a da tecnologia.** Repare que `GatewayDePagamento` não
tem `chargeStripeCustomer` nem `idempotencyKey`. Se o nome do provedor aparece na interface,
o vazamento já aconteceu: trocar Stripe por Pagar.me exigiria mudar o núcleo, que é
exatamente o que a interface deveria evitar.

**Port é escrito para quem consome, não para quem implementa.** Isso é o
*Interface Segregation Principle*. Se `RepositorioDePedidos` tem trinta métodos porque o
Postgres suporta trinta operações, está errado. Ele deve ter exatamente os métodos que os
casos de uso usam, e nada mais.

**`Relogio` parece exagero, mas não é.** É o port que mais paga por si mesmo. Com ele, você
testa "cobrar multa após 30 dias" em três linhas, sem `sleep`, sem manipular o relógio da
máquina de CI, sem flakiness. Vale o mesmo para geração de UUID e de números aleatórios.
Tudo que é não-determinístico deve ser um port.

**Onde declarar o port: `domain/` ou `application/`?** As duas escolhas aparecem em projetos
sérios. Regra de bolso: se a abstração é sobre um conceito do negócio (um repositório de um
agregado), pode ficar em `domain/ports/`; se é sobre uma capacidade técnica que o caso de uso
precisa (enviar e-mail, publicar evento, obter hora), fica em `application/ports/`. O que
não pode, em hipótese alguma, é o port estar em `infrastructure/`. Isso desfaz a inversão
inteira. Escolha uma convenção e mantenha no projeto todo.

### Input e Output: por que DTOs

Os casos de uso recebem e devolvem estruturas simples, não entidades. Motivos:

1. **Não vazar o domínio para fora.** Se o caso de uso devolve `Pedido`, o controller
   serializa `Pedido` direto no JSON. Aí renomear um campo privado da entidade quebra o
   contrato público da API, e ninguém percebe até o app mobile parar.
2. **Controlar o que é exposto.** A entidade `Usuario` tem `senhaHash`. O DTO não tem.
3. **Estabilizar o contrato.** O DTO pode ficar igual enquanto o domínio é refatorado.

Nota importante: o DTO de entrada do caso de uso **não é o body do request**. O body vem com
campos do protocolo, com nomes em inglês vindos do front, com `undefined`. Converter body em
input é trabalho do controller. Isso é chato e parece redundante nas primeiras semanas, e é
o que permite que o mesmo caso de uso atenda HTTP, fila e CLI sem alteração.

### Transações e Unit of Work

Onde fica o `BEGIN` / `COMMIT`? A transação é um conceito de infraestrutura, mas **o limite
dela é uma decisão de negócio**: qual conjunto de operações precisa ser tudo ou nada. Quem
sabe isso é o caso de uso.

A saída é um port que expressa o limite sem expressar a tecnologia:

```ts
// application/ports/UnitOfWork.ts
export interface UnitOfWork {
  executarEmTransacao<T>(operacao: () => Promise<T>): Promise<T>;
}

// no caso de uso
await this.uow.executarEmTransacao(async () => {
  await this.pedidos.salvar(pedido);
  await this.estoque.reservar(itens);
});
```

O caso de uso diz "isto é atômico". O adapter Postgres decide que isso significa
`BEGIN/COMMIT`. Um adapter em memória decide que não significa nada. Nenhum dos dois
conhecimentos vaza para o outro lado.

Alternativa comum e igualmente válida: colocar a transação num *decorator* que envolve o
caso de uso, deixando o caso de uso completamente ignorante disso. Escolha uma e seja
consistente.

### Autorização

Existe uma distinção que confunde:

- **Autenticação** ("quem é você") é infraestrutura. Validar JWT, ler cookie, checar
  assinatura: tudo isso é do adapter de entrada.
- **Autorização** ("você pode fazer isso") frequentemente é regra de negócio: "só o dono do
  pedido ou um gerente pode cancelar" é uma regra que existiria no papel.

Na prática, o controller autentica e passa um objeto simples de identidade para o caso de
uso, que aplica a regra:

```ts
async executar(input: { pedidoId: string; solicitante: Identidade }) {
  const pedido = await this.pedidos.porId(...);
  if (!pedido.podeSerCanceladoPor(input.solicitante)) throw new NaoAutorizado();
}
```

### Leitura: a exceção pragmática

Casos de uso de leitura ("listar pedidos com filtro e paginação para uma tela") passam por
todo o cerimonial de carregar agregados, mapear e devolver DTO. Isso é caro e, para leitura,
não compra quase nada: **não há invariante a proteger quando você não altera nada.**

A saída pragmática, e que é a porta de entrada natural para CQRS, é ter um caminho de leitura
separado:

```ts
// application/queries/ListarPedidosDoCliente.ts
export interface ConsultaDePedidos {   // port de leitura
  listarPorCliente(clienteId: string, pagina: number): Promise<LinhaDePedido[]>;
}
```

O adapter dessa interface pode fazer um SQL direto, com join, devolvendo exatamente as
colunas da tela, sem instanciar um único agregado. Você continua respeitando a regra da
dependência (a interface está dentro, o SQL está fora) e economiza muito código.

**A escrita passa pelo domínio; a leitura pode ir direto.** Essa é uma das decisões que mais
reduz o atrito de arquitetura limpa em CRUD-com-relatórios.

### O que NÃO é responsabilidade da aplicação

| Não é papel dela | De quem é |
|---|---|
| Ler `req.body`, montar `res` | Adapter de entrada |
| Escolher status HTTP | Adapter de entrada |
| Saber SQL, ORM, nome de tabela | Adapter de saída |
| Conter regra de negócio complexa | Domain |
| Ler variável de ambiente | Composition root |
| Formatar data para exibição | Adapter de entrada |
| Serializar JSON | Adapter de entrada |

---

## Parte 6 — Camada Infrastructure

Aqui mora tudo aquilo que a Parte 1 chamou de "detalhe substituível". É a camada mais
externa, a que pode conhecer todo mundo, e a que ninguém conhece.

Uma inversão mental que ajuda: em projetos tradicionais, o banco de dados é o centro do
sistema e tudo gira em torno do schema. Aqui, **o banco é um plugin**. É um lugar onde o
sistema decidiu jogar os bytes. Poderia ser outro. O sistema não muda.

### O que mora aqui

```
infrastructure/
├── persistence/
│   ├── postgres/
│   │   ├── PedidoRepositoryPostgres.ts     ← implementa port
│   │   ├── PedidoMapper.ts                 ← tradutor tabela ↔ domínio
│   │   ├── schema.sql
│   │   └── migrations/
│   └── in-memory/
│       └── PedidoRepositoryEmMemoria.ts    ← implementa o MESMO port
├── gateways/
│   ├── StripeGatewayDePagamento.ts
│   └── ViaCepGatewayDeEndereco.ts
├── messaging/
│   ├── RabbitPublicadorDeEventos.ts
│   └── consumers/
├── notifications/
│   └── SendGridEnviadorDeEmail.ts
├── time/
│   └── RelogioDoSistema.ts
├── http/                                    ← adapters de ENTRADA (Parte 7)
│   ├── controllers/
│   ├── middlewares/
│   └── rotas.ts
└── config/
    └── env.ts
```

### 6.1 Repositório: a implementação

```ts
// infrastructure/persistence/postgres/PedidoRepositoryPostgres.ts
import { RepositorioDePedidos } from '../../../application/ports/RepositorioDePedidos';

export class PedidoRepositoryPostgres implements RepositorioDePedidos {
  constructor(private readonly db: Pool) {}

  async porId(id: PedidoId): Promise<Pedido | null> {
    const pedido = await this.db.query(
      'SELECT id, cliente_id, status FROM pedidos WHERE id = $1', [id.valor],
    );
    if (pedido.rowCount === 0) return null;

    const itens = await this.db.query(
      'SELECT produto_id, quantidade, preco_centavos FROM itens_pedido WHERE pedido_id = $1',
      [id.valor],
    );

    return PedidoMapper.paraDominio(pedido.rows[0], itens.rows);
  }

  async salvar(pedido: Pedido): Promise<void> {
    const linha = PedidoMapper.paraPersistencia(pedido);
    await this.db.query(
      `INSERT INTO pedidos (id, cliente_id, status) VALUES ($1,$2,$3)
       ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status`,
      [linha.id, linha.cliente_id, linha.status],
    );
    await this.db.query('DELETE FROM itens_pedido WHERE pedido_id = $1', [linha.id]);
    for (const item of linha.itens) {
      await this.db.query(
        `INSERT INTO itens_pedido (pedido_id, produto_id, quantidade, preco_centavos)
         VALUES ($1,$2,$3,$4)`,
        [linha.id, item.produto_id, item.quantidade, item.preco_centavos],
      );
    }
  }

  proximoId(): PedidoId {
    return PedidoId.de(randomUUID());
  }
}
```

Detalhe frequentemente ignorado: **o repositório salva o agregado inteiro**, incluindo os
itens. Ele não expõe `salvarItem`. Isso é o que faz a regra "uma transação, um agregado"
ser real e não apenas uma boa intenção.

Outro detalhe: `proximoId()` fica no repositório. Isso permite que o caso de uso obtenha o
id **antes** de salvar, o que evita entidades meio-construídas com `id: null`. Se o banco
gera o id com auto-increment, você fica com um objeto de domínio inválido até o commit, e
isso contamina o modelo inteiro. Prefira UUID gerado pela aplicação.

### 6.2 Mapper: por que a tabela não é a entidade

Este é um dos pontos onde mais gente desiste da arquitetura, então vale insistir.

A tentação é usar a entidade do ORM como entidade de domínio. Uma classe só, menos código.
O que dá errado:

- Anotações de ORM (`@Entity`, `@Column`, `@ManyToOne`) são um import de infraestrutura
  dentro do domínio. A seta apontou para fora.
- O ORM exige construtor vazio e campos públicos, o que impede o construtor privado, os
  Value Objects e o encapsulamento inteiro da Parte 4.
- A modelagem relacional otimiza normalização e índices; a modelagem de domínio otimiza
  expressividade de regra. São objetivos diferentes e a mesma classe não serve bem aos dois.
- *Lazy loading* faz um getter aparentemente inocente disparar um SELECT. Sua regra de
  negócio virou I/O silencioso.

O Mapper é o preço de manter os dois mundos separados:

```ts
// infrastructure/persistence/postgres/PedidoMapper.ts
export class PedidoMapper {
  static paraDominio(linha: LinhaPedido, itens: LinhaItem[]): Pedido {
    return Pedido.reconstituir({
      id: PedidoId.de(linha.id),
      clienteId: ClienteId.de(linha.cliente_id),
      status: StatusPedido[linha.status],
      itens: itens.map(i => ItemDePedido.reconstituir(
        ProdutoId.de(i.produto_id),
        i.quantidade,
        Dinheiro.deCentavos(i.preco_centavos),
      )),
    });
  }

  static paraPersistencia(pedido: Pedido): LinhaPedido & { itens: LinhaItem[] } {
    return {
      id: pedido.id.valor,
      cliente_id: pedido.clienteId.valor,
      status: pedido.status.toString(),
      itens: pedido.itens.map(i => ({
        produto_id: i.produtoId.valor,
        quantidade: i.quantidade,
        preco_centavos: i.precoUnitario.centavos,
      })),
    };
  }
}
```

Sim, é código repetitivo. É a taxa que você paga para poder renomear uma coluna sem tocar
no domínio, e para poder mudar uma regra sem escrever migration. Em projetos com domínio
simples, essa taxa não compensa (Parte 14). Em projetos com domínio rico, compensa muito.

Um caminho intermediário: usar o ORM apenas em modo "data mapper" com classes de persistência
separadas (`PedidoRecord`), mantendo o Mapper. Você ganha o ORM para queries e migrations e
mantém o domínio limpo.

### 6.3 O adapter em memória

O irmão gêmeo do repositório Postgres, e provavelmente a peça de infraestrutura que você
mais vai usar no dia a dia:

```ts
// infrastructure/persistence/in-memory/PedidoRepositoryEmMemoria.ts
export class PedidoRepositoryEmMemoria implements RepositorioDePedidos {
  private readonly dados = new Map<string, Pedido>();

  async porId(id: PedidoId): Promise<Pedido | null> {
    return this.dados.get(id.valor) ?? null;
  }

  async salvar(pedido: Pedido): Promise<void> {
    this.dados.set(pedido.id.valor, pedido);
  }

  proximoId(): PedidoId {
    return PedidoId.de(randomUUID());
  }
}
```

Doze linhas que substituem um Docker Compose inteiro na suíte de testes. Aqui está,
concretamente, o retorno do investimento na interface: com um teste rodando em 8ms em vez de
800ms, você roda a suíte a cada save, e isso muda como você programa.

Cuidado com uma armadilha: se o fake em memória mente (aceita algo que o Postgres rejeitaria,
como violação de unicidade), seus testes ficam verdes e a produção quebra. A defesa é o
**teste de contrato**, na Parte 10.

### 6.4 Gateway de serviço externo

```ts
// infrastructure/gateways/StripeGatewayDePagamento.ts
export class StripeGatewayDePagamento implements GatewayDePagamento {
  constructor(private readonly stripe: Stripe) {}

  async cobrar(cmd: { valor: Dinheiro; token: string }): Promise<ResultadoDaCobranca> {
    try {
      const intent = await this.stripe.paymentIntents.create({
        amount: cmd.valor.centavos,          // tradução de conceito
        currency: cmd.valor.moeda.toLowerCase(),
        payment_method: cmd.token,
        confirm: true,
      });
      return { recusada: false, id: intent.id };
    } catch (e) {
      if (e instanceof Stripe.errors.StripeCardError) {
        // traduz erro do fornecedor para vocabulário do domínio
        return { recusada: true, motivo: MotivoDaRecusa.CartaoRecusado };
      }
      throw new FalhaNoProvedorDePagamento(e); // erro de infra, não de negócio
    }
  }
}
```

O adapter tem três trabalhos, sempre os mesmos:

1. **Traduzir vocabulário.** `Dinheiro` vira `amount` + `currency`.
2. **Traduzir erros.** `StripeCardError` vira `MotivoDaRecusa.CartaoRecusado`. O núcleo nunca
   deve capturar uma exceção do SDK de terceiro; se isso acontecer, o vazamento é total.
3. **Absorver o detalhe técnico.** Retry, timeout, chave de idempotência, rate limit,
   autenticação. Nada disso interessa ao caso de uso.

Uma distinção que vale internalizar: **erro de negócio** (cartão recusado) faz parte do
fluxo esperado e volta como valor de retorno. **Erro de infraestrutura** (Stripe fora do ar)
é excepcional e sobe como exceção. Misturar os dois produz casos de uso cheios de try/catch
sem semântica.

### 6.5 O adapter de tempo

```ts
// infrastructure/time/RelogioDoSistema.ts
export class RelogioDoSistema implements Relogio {
  agora(): Date { return new Date(); }
}

// para teste
export class RelogioCongelado implements Relogio {
  constructor(private instante: Date) {}
  agora(): Date { return this.instante; }
  avancarDias(n: number) { this.instante = addDays(this.instante, n); }
}
```

Trivial de escrever, e transforma "testar cobrança de multa após vencimento" de um problema
insolúvel em um teste de cinco linhas.

### 6.6 Configuração

Variáveis de ambiente são lidas **num único lugar**, na borda:

```ts
// infrastructure/config/env.ts
export const env = {
  databaseUrl: obrigatorio('DATABASE_URL'),
  stripeKey:   obrigatorio('STRIPE_SECRET_KEY'),
  porta:       Number(process.env.PORT ?? 3000),
};

function obrigatorio(chave: string): string {
  const v = process.env[chave];
  if (!v) throw new Error(`Variável de ambiente ausente: ${chave}`);
  return v;
}
```

`process.env` espalhado pelo código é uma dependência global escondida: o caso de uso passa
a depender de estado de ambiente sem declarar isso em lugar nenhum. Se um caso de uso precisa
de um valor de configuração (por exemplo, o limite máximo de itens), esse valor deve chegar
**pelo construtor**, e a decisão de que ele vem do ambiente é do composition root.

---

## Parte 7 — Camada de Entrada (driving adapters)

Alguns projetos chamam de `presentation/`, outros de `interfaces/`, outros colocam dentro de
`infrastructure/http/`. O nome não importa; a natureza sim: **é infraestrutura**, porque HTTP
é uma tecnologia tão substituível quanto Postgres.

### O controller

```ts
// infrastructure/http/controllers/PedidoController.ts
export class PedidoController {
  constructor(private readonly finalizarPedido: FinalizarPedido) {}

  finalizar = async (req: Request, res: Response) => {
    try {
      // 1. traduzir HTTP → input do caso de uso
      const input = {
        pedidoId: req.params.id,
        tokenDoCartao: String(req.body.card_token ?? ''),
      };

      // 2. delegar. UMA linha.
      const saida = await this.finalizarPedido.executar(input);

      // 3. traduzir output → HTTP
      return res.status(200).json({
        order_id: saida.pedidoId,
        total_amount: saida.total,
        payment_status: saida.statusPagamento,
      });
    } catch (e) {
      return tratarErro(e, res);
    }
  };
}
```

O controller tem exatamente três responsabilidades: **traduzir entrada, delegar, traduzir
saída**. Se ele tem um `if` de regra de negócio, essa regra está no lugar errado e não vai
funcionar quando o mesmo fluxo for chamado por uma fila.

Uma heurística de revisão: um controller com mais de 15 linhas de corpo geralmente está
fazendo algo que não é dele.

### Tradução de erro em protocolo

Este é o único lugar do sistema que sabe o que é um 404:

```ts
// infrastructure/http/errorHandler.ts
export function tratarErro(e: unknown, res: Response) {
  if (e instanceof PedidoNaoEncontrado)  return res.status(404).json({ code: e.codigo });
  if (e instanceof PedidoNaoEditavel)    return res.status(409).json({ code: e.codigo });
  if (e instanceof DomainError)          return res.status(422).json({ code: e.codigo, message: e.message });
  if (e instanceof NaoAutorizado)        return res.status(403).json({ code: 'FORBIDDEN' });

  logger.error(e);
  return res.status(500).json({ code: 'INTERNAL_ERROR' }); // nunca vaze stack para o cliente
}
```

Se amanhã o sistema virar gRPC, este arquivo é reescrito e nada mais.

### O mesmo caso de uso, três portas de entrada

Aqui a arquitetura mostra o serviço:

```ts
// HTTP
app.post('/pedidos/:id/finalizar', controller.finalizar);

// Consumidor de fila
fila.on('finalizar-pedido', async (msg) =>
  finalizarPedido.executar({ pedidoId: msg.orderId, tokenDoCartao: msg.token }));

// CLI
program.command('finalizar <id> <token>').action((id, token) =>
  finalizarPedido.executar({ pedidoId: id, tokenDoCartao: token }));

// Teste
it('finaliza pedido', () => finalizarPedido.executar({ pedidoId: 'p1', tokenDoCartao: 'tok' }));
```

Quatro adapters de entrada, uma implementação de regra. No código da Parte 1, isso teria
sido copiar e colar quatro vezes.

### Validação: onde fica o quê

Uma dúvida constante. A resposta é que existem **dois tipos diferentes de validação** e cada
um mora num lugar:

| Tipo | Exemplo | Onde | Por quê |
|---|---|---|---|
| **De formato / entrada** | `quantidade` precisa ser um número; `email` é obrigatório no body | Controller (Zod, class-validator, DTO) | É sobre o protocolo, não sobre o negócio |
| **De negócio** | Máximo de 50 itens por pedido; e-mail precisa ser único | Domain / Application | É verdade mesmo fora do HTTP |

O controller rejeita lixo sintático antes de incomodar o núcleo. O núcleo aplica as regras
reais, e continua se protegendo mesmo se o controller falhar. Sim, há alguma sobreposição
(o núcleo revalida coisas que o controller já checou), e isso é proposital: **o domínio nunca
confia em quem o chamou.**

---

## Parte 8 — O fluxo completo de uma requisição

Vale seguir um único dado atravessando todas as camadas e voltando. Cenário: um cliente
finaliza o pedido `abc-123` pelo app.

```
 ┌── 1 ──────────────────────────────────────────────────────────────────┐
 │ POST /pedidos/abc-123/finalizar                                       │
 │ { "card_token": "tok_visa" }                                          │
 └───────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼   INFRASTRUCTURE (entrada)
 ┌── 2 ──────────────────────────────────────────────────────────────────┐
 │ Middleware valida JWT → identidade                                    │
 │ Zod valida o formato do body                                          │
 │ PedidoController traduz:                                              │
 │    req.params.id  →  input.pedidoId                                   │
 │    req.body.card_token → input.tokenDoCartao                          │
 │ Neste ponto o HTTP ACABOU. Nada abaixo sabe que existe HTTP.          │
 └───────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼   APPLICATION
 ┌── 3 ──────────────────────────────────────────────────────────────────┐
 │ FinalizarPedido.executar(input)                                       │
 │   3.1  pedidos.porId(...)      ── chama PORT de saída ────┐           │
 └───────────────────────────────────────────────────────────┼───────────┘
                                                             ▼  INFRA (saída)
 ┌── 4 ──────────────────────────────────────────────────────────────────┐
 │ PedidoRepositoryPostgres: SELECT ... FROM pedidos WHERE id = $1       │
 │ PedidoMapper.paraDominio(linhas) → objeto Pedido, com Value Objects   │
 │ Devolve um agregado válido. O SQL morre aqui e não sobe.              │
 └───────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼   DOMAIN
 ┌── 5 ──────────────────────────────────────────────────────────────────┐
 │ politica.calcular(pedido, cliente)   → Dinheiro(desconto)             │
 │ pedido.finalizar(agora)                                               │
 │    ├── itens.length === 0?      → lança PedidoVazio                   │
 │    ├── total > 50.000?          → lança PedidoAcimaDoLimite           │
 │    ├── status = AguardandoPagamento                                   │
 │    └── registra evento PedidoFinalizado                               │
 │ Zero I/O. Puro cálculo e decisão. Testável em memória.                │
 └───────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼   APPLICATION → PORT → INFRA
 ┌── 6 ──────────────────────────────────────────────────────────────────┐
 │ pagamento.cobrar(...)  →  StripeGateway  →  api.stripe.com            │
 │ Erro do SDK é traduzido para MotivoDaRecusa (vocabulário do domínio)  │
 └───────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
 ┌── 7 ──────────────────────────────────────────────────────────────────┐
 │ pedidos.salvar(pedido)   → UPDATE + rewrite dos itens (transação)     │
 │ eventos.publicar(...)    → RabbitMQ                                   │
 └───────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼   APPLICATION
 ┌── 8 ──────────────────────────────────────────────────────────────────┐
 │ Monta FinalizarPedidoOutput (tipos simples: string, number)           │
 │ A entidade Pedido NÃO sai da aplicação.                               │
 └───────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼   INFRASTRUCTURE (entrada)
 ┌── 9 ──────────────────────────────────────────────────────────────────┐
 │ Controller traduz output → JSON com nomes públicos da API             │
 │ HTTP 200                                                              │
 └───────────────────────────────────────────────────────────────────────┘
```

Duas observações que resumem o documento inteiro:

**O HTTP existe só nos passos 1, 2 e 9.** O SQL existe só nos passos 4 e 7. O negócio existe
no passo 5. Cada tecnologia está confinada a uma faixa estreita e substituível.

**As setas de import contrariam as setas de execução.** A execução vai de fora para dentro e
volta. Os imports apontam só para dentro. Isso é a inversão de dependência funcionando.

---

## Parte 9 — Exemplo completo, end-to-end

Um sistema pequeno e inteiro: **empréstimo de livros numa biblioteca**. Escolhi este domínio
porque as regras são intuitivas e mesmo assim têm invariantes de verdade.

### As regras do negócio

1. Um membro pode ter no máximo 3 empréstimos ativos ao mesmo tempo.
2. Um exemplar emprestado não pode ser emprestado de novo.
3. O prazo de devolução é de 14 dias a partir do empréstimo.
4. Devolução em atraso gera multa de R$ 1,50 por dia, limitada a R$ 50,00.
5. Membro com multa em aberto acima de R$ 20,00 não pode pegar novos empréstimos.
6. Ao emprestar, o membro recebe um e-mail com a data de devolução.

Repare que **as seis regras seriam idênticas numa biblioteca de fichas de papel**. Isso é o
teste da Parte 4 passando. Nenhuma delas menciona banco, HTTP ou e-mail (a 6 menciona
e-mail, mas o que é de negócio é "notificar o membro"; o canal é detalhe).

### Estrutura de pastas

```
src/
├── domain/
│   ├── entities/
│   │   ├── Emprestimo.ts
│   │   ├── Membro.ts
│   │   └── Exemplar.ts
│   ├── value-objects/
│   │   ├── Dinheiro.ts
│   │   ├── Isbn.ts
│   │   └── Ids.ts
│   ├── errors/
│   │   └── DomainErrors.ts
│   └── events/
│       └── LivroEmprestado.ts
│
├── application/
│   ├── ports/
│   │   ├── RepositorioDeEmprestimos.ts
│   │   ├── RepositorioDeMembros.ts
│   │   ├── RepositorioDeExemplares.ts
│   │   ├── EnviadorDeNotificacao.ts
│   │   └── Relogio.ts
│   └── use-cases/
│       ├── EmprestarLivro.ts
│       └── DevolverLivro.ts
│
├── infrastructure/
│   ├── persistence/
│   │   ├── postgres/…
│   │   └── in-memory/…
│   ├── notifications/SendGridNotificacao.ts
│   ├── time/RelogioDoSistema.ts
│   └── http/
│       ├── EmprestimoController.ts
│       └── rotas.ts
│
└── main.ts                       ← composition root
```

### Domain

```ts
// domain/value-objects/Ids.ts
export class MembroId {
  private constructor(readonly valor: string) {}
  static de(v: string) { if (!v) throw new IdInvalido('MembroId'); return new MembroId(v); }
  igualA(o: MembroId) { return this.valor === o.valor; }
}
// ExemplarId e EmprestimoId são análogos.
```

```ts
// domain/entities/Exemplar.ts
export class Exemplar {
  private constructor(
    readonly id: ExemplarId,
    readonly isbn: Isbn,
    readonly titulo: string,
    private _emprestado: boolean,
  ) {}

  static catalogar(id: ExemplarId, isbn: Isbn, titulo: string) {
    return new Exemplar(id, isbn, titulo, false);
  }
  static reconstituir(p: ExemplarProps) {
    return new Exemplar(p.id, p.isbn, p.titulo, p.emprestado);
  }

  marcarComoEmprestado(): void {
    if (this._emprestado) throw new ExemplarIndisponivel(this.id);   // REGRA 2
    this._emprestado = true;
  }

  marcarComoDisponivel(): void { this._emprestado = false; }

  get disponivel(): boolean { return !this._emprestado; }
}
```

```ts
// domain/entities/Membro.ts
export class Membro {
  private constructor(
    readonly id: MembroId,
    readonly nome: string,
    private readonly _email: Email,
    private _emprestimosAtivos: number,
    private _multaEmAberto: Dinheiro,
  ) {}

  static reconstituir(p: MembroProps) {
    return new Membro(p.id, p.nome, p.email, p.emprestimosAtivos, p.multaEmAberto);
  }

  private static readonly LIMITE_EMPRESTIMOS = 3;                       // REGRA 1
  private static readonly TETO_DE_MULTA = Dinheiro.reais(20);           // REGRA 5

  garantirQuePodePegarEmprestado(): void {
    if (this._emprestimosAtivos >= Membro.LIMITE_EMPRESTIMOS) {
      throw new LimiteDeEmprestimosAtingido(this.id, Membro.LIMITE_EMPRESTIMOS);
    }
    if (this._multaEmAberto.maiorQue(Membro.TETO_DE_MULTA)) {
      throw new MultaEmAbertoImpedeEmprestimo(this._multaEmAberto);
    }
  }

  registrarNovoEmprestimo(): void { this._emprestimosAtivos += 1; }
  registrarDevolucao(): void      { this._emprestimosAtivos -= 1; }
  acumularMulta(v: Dinheiro): void { this._multaEmAberto = this._multaEmAberto.somar(v); }

  get email(): Email { return this._email; }
  get multaEmAberto(): Dinheiro { return this._multaEmAberto; }
}
```

Repare em `garantirQuePodePegarEmprestado()`. Ela não devolve `boolean`, ela lança. A escolha
é deliberada: um `podePegar(): boolean` deixaria o chamador decidir qual mensagem de erro
dar, e dois chamadores dariam mensagens diferentes. Lançando o erro tipado, a razão da recusa
é definida pelo domínio, uma vez só.

```ts
// domain/entities/Emprestimo.ts  — raiz de agregado
export class Emprestimo extends AggregateRoot {
  private static readonly PRAZO_EM_DIAS = 14;                          // REGRA 3
  private static readonly MULTA_POR_DIA = Dinheiro.reais(1.5);         // REGRA 4
  private static readonly TETO_DA_MULTA  = Dinheiro.reais(50);         // REGRA 4

  private constructor(
    readonly id: EmprestimoId,
    readonly membroId: MembroId,
    readonly exemplarId: ExemplarId,
    readonly retiradoEm: Date,
    readonly devolverAte: Date,
    private _devolvidoEm: Date | null,
    private _multa: Dinheiro,
  ) { super(); }

  static abrir(
    id: EmprestimoId, membro: Membro, exemplar: Exemplar, agora: Date,
  ): Emprestimo {
    membro.garantirQuePodePegarEmprestado();   // REGRAS 1 e 5
    exemplar.marcarComoEmprestado();           // REGRA 2

    const prazo = new Date(agora);
    prazo.setDate(prazo.getDate() + Emprestimo.PRAZO_EM_DIAS);          // REGRA 3

    const emp = new Emprestimo(
      id, membro.id, exemplar.id, agora, prazo, null, Dinheiro.reais(0),
    );
    membro.registrarNovoEmprestimo();
    emp.registrarEvento(new LivroEmprestado(id, membro.id, exemplar.id, prazo, agora));
    return emp;
  }

  static reconstituir(p: EmprestimoProps) { /* ... */ }

  devolver(agora: Date): Dinheiro {
    if (this._devolvidoEm) throw new EmprestimoJaDevolvido(this.id);

    this._devolvidoEm = agora;
    this._multa = this.calcularMulta(agora);                            // REGRA 4
    return this._multa;
  }

  private calcularMulta(quando: Date): Dinheiro {
    const diasDeAtraso = Math.max(
      0, Math.ceil((quando.getTime() - this.devolverAte.getTime()) / 86_400_000),
    );
    if (diasDeAtraso === 0) return Dinheiro.reais(0);

    const bruta = Emprestimo.MULTA_POR_DIA.multiplicar(diasDeAtraso);
    return bruta.maiorQue(Emprestimo.TETO_DA_MULTA) ? Emprestimo.TETO_DA_MULTA : bruta;
  }

  get estaAtrasado(): boolean { return !this._devolvidoEm && new Date() > this.devolverAte; }
}
```

Este arquivo é o coração do sistema. Ele contém cinco das seis regras de negócio, não importa
nada, não faz I/O, e pode ser testado com `new` e `expect`.

### Application

```ts
// application/ports/RepositorioDeEmprestimos.ts
export interface RepositorioDeEmprestimos {
  porId(id: EmprestimoId): Promise<Emprestimo | null>;
  salvar(e: Emprestimo): Promise<void>;
  proximoId(): EmprestimoId;
}

// application/ports/EnviadorDeNotificacao.ts
export interface EnviadorDeNotificacao {
  notificarEmprestimo(para: Email, titulo: string, devolverAte: Date): Promise<void>;
}
```

`notificarEmprestimo` fala de negócio, não de canal. O adapter pode mandar e-mail, SMS ou
push; o caso de uso não muda.

```ts
// application/use-cases/EmprestarLivro.ts
export class EmprestarLivro {
  constructor(
    private readonly emprestimos: RepositorioDeEmprestimos,
    private readonly membros: RepositorioDeMembros,
    private readonly exemplares: RepositorioDeExemplares,
    private readonly notificacao: EnviadorDeNotificacao,
    private readonly relogio: Relogio,
  ) {}

  async executar(input: { membroId: string; exemplarId: string }) {
    const membro = await this.membros.porId(MembroId.de(input.membroId));
    if (!membro) throw new MembroNaoEncontrado(input.membroId);

    const exemplar = await this.exemplares.porId(ExemplarId.de(input.exemplarId));
    if (!exemplar) throw new ExemplarNaoEncontrado(input.exemplarId);

    // toda a regra acontece nesta linha
    const emprestimo = Emprestimo.abrir(
      this.emprestimos.proximoId(), membro, exemplar, this.relogio.agora(),
    );

    await this.emprestimos.salvar(emprestimo);
    await this.membros.salvar(membro);
    await this.exemplares.salvar(exemplar);

    await this.notificacao.notificarEmprestimo(     // REGRA 6
      membro.email, exemplar.titulo, emprestimo.devolverAte,
    );

    return {
      emprestimoId: emprestimo.id.valor,
      devolverAte: emprestimo.devolverAte.toISOString(),
    };
  }
}
```

Cinquenta linhas contando tudo, e nenhuma delas contém uma regra de negócio. O caso de uso
carrega, delega e grava. Se você tirasse `Emprestimo.abrir` e espalhasse aqueles `if` neste
arquivo, ele funcionaria igual hoje e seria muito pior de manter em um ano.

### Infrastructure

```ts
// infrastructure/http/EmprestimoController.ts
export class EmprestimoController {
  constructor(private readonly emprestarLivro: EmprestarLivro) {}

  emprestar = async (req: Request, res: Response) => {
    try {
      const saida = await this.emprestarLivro.executar({
        membroId: req.body.member_id,
        exemplarId: req.body.copy_id,
      });
      return res.status(201).json({ loan_id: saida.emprestimoId, due_date: saida.devolverAte });
    } catch (e) {
      if (e instanceof ExemplarIndisponivel)          return res.status(409).json({ code: e.codigo });
      if (e instanceof LimiteDeEmprestimosAtingido)   return res.status(422).json({ code: e.codigo });
      if (e instanceof MultaEmAbertoImpedeEmprestimo) return res.status(402).json({ code: e.codigo });
      if (e instanceof MembroNaoEncontrado)           return res.status(404).json({ code: e.codigo });
      throw e;
    }
  };
}
```

### Composition root

```ts
// main.ts
const pool = new Pool({ connectionString: env.databaseUrl });

const emprestimos = new EmprestimoRepositoryPostgres(pool);
const membros     = new MembroRepositoryPostgres(pool);
const exemplares  = new ExemplarRepositoryPostgres(pool);
const notificacao = new SendGridNotificacao(env.sendgridKey);
const relogio     = new RelogioDoSistema();

const emprestarLivro = new EmprestarLivro(
  emprestimos, membros, exemplares, notificacao, relogio,
);

const controller = new EmprestimoController(emprestarLivro);

const app = express();
app.use(express.json());
app.post('/emprestimos', controller.emprestar);
app.listen(env.porta);
```

### O mesmo sistema, testado sem nenhuma infraestrutura

```ts
// tests/EmprestarLivro.test.ts
describe('EmprestarLivro', () => {
  const montarCenario = () => {
    const emprestimos = new EmprestimoRepositoryEmMemoria();
    const membros     = new MembroRepositoryEmMemoria();
    const exemplares  = new ExemplarRepositoryEmMemoria();
    const notificacao = new NotificacaoFake();
    const relogio     = new RelogioCongelado(new Date('2026-01-10T10:00:00Z'));
    const uc = new EmprestarLivro(emprestimos, membros, exemplares, notificacao, relogio);
    return { uc, membros, exemplares, notificacao, relogio };
  };

  it('define devolução para 14 dias depois', async () => {
    const c = montarCenario();
    c.membros.adicionar(umMembro({ id: 'm1' }));
    c.exemplares.adicionar(umExemplar({ id: 'e1' }));

    const saida = await c.uc.executar({ membroId: 'm1', exemplarId: 'e1' });

    expect(saida.devolverAte).toBe('2026-01-24T10:00:00.000Z');
  });

  it('recusa o quarto empréstimo simultâneo', async () => {
    const c = montarCenario();
    c.membros.adicionar(umMembro({ id: 'm1', emprestimosAtivos: 3 }));
    c.exemplares.adicionar(umExemplar({ id: 'e1' }));

    await expect(c.uc.executar({ membroId: 'm1', exemplarId: 'e1' }))
      .rejects.toBeInstanceOf(LimiteDeEmprestimosAtingido);
  });

  it('recusa exemplar já emprestado', async () => {
    const c = montarCenario();
    c.membros.adicionar(umMembro({ id: 'm1' }));
    c.exemplares.adicionar(umExemplar({ id: 'e1', emprestado: true }));

    await expect(c.uc.executar({ membroId: 'm1', exemplarId: 'e1' }))
      .rejects.toBeInstanceOf(ExemplarIndisponivel);
  });
});
```

Sem Docker, sem banco, sem servidor HTTP, sem mock de biblioteca, sem `jest.mock`. Roda em
milissegundos e testa regra de negócio de verdade. **Esse é o retorno concreto do
investimento.** Compare com o que seria necessário para testar o código da Parte 1.

---

## Parte 10 — Testes em cada camada

A arquitetura muda o formato da pirâmide de testes, e para melhor.

| Camada | Tipo de teste | Precisa de infra? | Velocidade | Quantidade |
|---|---|---|---|---|
| Domain | Unitário puro | Não | microssegundos | Muitos |
| Application | Caso de uso com fakes | Não | milissegundos | Muitos |
| Adapters de saída | Teste de integração | Sim (banco real) | segundos | Poucos, um por adapter |
| Adapters de entrada | Teste de API | Sim (app subindo) | segundos | Poucos, os fluxos críticos |

### Teste de domínio

Nenhuma cerimônia. É só chamar método e verificar:

```ts
it('limita a multa em R$ 50 mesmo com 90 dias de atraso', () => {
  const emp = umEmprestimoVencidoEm('2026-01-01');
  const multa = emp.devolver(new Date('2026-04-01'));
  expect(multa.emReais()).toBe(50);
});
```

Se para escrever um teste de domínio você precisou de `jest.mock`, de um container ou de um
`beforeAll` grande, o domínio está contaminado. O teste é o sensor de acoplamento mais
confiável que existe: quando ele fica difícil, a culpa quase nunca é do teste.

### Teste de caso de uso: prefira fakes a mocks

Existe uma diferença que muda a qualidade da suíte inteira:

- **Mock**: você verifica que um método foi chamado. `expect(repo.salvar).toHaveBeenCalled()`.
  Testa **implementação**.
- **Fake**: uma implementação real e simplificada. Você verifica o efeito.
  `expect(await repo.porId('p1')).not.toBeNull()`. Testa **comportamento**.

Testes com mocks quebram quando você refatora sem mudar comportamento, o que é exatamente o
oposto do que se quer de um teste. Prefira fakes; use mock só para verificar interação com
sistemas que não têm estado observável (por exemplo, confirmar que um e-mail seria enviado).

Um fake útil registra o que recebeu:

```ts
export class NotificacaoFake implements EnviadorDeNotificacao {
  readonly enviadas: { para: string; titulo: string }[] = [];
  async notificarEmprestimo(para: Email, titulo: string) {
    this.enviadas.push({ para: para.valor, titulo });
  }
}

expect(notificacao.enviadas).toHaveLength(1);
expect(notificacao.enviadas[0].para).toBe('joao@exemplo.com');
```

### Teste de contrato: a peça que quase todo mundo esquece

Risco real: seu fake em memória e seu repositório Postgres se comportam diferente, os testes
passam e a produção quebra. A defesa é rodar **a mesma bateria de testes contra as duas
implementações**:

```ts
// tests/contract/RepositorioDeEmprestimos.contract.ts
export function testarContrato(
  nome: string,
  criar: () => Promise<RepositorioDeEmprestimos>,
) {
  describe(`RepositorioDeEmprestimos: ${nome}`, () => {
    it('devolve null para id inexistente', async () => {
      const repo = await criar();
      expect(await repo.porId(EmprestimoId.de('nao-existe'))).toBeNull();
    });

    it('persiste e recupera preservando a multa', async () => {
      const repo = await criar();
      const emp = umEmprestimo({ multa: Dinheiro.reais(7.5) });
      await repo.salvar(emp);
      const lido = await repo.porId(emp.id);
      expect(lido!.multa.emReais()).toBe(7.5);
    });
  });
}

// rodado duas vezes:
testarContrato('em memória', async () => new EmprestimoRepositoryEmMemoria());
testarContrato('postgres',   async () => new EmprestimoRepositoryPostgres(await poolDeTeste()));
```

A versão em memória roda em todo commit; a versão Postgres roda no CI. Se as duas passam, o
fake é confiável e você pode usá-lo com tranquilidade em centenas de testes rápidos.

Este teste também é onde você percebe erros de mapeamento: campo que não é persistido,
`Dinheiro` que perde centavos ao virar `float`, enum que volta como string errada.

---

## Parte 11 — Clean × Hexagonal × Onion × MVC × DDD

Cinco nomes que aparecem juntos e confundem. A relação real entre eles:

| Nome | Ano | Autor | Contribuição própria |
|---|---|---|---|
| **Hexagonal (Ports & Adapters)** | 2005 | Alistair Cockburn | A ideia de dentro/fora e das portas simétricas |
| **Onion Architecture** | 2008 | Jeffrey Palermo | Os anéis concêntricos, domínio no centro |
| **Clean Architecture** | 2012 | Robert C. Martin | Sintetiza as duas e nomeia a Regra da Dependência |
| **DDD** | 2003 | Eric Evans | Como modelar o dentro: entidade, VO, agregado, linguagem ubíqua |
| **MVC** | 1979 | Trygve Reenskaug | Separação de apresentação, não de arquitetura de sistema |

**Hexagonal, Onion e Clean são a mesma ideia com desenhos e ênfases diferentes.** Não vale a
pena discutir qual você está usando. Se as dependências apontam para dentro e o domínio não
conhece tecnologia, você está usando as três.

As pequenas diferenças de ênfase, se você quiser precisão:

- **Hexagonal** enfatiza a **simetria** entre entrada e saída. É a que melhor ensina o
  conceito de porta, e a razão de este documento começar por ela.
- **Onion** enfatiza os **anéis** e a posição do domínio no centro.
- **Clean** enfatiza a **regra formal** de dependência e nomeia as camadas (Entities, Use
  Cases, Interface Adapters, Frameworks & Drivers).

**DDD não é concorrente de nenhuma delas.** DDD é sobre modelagem: como conversar com
especialistas do negócio, descobrir os *bounded contexts*, escolher agregados. Clean é sobre
organização de dependências. Você pode fazer Clean com domínio anêmico (e muita gente faz),
e pode fazer DDD tático dentro de uma arquitetura mal organizada. Juntos funcionam melhor.

**MVC opera numa escala diferente.** MVC responde "como separo apresentação de lógica";
Clean responde "como organizo o sistema inteiro". Num sistema Clean, o MVC inteiro
(controller e view) vive dentro do anel externo. O "Model" do MVC de framework, aquele que
herda de `ActiveRecord`, é infraestrutura, não é o domínio.

Sobre a Clean Architecture original: ela desenha quatro anéis e usa nomes específicos. O
mapeamento para o que este documento chamou de camadas:

| Clean original | Aqui |
|---|---|
| Entities | `domain/` |
| Use Cases | `application/` |
| Interface Adapters | `infrastructure/http/`, mappers, presenters |
| Frameworks & Drivers | `infrastructure/` (banco, SDKs, Express) |

Três camadas é o que a maioria dos projetos usa na prática, e é suficiente.

---

## Parte 12 — Anti-padrões e armadilhas

Os erros abaixo aparecem em quase todo projeto que adota isso pela primeira vez.

### 1. Arquitetura de fachada

As pastas existem, a regra não. Você abre `domain/Usuario.ts` e encontra
`import { Column } from 'typeorm'`. Pastas bonitas não são arquitetura. Só há arquitetura se
existir uma restrição que impede a dependência errada. Automatize isso (Parte 16).

### 2. Ports declarados na infraestrutura

Interface `RepositorioDePedidos` dentro de `infrastructure/`. O caso de uso importa de lá, e
a seta voltou a apontar para fora. Toda a inversão foi desfeita, com o agravante de que
parece certo. **O port sempre mora dentro.**

### 3. Vazamento de vocabulário no port

```ts
interface RepositorioDeUsuarios {
  findOneOrFail(options: FindOneOptions<UsuarioEntity>): Promise<UsuarioEntity>;
  createQueryBuilder(alias: string): SelectQueryBuilder<UsuarioEntity>;
}
```

A interface existe, mas ela é o TypeORM com outro nome. Se trocar o ORM te obriga a mudar o
núcleo, a abstração não abstraiu nada. O mesmo vale para port de pagamento que aceita
`stripeCustomerId`.

### 4. Entidade do ORM usada como entidade de domínio

Discutido na Parte 6.2. É a decisão que mais frequentemente esvazia o benefício inteiro. Se
você optar por isso conscientemente por pragmatismo, tudo bem, mas saiba que seu domínio
agora depende da infraestrutura e que o encapsulamento da Parte 4 fica indisponível.

### 5. Caso de uso com regra de negócio

```ts
// ❌ no caso de uso
if (pedido.itens.length === 0) throw new Error('vazio');
if (pedido.status !== 'RASCUNHO') throw new Error('não editável');
pedido.status = 'FINALIZADO';
```

O caso de uso virou o domínio, e o domínio virou um DTO. Sintoma: entidades sem métodos e
casos de uso de 200 linhas cheios de `if`. Toda regra que se repetiria em outro caso de uso
pertence à entidade.

### 6. Entidade vazando para a resposta HTTP

`res.json(usuario)` com a entidade direto. Um campo privado renomeado quebra o app mobile.
Um campo sensível novo vaza sem ninguém notar. Sempre passe por DTO.

### 7. Um port por tabela

Espelhar o schema em interfaces (`RepositorioDeItensDePedido`) desfaz o agregado. O
repositório é **por raiz de agregado**, não por tabela. `Pedido` e seus itens compartilham um
repositório porque compartilham um limite de consistência.

### 8. Excesso de camadas em CRUD

Sete arquivos para cadastrar um estado da federação. Aqui o problema não é a arquitetura, é
aplicá-la onde não há domínio. Módulos sem regra podem ser CRUD simples no mesmo projeto.
Arquitetura é escolha por módulo, não decreto global.

### 9. Interface com uma implementação e sem propósito

Criar `IUsuarioService` para `UsuarioService` porque "é boa prática". Se a interface não
atravessa uma fronteira de camada, ela só adiciona indireção. A interface tem que estar
invertendo alguma seta; se não está, não crie.

### 10. `new Date()`, `Math.random()` e `process.env` dentro do núcleo

Dependências globais escondidas. Tornam o código não determinístico e o teste, frágil. Vire
ports.

### 11. Prefixo `I` e outras cerimônias importadas

`IRepositorioDePedidos`, `RepositorioDePedidosImpl`. Convenção de C# antigo que atrapalha em
outras linguagens. Prefira nomear a interface pelo conceito (`RepositorioDePedidos`) e a
implementação pela tecnologia (`RepositorioDePedidosPostgres`). O nome já diz tudo.

### 12. Um agregado gigante

`Cliente` com todos os pedidos, todos os endereços, todo o histórico. Carregar é inviável e
duas operações não relacionadas passam a competir pelo mesmo bloqueio. Prefira agregados
pequenos com referência por id.

---

## Parte 13 — "Onde eu coloco esse código?" (árvore de decisão)

Use isto quando estiver com um trecho de código na mão e sem saber onde ele vai.

```
Esse código menciona HTTP, SQL, um SDK, arquivo, fila ou variável de ambiente?
│
├── SIM ──▶ INFRASTRUCTURE.
│            É adapter de entrada (recebe chamada) ou de saída (faz chamada)?
│            Se de saída, existe um PORT correspondente lá dentro? Se não, crie.
│
└── NÃO ──▶ Esse código coordena passos, carrega dados e grava resultado?
            │
            ├── SIM ──▶ APPLICATION (caso de uso).
            │            Se ele contém `if` de regra, extraia a regra para o domínio.
            │
            └── NÃO ──▶ É uma regra ou cálculo do negócio?
                        │
                        ├── Pertence a UM objeto e usa o estado dele?
                        │      ──▶ Método da ENTIDADE
                        │
                        ├── É um conceito com validação/formato próprio?
                        │      ──▶ VALUE OBJECT
                        │
                        ├── Envolve vários objetos e não pertence a nenhum?
                        │      ──▶ DOMAIN SERVICE
                        │
                        └── É formatação para exibição (moeda, data, máscara)?
                               ──▶ INFRASTRUCTURE (presenter). Não é negócio.
```

### Casos duvidosos resolvidos

| Situação | Onde | Por quê |
|---|---|---|
| Validar formato de CPF | Domain (Value Object `Cpf`) | O algoritmo do dígito verificador é do negócio |
| Validar que o body tem o campo `cpf` | Infra (controller/schema) | É sobre o protocolo |
| Gerar UUID | Port `GeradorDeId` + adapter | Não determinístico |
| Hash de senha | Port `Hasher` + adapter | O algoritmo é detalhe técnico e troca |
| "Senha precisa ter 8 caracteres" | Domain (VO `Senha`) | É política do negócio |
| Enviar e-mail de boas-vindas | Port + adapter, disparado por caso de uso ou evento | Efeito externo |
| "Sempre notificar o cliente ao finalizar" | Domain event + handler | A obrigação é do negócio; o canal, não |
| Paginação de uma listagem | Port de leitura em application, SQL na infra | A tela pede; o SQL implementa |
| Cache | Infra (decorator sobre o adapter) | Otimização invisível ao negócio |
| Retry de chamada externa | Infra (dentro do adapter) | Detalhe de comunicação |
| Log de auditoria de negócio | Domain event | "Quem aprovou o quê" é fato de negócio |
| Log de debug técnico | Infra | Não é negócio |
| Fuso horário na exibição | Infra (presenter) | Apresentação |
| "Vence em 14 dias corridos" | Domain | Regra |

---

## Parte 14 — Quando NÃO usar isso

Um documento honesto precisa desta parte. Clean Architecture tem custo real, e ele é pago
adiantado enquanto o benefício chega depois. Nem todo projeto chega ao "depois".

### O custo

- **Mais arquivos.** Um caso de uso pode envolver entidade, VO, port, caso de uso, adapter,
  mapper, controller e registro no composition root. Oito arquivos onde havia um.
- **Curva de aprendizado.** Um dev novo leva semanas para entender por que existe indireção,
  e nesse meio-tempo produz código nos lugares errados.
- **Atrito com o framework.** Rails, Laravel e Django foram desenhados em torno do
  ActiveRecord. Nadar contra isso significa abrir mão de scaffolding, de boa parte da
  documentação e de bibliotecas do ecossistema.
- **Mapeamento manual.** O Mapper é código repetitivo e sem glamour.

### Quando o custo não se paga

Não use, ou use uma versão bem reduzida, se:

- O sistema é **CRUD com validação simples**. Se todas as suas "regras" são "campo
  obrigatório" e "e-mail válido", não há domínio a proteger. Um `ActiveRecord` bem feito é
  a resposta certa.
- É **protótipo, MVP de validação ou script**. Vida útil de semanas. Otimize para velocidade.
- É um **serviço muito pequeno**, de dois endpoints, que faz uma coisa só.
- O time é **pequeno e não conhece o padrão**, e o prazo é curto. Arquitetura mal aplicada é
  pior que ausência de arquitetura, porque produz indireção sem proteção.
- O sistema é essencialmente **ETL ou transformação de dados**, onde o "domínio" é o pipeline.

### Quando o custo se paga com folga

- Regras de negócio **numerosas, condicionais e mutáveis** (seguros, crédito, logística,
  saúde, fiscal, folha de pagamento).
- Vida útil esperada acima de **dois ou três anos**.
- Mais de **um canal de entrada** (API pública, app, painel interno, fila, cron, importação).
- Múltiplas **integrações externas** que você não controla e que mudam.
- Time **grande ou rotativo**, onde a estrutura vira documentação executável.
- Domínio onde **um bug custa caro** (dinheiro, conformidade legal, segurança).

### O caminho do meio, que costuma ser a melhor escolha

Você não precisa escolher entre tudo e nada. Uma progressão pragmática que funciona bem:

1. **Comece separando apenas casos de uso de controllers.** Baixo custo, ganho imediato de
   testabilidade e clareza. Sem entidades ricas ainda.
2. **Introduza ports** para as duas ou três integrações externas que mais te incomodam
   (pagamento, e-mail, o serviço legado instável).
3. **Introduza Value Objects** para os conceitos que você já validou em três lugares.
4. **Enriqueça as entidades** apenas nos agregados que concentram regra de verdade.
5. **Deixe os módulos de CRUD como CRUD.** Sério. Nada obriga que o cadastro de categorias
   tenha a mesma cerimônia que o motor de precificação.

E o teste final para saber se valeu: **quando chegou a mudança inesperada, quantos arquivos
você teve que abrir?**

---

## Parte 15 — Glossário

**Adapter** — Implementação concreta de uma porta, ou tradutor entre o mundo externo e o
núcleo. `PedidoRepositoryPostgres`, `PedidoController`.

**Agregado** — Grupo de entidades e VOs tratado como unidade de consistência e de transação.

**Anti-Corruption Layer (ACL)** — Camada de tradução entre o seu domínio e um sistema
externo com modelo diferente, para que o modelo alheio não contamine o seu. Um gateway bem
feito já é uma ACL pequena.

**Application Service** — Sinônimo de caso de uso. Orquestra, não decide regra.

**Bounded Context** — Fronteira dentro da qual um termo tem um significado só. "Cliente" no
contexto de Vendas e no de Suporte podem ser modelos diferentes, e tudo bem.

**Composition Root** — Único ponto do sistema onde as dependências concretas são instanciadas
e injetadas. Normalmente `main`.

**CQRS** — Separar o caminho de escrita (comandos, passam pelo domínio) do de leitura
(consultas, podem ir direto ao banco).

**Domain Event** — Registro de um fato de negócio já ocorrido. Nome no particípio.

**Domain Service** — Regra de negócio sem estado que envolve várias entidades.

**DTO** — Estrutura de dados sem comportamento, usada para atravessar fronteiras.

**Entidade** — Objeto com identidade que persiste através de mudanças de estado.

**Invariante** — Condição que precisa ser sempre verdadeira. "O total do pedido é a soma dos
itens." Agregados existem para protegê-las.

**Inversão de Dependência (DIP)** — Módulos de alto nível não dependem de módulos de baixo
nível; ambos dependem de abstrações definidas pelo alto nível.

**Linguagem Ubíqua** — O vocabulário compartilhado entre time técnico e negócio, refletido
literalmente nos nomes de classes e métodos.

**Modelo Anêmico** — Objetos de domínio só com dados; comportamento espalhado em services.

**Port** — Interface que define uma fronteira do núcleo. De entrada (o que o sistema faz) ou
de saída (o que o sistema precisa).

**Repository** — Port de saída que abstrai a persistência de um agregado, com vocabulário de
coleção (`salvar`, `porId`), não de banco.

**Unit of Work** — Abstração de um limite transacional.

**Value Object** — Objeto imutável, sem identidade, definido pelos seus valores e
auto-validado.

---

## Parte 16 — Checklist de revisão de código

Para colar no template de pull request do projeto.

### Regra da dependência
- [ ] Nenhum arquivo em `domain/` importa de `application/` ou `infrastructure/`
- [ ] Nenhum arquivo em `domain/` importa framework, ORM ou SDK
- [ ] Nenhum arquivo em `application/` importa de `infrastructure/`
- [ ] Todos os ports estão declarados dentro (`domain/` ou `application/`)

### Domain
- [ ] Entidades têm comportamento, não só getters e setters
- [ ] Nenhum setter público que permita estado inválido
- [ ] Value Objects para conceitos com validação própria (dinheiro, documentos, contatos)
- [ ] Nada de `new Date()`, `Math.random()`, `process.env` ou `async` desnecessário
- [ ] Erros de domínio são tipados e não conhecem status HTTP
- [ ] Referência entre agregados por id, não por objeto

### Application
- [ ] O caso de uso tem um único método público e um nome de verbo
- [ ] Não há regra de negócio no caso de uso (só orquestração)
- [ ] Entrada e saída são DTOs, não entidades
- [ ] Todas as dependências chegam pelo construtor, como interfaces
- [ ] Ports usam vocabulário do domínio, sem nome de tecnologia

### Infrastructure
- [ ] Cada adapter implementa uma interface declarada no núcleo
- [ ] Erros de SDK são traduzidos antes de subir para o núcleo
- [ ] O repositório salva o agregado inteiro
- [ ] `process.env` só é lido em `config/`
- [ ] Controllers só traduzem e delegam

### Testes
- [ ] Regra nova tem teste de domínio sem infraestrutura
- [ ] Caso de uso novo tem teste com fakes
- [ ] Adapter novo tem teste de contrato rodando contra a implementação real

### Automatize o que der

Checklist humano falha. Vale configurar a verificação:

**ESLint** (`eslint-plugin-boundaries` ou `no-restricted-imports`):

```jsonc
{
  "rules": {
    "no-restricted-imports": ["error", {
      "patterns": [
        { "group": ["**/infrastructure/**"], "message": "domain/application não podem importar infrastructure" },
        { "group": ["typeorm", "express", "@nestjs/*"], "message": "framework não entra no núcleo" }
      ]
    }]
  }
}
```

Ferramentas equivalentes em outras plataformas: **ArchUnit** (Java/Kotlin/.NET),
**Deptrac** ou **PHPArkitect** (PHP), **import-linter** (Python), **go-arch-lint** (Go).

Uma regra verificada pelo CI vale mais que dez páginas de documento. Inclusive mais que este.

---

## Conclusão

Se você fechar o arquivo agora, leve quatro frases:

1. **Dependências apontam para dentro.** Sempre, sem exceção.
2. **O núcleo declara o que precisa; a infraestrutura obedece.** É isso que uma porta é.
3. **Regra de negócio mora em entidades; casos de uso apenas coordenam.**
4. **Se testar está difícil, o problema é o acoplamento, não o teste.**

E leve uma advertência: nada disso é dogma. O objetivo nunca foi ter as pastas certas, foi
conseguir mudar o sistema em dois anos sem medo. Se em algum módulo a estrutura completa não
serve a esse objetivo, use menos. Arquitetura boa é a que você consegue justificar, não a
que você copiou.

---

### Para ir além

- Alistair Cockburn, *Hexagonal Architecture* (2005), o artigo original
- Robert C. Martin, *Clean Architecture* (2017)
- Eric Evans, *Domain-Driven Design* (2003), o "livro azul"
- Vaughn Vernon, *Implementing Domain-Driven Design* (2013), mais prático que o Evans
- Martin Fowler, *Patterns of Enterprise Application Architecture* (2002), para Repository,
  Unit of Work e Data Mapper
