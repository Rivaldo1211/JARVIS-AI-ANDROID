# JARVIS AI — Android

Assistente pessoal Android em Kotlin e Jetpack Compose, com reconhecimento de fala pt-BR, resposta falada, comandos locais e memória pessoal adaptativa armazenada no próprio aparelho.

## Versão 0.4.0

- [x] Interface futurista JARVIS HUD
- [x] Reconhecimento de fala pt-BR pelo serviço disponível no Android
- [x] Resposta por Text-to-Speech com ajuste de timbre e ritmo
- [x] Abrir YouTube, WhatsApp, Chrome e Configurações por comando
- [x] Pesquisa web iniciada por comando de voz
- [x] Perfil pessoal local e persistente
- [x] Aprendizado explícito de nome, interesses, preferências, trabalho, objetivos, localidade e estilo de resposta
- [x] Consulta e exclusão da memória por comando de voz
- [x] Fluxo principal sem dependência de API de IA ou servidor externo
- [ ] Raciocínio generativo avançado totalmente offline
- [ ] Modelo de linguagem local embarcado
- [ ] Palavra de ativação contínua "Jarvis"
- [ ] Serviço de voz em segundo plano / integração com assistente padrão

## Memória e privacidade

O perfil é salvo em armazenamento privado do aplicativo (SharedPreferences) no próprio Android. O JARVIS não envia essas informações para um backend. Nesta versão, ele aprende quando a pessoa declara explicitamente fatos e preferências, por exemplo:

- "Meu nome é Ana"
- "Eu gosto de tecnologia"
- "Prefiro respostas curtas"
- "Trabalho com logística"
- "Meu objetivo é aprender programação"

Comandos de memória:
- "O que você sabe sobre mim?"
- "Qual é meu nome?"
- "Esqueça meu nome"
- "Apague minha memória"

A memória é local ao aplicativo. Desinstalar o app ou limpar os dados do aplicativo também apaga esse armazenamento. O reconhecimento de fala do Android pode depender do serviço de voz configurado no aparelho; pesquisas na web abrem o navegador.

## Limite técnico desta versão

Memória, perfil e regras adaptativas funcionam sem API. Isso não equivale a um modelo de linguagem: respostas abertas, raciocínio amplo e geração de conteúdo ainda são limitados. Para obter conversa generativa offline, será necessário integrar e otimizar um modelo de linguagem local compatível com a memória e com os recursos de hardware do dispositivo.

## Requisitos

- Android 8.0 (API 26) ou superior
- Microfone e serviço de reconhecimento de fala disponíveis
- Internet somente para pesquisas web e quando o provedor de reconhecimento de voz exigir

## Compilação

O workflow GitHub Actions em `.github/workflows/jarvis-android-build.yml` compila o APK de depuração e publica o artefato na execução do workflow.
