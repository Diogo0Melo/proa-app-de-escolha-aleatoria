# Arquitetura — Aleastra

> Documento normativo. Se o código divergir daqui, um dos dois está errado: decida qual e conserte.
> Para um resumo operacional voltado a assistentes de IA, veja [`AI_CONTEXT.md`](AI_CONTEXT.md).

## Princípio fundamental

**Core vs. Features. A dependência aponta para dentro.**

- `core/` — infraestrutura compartilhada. **NUNCA** importa de `features/`.
- `features/` — cada modo de jogo é autocontido. Depende **só** de `core/` (e de si mesmo).
- Composition root — `core/di/AppContainer.kt`, `core/di/ViewModelFactory.kt` e `core/ui/navigation/AppNavigation.kt` são os **únicos** pontos que conhecem todas as features.

```
com.ogos.apprandomizador
├── MainActivity.kt                 ← monta o Scaffold e delega ao AppNavigation
├── RandomApplication.kt            ← cria o AppContainer
├── core/
│   ├── ai/
│   │   ├── domain/                 LlmClient, AiSettingsRepository, LlmRetry, ApiErrorMapper, SecretStore
│   │   └── data/                   OpenAiCompatibleClient, AiSettingsDataStore, KeystoreSecretStore
│   ├── data/                       IRepository, IRouletteSource, IDefaultInitialization
│   ├── database/                   AppDatabase (Room), DAOs, Converters
│   ├── datastore/                  DataStoreManager
│   ├── di/                         AppContainer, ViewModelFactory   ← composition root
│   ├── entropy/                    EntropySource, SecureEntropySource, EntropyStream, MonotonicClock
│   ├── model/                      RaffleItem, ItemList, DrawHistoryEntry
│   ├── outcome/                    StoredOutcome, IOracleRepository, OracleRepository
│   └── ui/                         MainViewModel, navigation, theme, component, format, voice
└── features/
    ├── ai_settings/  ui/
    ├── fingers/      domain/, ui/
    ├── hub/          ui/           ← ModeCatalog (catálogo de modos)
    ├── oracle/       domain/, ui/
    ├── randomize/    data/, domain/, ui/
    └── tarot/        data/, domain/, ui/
```

## Estrutura de uma feature

Toda feature segue o mesmo corte. Só crie a pasta de que precisa.

| Pasta | Contém | Pode depender de |
|---|---|---|
| `domain/` | Regras puras, modelos, casos de uso. Kotlin puro. | `core/domain`-like, stdlib |
| `data/` | Implementações de contrato: banco, rede, DataStore. | `domain/` + `core/` |
| `ui/` | Telas Compose, ViewModel, componentes, formatação de apresentação. | `domain/` + `data/` + `core/ui` |

Regra de dentro para fora: `ui` → `data` → `domain`. Nunca o contrário. `domain/` não importa `android.*` nem `androidx.*`.

## Regras de ouro

1. **O ViewModel não guarda estado de apresentação.** Ordem de exibição, seleção visual, animação e ciclo de revelação pertencem à View. A sequência da roleta, por exemplo, é construída por `buildDisplaySequence` em `features/randomize/ui/DisplaySequence.kt` — o ViewModel entrega o vencedor, a UI decide como exibir.
2. **`domain/` é Kotlin puro.** Zero Android/Compose. Se precisa de `Context`, `Uri` ou `Log`, não é domínio.
3. **Novo modo de jogo:** criar `features/nome_do_modo/`, registrar o modo em `features/hub/ui/ModeCatalog.kt` e a rota em `core/ui/navigation/Routes.kt`. Nada mais precisa ser tocado nas features existentes.
4. **Injeção de dependência explícita.** Serviços nascem em `AppContainer`; ViewModels em `ViewModelFactory`. ViewModel recebe **interfaces** (`IRepository`, `EntropySource`, `IOracleRepository`, `LlmClient`), nunca classes concretas.
5. **Aleatoriedade é criptográfica.** Proibido `kotlin.random.Random`. A entropia vem de `EntropySource` (`SecureEntropySource`) e o sorteio é derivado por `EntropyStream` (SHA-256, `uniformInt`, `pickWinnerIndex`, `deriveSpinStream`, `deriveShuffleStream`, `cryptoShuffled`).

## Contratos compartilhados

| Contrato | Onde | Implementação |
|---|---|---|
| `IRepository` / `IRouletteSource` | `core/data` | `features/randomize/data/ItemListRepository` |
| `IDefaultInitialization` | `core/data` | `features/randomize/data/DefaultInitialize` |
| `IOracleRepository` | `core/outcome` | `core/outcome/OracleRepository` |
| `LlmClient` | `core/ai/domain` | `core/ai/data/OpenAiCompatibleClient` |
| `AiSettingsRepository` | `core/ai/domain` | `core/ai/data/AiSettingsRepositoryImpl` |
| `SecretStore` | `core/ai/domain` | `core/ai/data/KeystoreSecretStore` |
| `EntropySource` | `core/entropy` | `core/entropy/SecureEntropySource` |
| `DeckProvider` / `ReadingInterpreter` | `features/tarot/domain` | `FullTarotDeck` / `OfflineReadingInterpreter` |

## Fluxo de dados

```
Compose (ui/)
  └─ ViewModel (ui/)  ── StateFlow<UiState @Immutable>
       └─ domínio puro (domain/)  ── EntropyStream para qualquer sorteio
            └─ contrato (core/data|outcome|ai)
                 └─ implementação (data/ ou core/*/data)
                      └─ Room / DataStore / Keystore / HTTP
```

A UI nunca chama repositório direto. O ViewModel nunca importa `androidx.compose.*` além de `@Immutable`.

## Compatibilidade que não pode quebrar

Estes valores são contrato com dados já gravados no aparelho do usuário. Renomear quebra histórico e estatísticas.

- `Routes.*` — strings fixas: `home_randomize`, `mode_oracle`, `mode_tarot`, `mode_ai_settings`, etc.
- `ModeCatalog` — o modo Sim/Não usa `id = "yes_no"`.
- `StoredOutcome` — chaves `@SerialName("yes_no")`, `"oracle"`, `"tarot"`, `"fingers"`.
- `DataStoreManager` — nomes de arquivo e chaves de preferência.
- `KEY_ALIAS` do Keystore.

## Dívida conhecida

Registrada para não parecer intencional. Corrigir exige extrair o código comum para `core/`.

- `features/oracle` importa `features/tarot/domain/AiReadingService` (contrato de leitura com IA reaproveitado).
- `features/oracle` importa `features/randomize/ui/component/RouletteMath` (gradiente/contraste de cor).
- `features/randomize` importa `features/tarot/ui/CardShape` (forma de carta usada nos slots).

## Testes

A árvore de testes espelha a de produção: `app/src/test/.../core` e `.../features`; idem em `androidTest`.

- `domain/` sempre tem teste na JVM, no caminho espelhado.
- ViewModels usam `MainDispatcherRule`, que mora em `test/core/testing/` porque é usado por quatro features.
- Nada de teste na raiz do pacote.

## Checklist antes de commitar

- [ ] `core/` não importa `features/` (exceto os três arquivos do composition root).
- [ ] `domain/` novo não importa `android.*`/`androidx.*`.
- [ ] Nenhum `kotlin.random` no caminho do sorteio.
- [ ] Nenhuma rota, `@SerialName`, chave de DataStore ou `KEY_ALIAS` renomeada.
- [ ] `./gradlew assembleDebug testDebugUnitTest compileDebugAndroidTestKotlin` verde.
