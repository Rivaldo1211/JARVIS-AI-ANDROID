# JARVIS AI — Android

Assistente pessoal para Android, desenvolvido em Kotlin.

## Aplicativo Android
O aplicativo está em `jarvis-android/`. A versão inicial oferece interface nativa, reconhecimento de fala em português, síntese de voz e atalhos locais.

## Backend de inteligência artificial
A API está em `jarvis-backend/` e usa FastAPI + Google Gemini. A chave `GEMINI_API_KEY` é lida exclusivamente como variável de ambiente no servidor. **Nunca coloque uma chave real no código, no APK, em arquivos versionados ou em mensagens públicas.**

### Publicação de teste no Render
1. Entre em https://render.com e conecte sua conta GitHub.
2. Crie um serviço usando **New → Blueprint** e selecione este repositório.
3. O Render identificará `jarvis-backend/render.yaml`. Confirme o serviço `jarvis-ai-api` no plano Free.
4. Gere uma chave em https://aistudio.google.com/app/apikey.
5. No painel do serviço Render, abra **Environment** e cadastre `GEMINI_API_KEY` com o valor da chave. Não a envie neste repositório nem neste chat.
6. Salve e aguarde o deploy. Copie a URL pública do serviço (por exemplo, `https://jarvis-ai-api.onrender.com`). No GitHub, abra **Settings → Secrets and variables → Actions → Variables → New repository variable** e crie a variável `JARVIS_API_URL` com essa URL HTTPS. Ela é um endereço público, não uma chave secreta. Depois, execute novamente o workflow **Build JARVIS Android APK** na aba Actions para gerar o aplicativo conectado ao servidor. Teste `https://SEU-SERVICO.onrender.com/health`; a resposta esperada contém `"status":"ok"` e `"model_configured":true`.

O plano gratuito do Render pode suspender o serviço após inatividade; a primeira chamada posterior pode demorar para reativá-lo. O uso do Gemini depende dos limites e das condições vigentes na conta Google.

## Compilação Android
O workflow GitHub Actions em `.github/workflows/jarvis-android-build.yml` compila o APK de depuração e publica o artefato.

## Verificações do backend
O workflow `.github/workflows/jarvis-backend-check.yml` verifica a sintaxe do backend.

## Estado
A API Gemini está preparada no repositório, mas ainda precisa ser publicada e configurada. O aplicativo Android ainda precisa receber a URL pública do backend para iniciar conversas com IA. A palavra de ativação contínua "Jarvis" e a execução confiável com tela apagada continuam pendentes de implementação e testes.

## Segurança
Não armazene chaves de API, tokens ou credenciais no aplicativo ou neste repositório.
