# JARVIS AI — Android

Assistente pessoal para Android, desenvolvido em Kotlin e Jetpack Compose. O projeto prioriza interação por voz, comandos no aparelho e uma memória pessoal local.

<p align="center">
  <strong>JARVIS 0.7.0</strong><br>
  Assistente Android • Português brasileiro • Memória local • Wake word offline
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
| Detecção local da frase "Hey Jarvis" | Integrada ao código; aguardando validação do APK e teste no aparelho |
| Conversação generativa com raciocínio amplo | Ainda não implementada |
| Modelo de linguagem executado offline | Planejado |
| Funcionamento contínuo com tela apagada | Depende das regras do Android e do gerenciamento de bateria do fabricante |

## Como funciona atualmente

O aplicativo Android está em [`jarvis-android/`](jarvis-android/). O núcleo de interação usa reconhecimento de fala, regras locais, comandos do aparelho e síntese de voz.

A memória identifica algumas declarações explícitas feitas pela pessoa, registra os dados no armazenamento privado do aplicativo e utiliza essas informações em respostas específicas. Isso é uma primeira camada de personalização; não é aprendizado autônomo irrestrito nem substitui um modelo de linguagem.

A detecção da frase de ativação é feita pelo openWakeWord com inferência ONNX no próprio aparelho. Não exige conta, AccessKey ou serviço de nuvem para detectar a palavra-chave. O reconhecimento do comando depois da ativação continua dependendo do serviço de fala configurado no Android.

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

1. Validar a detecção de "Hey Jarvis" no Xiaomi Mi 11 Lite, inclusive consumo de bateria e estabilidade em segundo plano.
2. Ampliar a memória para fatos, preferências e contexto com gestão transparente.
3. Criar uma tela para visualizar, editar e excluir informações salvas.
4. Incorporar um modelo de linguagem local, após avaliar desempenho e compatibilidade com o hardware.
5. Melhorar a qualidade da voz com uma opção neural compatível.

## Segurança e licenças

- Não inclua chaves de API, tokens, senhas ou credenciais no código, no APK ou em commits.
- Não registre informações pessoais sem transparência e possibilidade de exclusão.
- Antes de integrar serviços externos, documente quais dados são enviados, para qual finalidade e sob quais condições.
- A implementação openWakeWord-Android utilizada declara código Apache 2.0, mas os modelos pré-treinados openWakeWord são licenciados sob CC BY-NC-SA 4.0. Essa licença restringe o uso comercial e exige atenção à atribuição e ao compartilhamento sob a mesma licença. Não use esse modelo em distribuição comercial sem substituir por um modelo com licença compatível.

## Documentação

- [Documentação técnica e funcional do Android](jarvis-android/README.md)
- [Workflow de compilação](.github/workflows/jarvis-android-build.yml)

## Ativação por voz — 0.7.0

1. Abra o JARVIS e conceda a permissão de microfone e notificações.
2. Toque em **ATIVAR CHAMADA: JARVIS**.
3. Diga **"Hey Jarvis"** para acionar o detector integrado.

A detecção usa openWakeWord e ONNX Runtime no aparelho, sem AccessKey, conta ou mensalidade. O modelo pré-treinado utilizado tem licença CC BY-NC-SA 4.0 e é adequado apenas a usos compatíveis com essa licença, não a exploração comercial sem autorização/licença alternativa.

O serviço de microfone em primeiro plano mantém uma notificação visível. A detecção pode continuar com a tela apagada enquanto o Android mantiver o serviço ativo, mas o HyperOS pode encerrar processos por economia de bateria. Quando o app está em primeiro plano, a detecção inicia o reconhecimento do comando; em segundo plano, o Android pode exigir que a pessoa toque na notificação para continuar. A inicialização do serviço deve ser feita com o aplicativo visível, respeitando as restrições do Android 14+.


## Pesquisa por voz e leitura de tela — versão 0.8.0

O JARVIS pode abrir pesquisas por comando de voz no Google e no YouTube. Ao iniciar uma pesquisa, o aplicativo arma uma leitura única da página de resultados e o serviço de acessibilidade lê o texto disponível quando a página carrega.

Exemplos:
- "Jarvis, o Flamengo joga hoje?" — pesquisa no Google com a data atual.
- "Jarvis, pesquise gols do Flamengo no YouTube" — abre os resultados do YouTube.
- "Jarvis, leia a tela" — solicita a leitura do conteúdo acessível que estiver visível.

### Ativar leitura de tela
1. Abra o JARVIS e toque em **ATIVAR LEITURA DE TELA**.
2. Nas configurações de Acessibilidade do Android, selecione **JARVIS AI - Leitura de tela** e ative o serviço.
3. Autorize o serviço. Ele só tenta extrair e falar o conteúdo quando uma pesquisa é iniciada pelo JARVIS ou quando você solicita a leitura.
4. Para interromper, desative o serviço nas configurações de Acessibilidade.

A leitura depende dos elementos que o aplicativo de destino disponibiliza à acessibilidade. Algumas páginas, vídeos, anúncios e conteúdos protegidos podem não expor texto legível. O JARVIS lê o texto extraído da página; não valida por conta própria a veracidade dos resultados e não substitui uma API esportiva estruturada. O serviço de acessibilidade tem acesso ao conteúdo da janela ativa enquanto estiver habilitado; mantenha-o ativado apenas se concordar com essa permissão.
