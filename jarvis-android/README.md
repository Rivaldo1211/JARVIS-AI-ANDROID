# JARVIS Android — Documentação do aplicativo

**Versão do código:** 0.8.1 (teste sem Acessibilidade)  
**Plataforma:** Android 8.0 (API 26) ou superior  
**Tecnologias:** Kotlin, Jetpack Compose, Android SpeechRecognizer, Text-to-Speech, openWakeWord e ONNX Runtime

## 1. Visão geral

O JARVIS é um assistente pessoal Android com interface futurista, interação por voz, comandos locais, memória pessoal persistente e detecção local da frase de ativação.

O núcleo atual não precisa de API de IA ou servidor externo para salvar o perfil, consultar informações memorizadas e executar os comandos locais implementados.

## 2. Funcionalidades implementadas

- Interface JARVIS HUD em Jetpack Compose.
- Reconhecimento de fala em português brasileiro, conforme disponibilidade do serviço Android.
- Respostas faladas por Text-to-Speech, com ajustes de ritmo e tom.
- Atalhos de voz para YouTube, WhatsApp, Chrome e Configurações.
- Pesquisa na web por comando, abrindo o navegador.
- Memória pessoal persistente no armazenamento privado do aplicativo.
- Reconhecimento de declarações explícitas sobre nome, interesses, preferências, trabalho, objetivos, localidade e estilo de resposta.
- Detecção da frase "Hey Jarvis" com openWakeWord e processamento ONNX local, sem chave de API ou conta.

## 3. Ativação por voz

1. Abra o aplicativo.
2. Conceda a permissão de microfone e, no Android 13+, a permissão de notificações.
3. Toque em **ATIVAR CHAMADA: JARVIS**.
4. Diga **"Hey Jarvis"** para acionar o detector.
5. Com o aplicativo visível, o reconhecimento do comando é iniciado automaticamente. Se o aplicativo estiver em segundo plano, o Android pode exigir que a pessoa toque na notificação antes de continuar.

O detector utiliza o modelo integrado `HEY_JARVIS` da biblioteca openWakeWord Android. O modelo reconhece a frase "Hey Jarvis"; não é a palavra isolada "Jarvis".

### Custo e licenciamento

- Não há AccessKey, conta Picovoice, cobrança por chamada ou mensalidade.
- O código da biblioteca openWakeWord Android declara licença Apache 2.0.
- Os modelos pré-treinados openWakeWord são licenciados sob CC BY-NC-SA 4.0. Essa licença não permite uso comercial sem autorização compatível e exige atribuição e compartilhamento nos termos da licença.
- Para transformar o JARVIS em produto comercial, substitua o modelo por outro com licença comercialmente compatível ou treine/obtenha um modelo com direitos adequados antes da distribuição.

## 4. Como ensinar informações ao JARVIS

Fale de forma direta, por exemplo:

| Diga ao JARVIS | Informação que ele registra |
|---|---|
| "Meu nome é Ana" | Nome |
| "Eu gosto de tecnologia" | Interesse |
| "Prefiro respostas curtas" | Preferência |
| "Trabalho com logística" | Trabalho |
| "Meu objetivo é aprender programação" | Objetivo |
| "Moro em Palotina" | Localidade |

As informações são salvas localmente. Se uma declaração não seguir os padrões reconhecidos pelo código, o JARVIS pode não identificá-la como informação de perfil.

## 5. Consultar e administrar a memória

| Comando | Resultado |
|---|---|
| "O que você sabe sobre mim?" | Lê o resumo das informações registradas |
| "Qual é meu nome?" | Informa o nome salvo, se houver |
| "Esqueça meu nome" | Exclui somente o nome |
| "Apague minha memória" | Exclui todas as informações pessoais salvas |

A exclusão completa também pode ser feita limpando os dados do aplicativo nas configurações do Android ou desinstalando-o.

## 6. Armazenamento e privacidade

O perfil é guardado em `SharedPreferences`, no espaço privado do aplicativo. A funcionalidade de memória não transmite o perfil para uma API ou backend.

A detecção da frase de ativação é processada localmente pelo modelo ONNX. A entrada de voz depois da ativação utiliza o serviço de reconhecimento de fala disponível/configurado no aparelho. Esse serviço pode pertencer ao Google ou a outro provedor, e suas práticas de processamento e privacidade são independentes do armazenamento local do JARVIS.

Não use a memória do aplicativo como cofre de senhas, dados bancários, documentos ou outras informações altamente sensíveis.

## 7. Limitações conhecidas

A memória atual é baseada em padrões de frases e regras locais. Ela não interpreta qualquer frase com compreensão humana, não aprende automaticamente tudo o que é dito e não possui raciocínio generativo amplo.

Ainda não estão implementados:
- Modelo de linguagem local embarcado.
- Conversação aberta com raciocínio avançado totalmente offline.
- Aprendizado automático de padrões de comportamento.
- Ativação garantida com a tela apagada em todos os aparelhos.
- Voz neural masculina de alta naturalidade integrada ao aplicativo.

A qualidade do reconhecimento de fala e da voz sintetizada também depende dos serviços e vozes instalados no Android.

## 8. Requisitos e permissões

- Android 8.0 (API 26) ou superior.
- Permissão de microfone para detectar a frase de ativação e reconhecer comandos.
- Permissão de notificações para exibir o estado do serviço.
- Serviço de reconhecimento de fala disponível.
- Conexão com a internet para abrir pesquisas na web e quando o serviço de voz exigir.

## 9. Compilação e instalação

A compilação é feita pelo GitHub Actions, sem necessidade de compilar no computador local:

1. Abra a aba **Actions** do repositório.
2. Selecione **Build JARVIS Android APK**.
3. Clique em **Run workflow** e escolha `main`.
4. Aguarde o workflow terminar com sucesso.
5. Baixe o artefato `jarvis-ai-no-accessibility-debug-apk`.
6. Extraia o ZIP e instale `app-debug.apk`.
7. Conceda as permissões de microfone e notificações.

O artefato é um APK debug. Se houver incompatibilidade de assinatura ao atualizar, desinstale a versão anterior antes de instalar a nova. A desinstalação remove a memória local.

## 10. Próximas evoluções

1. Criar uma área de perfil para revisar, corrigir e excluir cada informação.
2. Melhorar a extração de fatos e o reconhecimento de contexto em português.
3. Implementar um mecanismo de memória com categorias, origem e confirmação do usuário.
4. Avaliar modelos de linguagem locais adequados ao hardware Android.
5. Integrar voz neural opcional e controles de privacidade.
6. Testar estabilidade, consumo de bateria e comportamento do serviço em segundo plano no Xiaomi Mi 11 Lite.

---

Para a visão geral do projeto, consulte o [README principal](../README.md).


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


## Versão de teste 0.8.1 — sem serviço de Acessibilidade

Nesta compilação, o serviço de leitura de tela não é declarado no Manifest e o botão para ativá-lo foi removido. Os comandos relacionados à leitura de tela informam que o recurso está temporariamente desativado. A pesquisa por voz continua abrindo o navegador, mas não lê automaticamente os resultados. Esta versão serve para isolar o impacto da permissão de Acessibilidade no alerta do Play Protect; não garante que o alerta será removido.
