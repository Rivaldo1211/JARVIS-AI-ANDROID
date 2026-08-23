# GEM Leitor

Aplicação web para **leitura inteligente de fichas operacionais**, extração estruturada de dados logísticos e geração automática de planilhas Excel.

O projeto utiliza visão computacional/multimodal com a API do Google Gemini para interpretar imagens de fichas de motoristas e transformar as informações em dados estruturados de **viagens** e **abastecimentos**.

## Visão geral

O fluxo da aplicação é simples:

1. o usuário envia uma imagem da ficha (`PNG`, `JPG` ou `JPEG`);
2. a imagem é otimizada antes do processamento;
3. o Gemini analisa o conteúdo visual;
4. a resposta é validada em um schema JSON definido pela aplicação;
5. os dados são organizados com Pandas;
6. o sistema exibe as tabelas na interface;
7. um arquivo Excel é gerado com abas separadas para viagens e abastecimentos.

## Tecnologias

- **Python**
- **Streamlit**
- **Google Gemini API (`google-genai`)**
- **Pandas**
- **Pillow**
- **OpenPyXL**

## Dados extraídos

### Viagens

- Placa
- Motorista
- Data
- Peso
- Origem
- Destino
- Valor do frete

### Abastecimentos

- Data
- Placa
- Litros
- KM
- Valor
- Posto

## Estrutura do projeto

```text
GEM_LEITOR/
├── app.py
├── requirements.txt
└── README.md
```

## Como executar localmente

Clone o repositório e entre na pasta do projeto:

```bash
git clone https://github.com/Rivaldo1211/GEM_LEITOR.git
cd GEM_LEITOR
```

Crie e ative um ambiente virtual:

```bash
python -m venv .venv
```

Linux/macOS:

```bash
source .venv/bin/activate
```

Windows PowerShell:

```powershell
.\.venv\Scripts\Activate.ps1
```

Instale as dependências:

```bash
pip install -r requirements.txt
```

Configure a chave da API do Gemini nos secrets do Streamlit:

```toml
# .streamlit/secrets.toml
GEMINI_API_KEY = "sua_chave_aqui"
```

Execute a aplicação:

```bash
streamlit run app.py
```

## Segurança

A chave da API não deve ser versionada no repositório. A aplicação utiliza `st.secrets` para carregar `GEMINI_API_KEY` fora do código-fonte.

## Objetivo do projeto

Este projeto foi criado para reduzir trabalho manual na transcrição de dados operacionais, transformando fichas em informações estruturadas que podem ser utilizadas em relatórios, controles e análises logísticas.

## Autor

**Rivaldo Sousa do Nascimento**

Foco em logística, análise de dados, automação de processos e desenvolvimento de soluções para operações de transporte.
