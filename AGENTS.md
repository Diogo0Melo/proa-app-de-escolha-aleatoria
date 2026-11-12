# AGENTS.md — instruções para agentes de IA

Você é um agente de IA trabalhando neste repositório. Antes de gerar código, leia e siga estas regras sem exceção.

Projeto Android nativo: Kotlin, Jetpack Compose, MVVM, SOLID, Room e DataStore.
A arquitetura é **Core (infraestrutura)** + **Features (modos de jogo)**, com dependência apontando para dentro.
Detalhes completos em [`ARCHITECTURE.md`](ARCHITECTURE.md).

Se for sugerir código, siga estas regras sem exceção.

## 1. Onde colocar o código?

| O que é | Onde vai |
|---|---|
| Regra de negócio pura (sem Android) | `features/<nome>/domain/` |
| Tela, ViewModel, componente visual da feature | `features/<nome>/ui/` |
| Banco, rede ou armazenamento **daquela** feature | `features/<nome>/data/` |
| Banco, rede ou armazenamento **compartilhado** | `core/*/` (`data`, `domain`) |
| Tema, navegação, formatação, TTS, utilitário de UI | `core/ui/` |
| Aleatoriedade, entropia, relógio | `core/entropy/` |

Features existentes: `ai_settings`, `fingers`, `hub`, `oracle`, `randomize`, `tarot`.
Nome de pasta de feature usa **underscore** (`ai_settings`), nunca hífen.

## 2. O que NUNCA fazer

- **Não** usar `kotlin.random.Random`. Use `core/entropy`: `EntropySource` para a semente e `EntropyStream` (`uniformInt`, `pickWinnerIndex`, `deriveSpinStream`, `deriveShuffleStream`, `cryptoShuffled`) para derivar o sorteio.
- **Não** colocar lógica de negócio dentro de `@Composable`.
- **Não** chamar repositório direto da UI. Sempre ViewModel.
- **Não** importar `features/X` dentro de `features/Y`. Se precisar, extraia o contrato para `core/`.
- **Não** criar estado de exibição no ViewModel (lista embaralhada, ordem, animação). Isso é papel da `ui/` — ver `features/randomize/ui/DisplaySequence.kt`.
- **Não** renomear rotas de `Routes.kt`, `@SerialName` de `StoredOutcome`, chaves do DataStore, `KEY_ALIAS` ou o id `"yes_no"` do `ModeCatalog`: são dados já gravados no aparelho do usuário.

## 3. Padrão de ViewModel

- Recebe dependências pelo construtor, sempre por interface (`IRepository`, `EntropySource`, `IOracleRepository`, `LlmClient`).
- Expõe `StateFlow<UiState>`; `UiState` é `@Immutable` e não carrega estado de apresentação.
- Registre o ViewModel em `core/di/ViewModelFactory.kt` e seus serviços em `core/di/AppContainer.kt`. São os únicos arquivos que podem conhecer todas as features.

## 4. Testes

- Todo código em `domain/` tem teste em `test/features/<nome>/domain/` (ou `test/core/...` para o core).
- ViewModels usam `MainDispatcherRule` de `test/core/testing/`.
- A árvore de testes espelha a de produção; nada de teste na raiz do pacote.

## 5. Definição de pronto

`./gradlew assembleDebug testDebugUnitTest compileDebugAndroidTestKotlin` verde, `core/` sem imports de `features/` (exceto composition root) e nenhum contrato de persistência alterado.
