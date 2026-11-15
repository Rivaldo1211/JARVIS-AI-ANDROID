# JARVIS AI — Android

Assistente pessoal para Android, desenvolvido em Kotlin e Jetpack Compose. O projeto prioriza interação por voz, comandos no aparelho e uma memória pessoal local, sem exigir uma API de IA para essas funções.

<p align="center">
  <strong>JARVIS 0.6.0</strong><br>
  Assistente Android • Português brasileiro • Memória local
</p>

## Estado do projeto

| Recurso | Situação |
|---|---|
| Interface futurista JARVIS HUD | Implementada |
| Reconhecimento de fala pt-BR | Implementado, conforme serviço disponível no Android |
| Resposta falada (Text-to-Speech) | Implementada |
| Comandos para abrir aplicativos | Implementados para atalhos previstos no código |
| Pesquisa na web por comando | Implementada; abre o navegador |
| Memória pessoal persistente no aparelho | Implementada |
| Perfil adaptativo por informações declaradas | Implementado em nível básico |
| Conversação generativa com raciocínio amplo | Ainda não implementada |
| Modelo de linguagem executado offline | Planejado |
| Palavra de ativação contínua "Jarvis" | Planejada |
| Funcionamento contínuo com tela apagada | Não implementado |

## Como funciona atualmente

O aplicativo Android está em [`jarvis-android/`](jarvis-android/). Nesta versão, o núcleo de interação funciona por reconhecimento de fala, regras locais, comandos do aparelho e síntese de voz.

A memória identifica algumas declarações explícitas feitas pela pessoa, registra os dados no armazenamento privado do aplicativo e utiliza essas informações em respostas específicas. Isso é uma primeira camada de personalização; não é aprendizado autônomo irrestrito nem substitui um modelo de linguagem.

**A versão Android atual não depende de Gemini, OpenAI ou outro endpoint de IA para sua memória e comandos locais.** Uma integração antiga com backend/API não faz parte do fluxo atual do aplicativo.

## Memória pessoal e privacidade

As informações do perfil são armazenadas localmente no Android, em preferências privadas do aplicativo. O perfil não é enviado a um servidor pelo recurso de memória implementado nesta versão.

Exemplos de informações que o JARVIS reconhece quando declaradas:
- Nome: "Meu nome é Ana".
- Interesses: "Eu gosto de tecnologia".
- Preferências: "Prefiro respostas curtas".
- Trabalho: "Trabalho com logística".
- Objetivos: "Meu objetivo é aprender programação".
- Localidade: "Moro em Palotina".

Comandos disponíveis:
- "O que você sabe sobre mim?"
- "Qual é meu nome?"
- "Esqueça meu nome".
- "Apague minha memória".

A memória é apagada se a pessoa solicitar a exclusão completa ou se limpar os dados/desinstalar o aplicativo. O reconhecimento de fala pode utilizar o serviço configurado no Android; o comportamento de privacidade desse serviço é regido pelo respectivo provedor.

Consulte [a documentação do aplicativo](jarvis-android/README.md) para detalhes, exemplos e limitações.

## Requisitos

- Android 8.0 (API 26) ou superior.
- Microfone e permissão de gravação de áudio.
- Serviço de reconhecimento de fala disponível no dispositivo.
- Conexão com a internet para pesquisas web e, dependendo da configuração, para reconhecimento de fala.

## Gerar o APK pelo GitHub

O workflow [Build JARVIS Android APK](.github/workflows/jarvis-android-build.yml) executa automaticamente quando há alterações em `jarvis-android/` ou no próprio workflow. Também permite execução manual:

1. Abra a aba **Actions** deste repositório.
2. Selecione **Build JARVIS Android APK**.
3. Clique em **Run workflow** e confirme a branch `main`.
4. Aguarde o resultado ficar como **Success**.
5. Abra a execução concluída e baixe o artefato `jarvis-ai-debug-apk`.
6. Extraia o ZIP e instale o arquivo `app-debug.apk` no Android.

O APK gerado é de depuração (debug), não uma versão assinada para distribuição na Play Store. Se o Android informar conflito de assinatura ao atualizar uma instalação anterior, pode ser necessário desinstalar a versão antiga antes; isso apaga os dados locais desse aplicativo.

## Estrutura

```text
.
├── .github/
│   └── workflows/
│       └── jarvis-android-build.yml
├── jarvis-android/
│   ├── app/
│   │   └── src/main/
│   ├── build.gradle.kts
│   └── README.md
└── README.md
```

## Próximas etapas

1. Ampliar a memória para fatos, preferências e contexto com gestão transparente.
2. Criar uma tela para visualizar, editar e excluir informações salvas.
3. Incorporar um modelo de linguagem local, após avaliar desempenho e compatibilidade com o hardware.
4. Melhorar a qualidade da voz com uma opção neural compatível.
5. Pesquisar uma implementação segura de ativação por palavra-chave e serviço em segundo plano, respeitando as restrições do Android.

## Segurança

- Não inclua chaves de API, tokens, senhas ou credenciais no código, no APK ou em commits.
- Não registre informações pessoais sem transparência e possibilidade de exclusão.
- Antes de integrar serviços externos, documente quais dados são enviados, para qual finalidade e sob quais condições.

## Documentação

- [Documentação técnica e funcional do Android](jarvis-android/README.md)
- [Workflow de compilação](.github/workflows/jarvis-android-build.yml)

## Ativação por voz (0.6.0)

A detecção da palavra-chave usa o SDK Porcupine, com processamento de áudio no aparelho. Para habilitar, crie uma conta no [Picovoice Console](https://console.picovoice.ai/), gere uma AccessKey e cadastre-a no GitHub em **Settings → Secrets and variables → Actions → New repository secret**, com o nome `PICOVOICE_ACCESS_KEY`. Nunca coloque essa chave diretamente no código ou em commits. O workflow injeta o segredo somente durante a compilação.

No Android, abra o JARVIS e toque em **ATIVAR CHAMADA: JARVIS**. O Android exibirá a notificação permanente do serviço. A detecção pode continuar com a tela apagada enquanto o serviço permanecer ativo, mas o HyperOS pode encerrar processos em segundo plano. Quando a palavra for detectada com o app em primeiro plano, o reconhecimento de comando é iniciado automaticamente; se o app estiver totalmente em segundo plano, o sistema mostra uma notificação para tocar e continuar a conversa. A inicialização do serviço exige que o usuário abra o app e ative o recurso enquanto ele está visível, conforme as regras de microfone do Android 14+.
