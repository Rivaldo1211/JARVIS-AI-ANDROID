# JARVIS AI — Android

Assistente pessoal Android em Kotlin, com interface nativa, reconhecimento de fala em português brasileiro, resposta falada e primeiros comandos locais.

## Estado atual — 0.1.0

- [x] Estrutura inicial Android/ Kotlin + Jetpack Compose
- [x] Interface futurista inicial
- [x] Solicitação de permissão de microfone em tempo de execução
- [x] Reconhecimento de fala pt-BR usando o serviço de voz disponível no Android
- [x] Resposta por Text-to-Speech
- [x] Abrir YouTube, WhatsApp, Chrome e Configurações por comando
- [x] Pesquisa web por comando de voz
- [ ] Conversação com modelo de IA via backend seguro
- [ ] Palavra de ativação contínua "Jarvis"
- [ ] Serviço de voz em segundo plano / integração com assistente padrão
- [ ] Memória e preferências locais

## Requisitos

- Android 8.0 (API 26) ou superior
- Android 14 / HyperOS 2 testável
- Microfone e serviço de reconhecimento de fala disponíveis
- Internet para pesquisas web e reconhecimento quando o provedor exigir conexão

## Comandos iniciais

- "Abra o YouTube"
- "Abra o WhatsApp"
- "Abra o Chrome"
- "Abra as configurações"
- "Pesquise sobre inteligência artificial"

A captura de voz nesta versão é iniciada pelo botão do aplicativo. A palavra de ativação contínua com a tela apagada ainda não está implementada. Essa etapa exige serviço dedicado, integração com as restrições de execução em segundo plano do Android e testes específicos no HyperOS.

## Segurança

Não inclua chaves de API, tokens ou senhas no aplicativo nem no repositório. A futura integração com IA deverá passar por um backend que proteja as credenciais.

## Compilação

O workflow GitHub Actions em `.github/workflows/jarvis-android-build.yml` compila o APK de depuração e publica o artefato na execução do workflow.
