# Gerenciador de Tarefas (TarefasApp)

Aplicativo Android nativo, escrito em Kotlin, para cadastrar e acompanhar tarefas. As tarefas ficam salvas em um banco de dados local (Room), e o app inclui uma tela de estatísticas feita em Jetpack Compose e uma tela de mapa que gira de acordo com o sensor de orientação do aparelho. O projeto conta com 15 testes automatizados (unitários e instrumentados).

## Funcionalidades

- **Cadastro de tarefas** com título e prioridade (se a prioridade ficar em branco, o app usa "Normal").
- **Lista de tarefas** com checkbox para marcar como concluída ou pendente.
- **Detalhe da tarefa:** mostra título, prioridade, categoria e status, e permite escolher uma categoria (Trabalho, Pessoal, Estudo, Saúde, Lazer ou Urgente) e alternar o status.
- **Persistência local:** as tarefas continuam salvas depois de fechar o app ou reiniciar o aparelho.
- **Estatísticas** (Jetpack Compose): total de tarefas, tarefas concluídas e percentual de conclusão com barra de progresso.
- **Mapa** (OpenStreetMap): mostra a posição do usuário, gira conforme a direção para a qual o celular aponta e exibe a direção em graus e ponto cardeal. Há botões para ligar ou desligar a rotação e para centralizar no usuário.

## Telas

| Tela | Classe | Tecnologia |
|---|---|---|
| Lista e cadastro | `MainActivity` | `LinearLayout`, `ListView`, `findViewById` |
| Detalhe da tarefa | `TaskDetailActivity` | `ConstraintLayout`, `GridView`, View Binding |
| Estatísticas | `StatisticsActivity` + `StatisticsScreen` | Jetpack Compose (Material 3) |
| Mapa | `MapActivity` | osmdroid, sensor `TYPE_ROTATION_VECTOR`, View Binding |

## Arquitetura

O projeto segue o padrão **MVVM** com a camada de dados em Room:

```
Activities (View)  →  TaskViewModel  →  TaskDao / AppDataBase (Room)  →  SQLite
```

- A `MainActivity` e a `TaskDetailActivity` não acessam o banco diretamente. Elas observam `LiveData` e chamam métodos do `TaskViewModel`.
- O `TaskViewModel` estende `AndroidViewModel` e executa as gravações em `viewModelScope`, fora da thread principal.
- O `AppDataBase` é um singleton, e o Room reemite a lista de tarefas automaticamente a cada alteração.
- A tela de estatísticas recebe o total e as concluídas por `Intent`. O percentual é calculado pela função `calcularPercentualConcluido`.

### Estrutura de pastas

```
app/src/
├── main/java/com/example/tarefas/
│   ├── MainActivity.kt
│   ├── TaskDetailActivity.kt
│   ├── StatisticsActivity.kt
│   ├── MapActivity.kt
│   ├── adapter/        TaskAdapter, CategoryAdapter
│   ├── data/           AppDataBase, TaskDao
│   ├── model/          Task (entidade do Room)
│   ├── viewmodel/      TaskViewModel
│   └── ui/             StatisticsScreen e tema do Compose
├── test/java/com/example/tarefas/
│   └── ui/             CalcularPercentualConcluidoTest
└── androidTest/java/com/example/tarefas/
    ├── LiveDataTestUtils.kt
    ├── data/           TaskDaoTest
    └── viewmodel/      TaskViewModelTest
```

## Persistência

- Banco: SQLite via **Room 2.6.1**, com processamento de anotações pelo **KSP**.
- Arquivo do banco: `app-database`; tabela: `tarefas`; versão do schema: `1`.
- Não há migrações configuradas. Se a entidade `Task` mudar, aumente a versão do banco e crie uma migração, ou desinstale o app durante o desenvolvimento.

## Mapa e sensores

- Os mapas vêm do **OpenStreetMap** pela biblioteca **osmdroid** e **não exigem chave de API**.
- O app precisa de **internet** para baixar os tiles. Eles ficam em cache na pasta `cache/osmdroid` do app.
- O servidor do OSM bloqueia requisições sem identificação, então o `MapActivity` define um *user agent* próprio antes de carregar o mapa.
- A direção usa o sensor `TYPE_ROTATION_VECTOR`, com suavização do valor e correção da declinação magnética (norte magnético para norte geográfico) a partir da posição do usuário.
- A tela do mapa é fixa em **retrato**, porque o cálculo do azimute assume essa orientação.
- Permissões usadas: `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_FINE_LOCATION` e `ACCESS_COARSE_LOCATION` (a localização é pedida em tempo de execução). Sem a permissão, o mapa funciona, mas não mostra a posição do usuário.
- Se o aparelho não tiver o sensor, o app avisa e o mapa fica com o norte para cima.

## Testes

O projeto tem **15 testes**, divididos em dois tipos:

| Tipo | Pasta | Onde roda | Quantidade |
|---|---|---|---|
| Unitários (JVM) | `app/src/test` | Computador, sem emulador | 5 |
| Instrumentados | `app/src/androidTest` | Emulador ou aparelho | 10 |

O `TaskDao` e o `TaskViewModel` dependem do Android (o Room precisa de um `Context`, e o ViewModel obtém o banco dentro de si), por isso seus testes são instrumentados.

### O que é testado

**`CalcularPercentualConcluidoTest`** (função pura do percentual de conclusão):

| Teste | Verifica |
|---|---|
| `semTarefas_retornaZero` | total 0 resulta em 0% |
| `totalNegativo_retornaZero` | total negativo resulta em 0% |
| `nenhumaConcluida_retornaZero` | nenhuma tarefa concluída resulta em 0% |
| `todasConcluidas_retorna100` | todas concluídas resulta em 100% |
| `percentualFracionado_truncaParaBaixo` | 2 de 3 resulta em 66% (a função trunca, não arredonda) |

**`TaskDaoTest`** (usa um banco **em memória**, separado do banco real do app):

| Teste | Verifica |
|---|---|
| `inserir_retornaIdsPositivosEDistintos` | cada inserção gera um ID novo e maior |
| `listar_retornaTarefasOrdenadasPorId` | a lista vem na ordem de inserção |
| `atualizar_persisteNovosValores` | valores padrão da entidade e atualização de categoria e status |
| `deletar_removeApenasATarefaIndicada` | só a tarefa indicada é removida |
| `buscar_idInexistente_retornaNull` | busca por ID que não existe devolve `null` |

**`TaskViewModelTest`**:

| Teste | Verifica |
|---|---|
| `addTask_removeEspacosDoTitulo_ePersiste` | o título é aparado e a tarefa chega ao banco |
| `addTask_prioridadeEmBranco_usaNormal` | prioridade vazia vira "Normal" |
| `totalCount_refleteQuantidadeDeTarefas` | contagem total confere com as tarefas salvas |
| `toggleDone_marcaTarefaEAtualizaCompletedCount` | alterna o status e atualiza a contagem de concluídas |
| `updateCategory_alteraApenasACategoria` | título, prioridade e status permanecem intactos |

O arquivo `LiveDataTestUtils.kt` traz funções auxiliares que aguardam a emissão de um `LiveData` fora da thread principal, para que os testes possam esperar as operações assíncronas do Room.

### Como executar

**Pelo Android Studio:** clique com o botão direito na pasta `test` (unitários) ou `androidTest` (instrumentados) e escolha **Run Tests**.

**Pelo terminal:**

```bash
./gradlew test                    # unitários, sem emulador
./gradlew connectedAndroidTest    # instrumentados, com emulador ligado ou aparelho conectado
```

No Windows (PowerShell), use `.\gradlew` no lugar de `./gradlew`.

### Atenção

Os testes do `TaskViewModel` usam o **banco real do app** (o `AppDataBase` é um singleton e o ViewModel o obtém sozinho). Por isso eles limpam as tabelas antes e depois de cada teste. **Isso apaga as tarefas do app instalado no emulador ou aparelho onde os testes rodam.** Prefira um emulador. Os testes do DAO usam um banco em memória e não afetam os dados do app.

## Tecnologias e versões

| Item | Versão |
|---|---|
| Linguagem | Kotlin 1.9.24 |
| Android Gradle Plugin | 8.5.0 |
| Gradle | 8.7 |
| KSP | 1.9.24-1.0.20 |
| compileSdk / targetSdk | 34 |
| minSdk | 24 (Android 7.0) |
| Jetpack Compose | BOM 2024.06.00 (Material 3), compilador 1.5.14 |
| Room | 2.6.1 |
| Lifecycle (ViewModel e LiveData) | 2.8.4 |
| osmdroid | 6.1.18 |
| JUnit (testes unitários) | 4.13.2 |
| AndroidX Test (testes instrumentados) | ext:junit 1.2.1, runner 1.6.2, core 1.6.1 |

## Como executar o app

**Pré-requisitos:** Android Studio (versão recente, com JDK 17 embutido) e um emulador ou aparelho com Android 7.0 ou superior.

1. Clone o repositório:
   ```bash
   git clone https://github.com/FranciscoBendo/Trabalho-2.git
   ```
2. No Android Studio, use **File → Open** e selecione a pasta que contém o `settings.gradle.kts` e o `gradlew`. Abrir uma pasta acima ou abaixo dessa impede a IDE de reconhecer o projeto como Gradle.
3. Aguarde o **Gradle Sync** terminar. O primeiro sync baixa o Gradle e as dependências, então precisa de internet.
4. Escolha o módulo `app`, selecione um dispositivo e clique em **Run**.

### Testando o mapa no emulador

- Defina uma localização em **Extended controls (⋯) → Location**.
- Para simular a rotação do aparelho, use **Extended controls → Virtual sensors**.
- O emulador precisa de acesso à internet para carregar os tiles.

## Limitações conhecidas

- Não é possível editar nem excluir tarefas pela interface (o DAO já tem o método `deletar`, ainda sem uso na tela).
- A prioridade é um texto livre, sem validação.
- As estatísticas são uma "foto" do momento em que o botão é tocado, porque os números viajam por `Intent`.
- Parte dos textos da interface está escrita direto no código, e não em `strings.xml`.
- Os testes do `TaskViewModel` dependem do banco real do app e apagam seus dados no aparelho de teste, porque o ViewModel não recebe o DAO por parâmetro.
- Não há testes de interface (Espresso ou Compose Test) nem testes da lógica do mapa.
- Os servidores públicos do OpenStreetMap servem para desenvolvimento e uso leve. Em produção, use um provedor de tiles próprio ou pago.

## Autor

Francisco Bendo, [github.com/FranciscoBendo](https://github.com/FranciscoBendo)
