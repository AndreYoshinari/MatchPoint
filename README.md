🏐 MatchPoint

MatchPoint é um aplicativo Android desenvolvido para auxiliar no gerenciamento e acompanhamento de partidas de vôlei. A proposta é oferecer uma experiência simples e intuitiva para organizar partidas, cadastrar jogadores, acompanhar a pontuação e consultar estatísticas.

O projeto está sendo desenvolvido com foco em boas práticas de desenvolvimento Android, arquitetura organizada, separação de responsabilidades e persistência local de dados.

✨ Funcionalidades

Implementadas / em desenvolvimento

Estrutura inicial do projeto Android.

Modelagem de domínio para partidas, jogadores, equipes e sets.

Regras de pontuação do vôlei.

Casos de uso para gerenciamento de partidas.

Persistência local utilizando Room.

Separação entre camadas de apresentação, domínio e dados.

Injeção de dependências com Koin.

Estrutura de testes unitários.

Atualizar esta lista conforme cada funcionalidade estiver efetivamente implementada e validada.

🛠️ Tecnologias e ferramentas

- Kotlin — linguagem principal.
- Android SDK — desenvolvimento nativo Android.
- Jetpack Compose — construção da interface declarativa, caso utilizado nas telas finais.
- MVVM — organização da camada de apresentação.
- Clean Architecture — separação de responsabilidades e regras de negócio.
- Room — persistência local de dados.
- Koin — injeção de dependências.
- Coroutines / Flow — operações assíncronas e fluxo de dados.
- JUnit / Mockito — testes automatizados.

🏗️ Arquitetura

O projeto utiliza uma organização baseada em Clean Architecture, dividindo as responsabilidades em três camadas principais:

Presentation
Responsável pela interface do usuário, gerenciamento de estados e interação com os casos de uso.

Screens
ViewModels
UiStates
Navigation
Theme

Domain
Contém as regras de negócio e os conceitos centrais do aplicativo, sem depender de detalhes de implementação do Android ou da persistência.
Models
Repository interfaces
UseCases
Scoring Rules

Data
Responsável pela implementação dos contratos definidos no domínio e pelo acesso aos dados.
Room Database
DAOs
Entities
Mappers
Repository Implementations

Estrutura de diretórios

br.com.matchpoint
│
├── app/
│   ├── MainActivity.kt
│   └── MatchPointApp.kt
│
├── data/
│   ├── local/
│   │   ├── dao/
│   │   ├── db/
│   │   ├── entity/
│   │   └── mapper/
│   │
│   └── repository/
│
├── di/
│   ├── DataModule.kt
│   ├── DomainModule.kt
│   └── PresentationModule.kt
│
├── domain/
│   ├── model/
│   ├── repository/
│   ├── scoring/
│   └── usecase/
│
└── presentation/
    ├── components/
    ├── error/
    ├── home/
    ├── matchdetail/
    ├── matchsetup/
    ├── navigation/
    ├── roster/
    ├── scoreboard/
    ├── stats/
    └── theme/

Fluxo de comunicação

A camada de apresentação interage com o domínio por meio dos casos de uso. O domínio define os contratos dos repositórios, enquanto a camada de dados fornece suas implementações.

Presentation
     │
     ▼
  ViewModel
     │
     ▼
   UseCase
     │
     ▼
  Repository
     │
     ▼
    Data
     │
     ▼
 Room Database

🧪 Testes

O projeto possui uma estrutura dedicada a testes automatizados, buscando garantir a confiabilidade das regras de negócio e dos fluxos principais.

Os testes contemplam:
Regras de pontuação e encerramento de sets.
Validação de estados das partidas.
Casos de uso.
Comportamento dos ViewModels.
Repositórios fake para testes isolados.

🚀 Como executar o projeto

Pré-requisitos

Android Studio.
JDK compatível com a versão do Gradle utilizada.
Android SDK configurado.

Instalação

Clone o repositório:

git clone https://github.com/AndreYoshinari/MatchPoint.git

Abra o projeto no Android Studio.

Aguarde a sincronização do Gradle.

Execute o aplicativo em um emulador ou dispositivo Android.

🗺️ Futuras funcionalidades

O MatchPoint está em desenvolvimento e possui diversas funcionalidades planejadas para as próximas versões.

Gerenciamento de partidas
Histórico completo de partidas.
Edição e exclusão de partidas.
Recuperação de partidas em andamento.
Configurações personalizadas de sets e pontuação.
Jogadores e equipes
Cadastro e edição de jogadores.
Gerenciamento de equipes.
Distribuição de jogadores entre equipes.
Histórico individual de desempenho.
Placar e acompanhamento
Interface de placar em tempo real.
Animações durante a atualização da pontuação.
Desfazer última pontuação.
Identificação do jogador responsável pelo ponto.

Estatísticas
Estatísticas individuais por jogador.
Estatísticas por equipe.
Aproveitamento de vitórias e derrotas.

Histórico de desempenho.
Gráficos e indicadores de desempenho.
Evoluções técnicas
Testes de interface e integração.

Melhorias de acessibilidade.

Exportação e compartilhamento de resultados.
Backup e sincronização em nuvem.

👨‍💻 Desenvolvedor

Desenvolvido por André Yoshinari.

Projeto pessoal criado para aprofundar conhecimentos em desenvolvimento Android nativo, Kotlin, arquitetura de software, persistência de dados e testes automatizados.

LinkedIn: https://www.linkedin.com/in/andreyoshinari/