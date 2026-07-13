# ==============================================================================
# 1. IMPORTAÇÃO DAS BIBLIOTECAS
# ==============================================================================
import streamlit as st
from PIL import Image
from google import genai
from google.genai import types # Importação necessária para forçar o JSON estrito
import pandas as pd
import json
from io import BytesIO

# ==============================================================================
# 2. CONFIGURAÇÃO DA CONEXÃO COM O GEMINI
# ==============================================================================
if "GEMINI_API_KEY" in st.secrets:
    MINHA_CHAVE_API = st.secrets["GEMINI_API_KEY"]
else:
    MINHA_CHAVE_API = "CHAVE_REMOVIDA_POR_SEGURANCA"

# ==============================================================================
# 3. INTERFACE WEB (STREAMLIT)
# ==============================================================================
st.set_page_config(page_title="Leitor de Fichas Otimizado", layout="wide")

st.title("📋 Sistema Inteligente de Controle de Fichas (Alta Velocidade)")
st.markdown("---")

arquivo_enviado = st.file_uploader(
    label="Faça o upload da foto ou print da ficha do motorista", 
    type=["png", "jpg", "jpeg"]
)

if arquivo_enviado is not None:
    col1, col2 = st.columns([1, 1.5])
    
    with col1:
        imagem = Image.open(arquivo_enviado)
        
        # ⚡ OTIMIZAÇÃO 1: Reduzir o tamanho da imagem para o upload ser instantâneo
        # Se a imagem for gigante, redimensionamos mantendo a qualidade para leitura
        if imagem.width > 1200:
            proporcao = 1200 / float(imagem.width)
            altura_alvo = int((float(imagem.height) * float(proporcao)))
            imagem = imagem.resize((1200, altura_alvo), Image.Resampling.LANCZOS)
            
        st.image(imagem, caption="Ficha Otimizada para Processamento", use_container_width=True)
    
    with col2:
        if st.button("🚀 Processar Ficha e Gerar Excel"):
            with st.spinner("Processando dados em alta velocidade..."):
                try:
                    # Instrução direta para o analista de IA
                    instrucao = (
                        "Você é um analista de dados logísticos. Analise a imagem da ficha fornecida e extraia "
                        "as informações de viagens e abastecimentos estruturando-as estritamente no esquema visual exigido."
                    )
                    
                    # ⚡ A SOLUÇÃO DEFINITIVA: Criamos a estrutura exata (Schema) que a IA DEVE seguir.
                    # Isso impede que ela adicione qualquer "dado extra" ou texto de conversa.
                    esquema_exigido = {
                        "type": "OBJECT",
                        "properties": {
                            "viagens": {
                                "type": "ARRAY",
                                "items": {
                                    "type": "OBJECT",
                                    "properties": {
                                        "PLACA": {"type": "STRING"},
                                        "MOTORISTA": {"type": "STRING"},
                                        "DATA": {"type": "STRING"},
                                        "PESO": {"type": "STRING"},
                                        "DE": {"type": "STRING"},
                                        "PARA": {"type": "STRING"},
                                        "VALOR FRETE": {"type": "STRING"}
                                    }
                                }
                            },
                            "abastecimento": {
                                "type": "ARRAY",
                                "items": {
                                    "type": "OBJECT",
                                    "properties": {
                                        "DATA": {"type": "STRING"},
                                        "PLACA": {"type": "STRING"},
                                        "LITROS": {"type": "STRING"},
                                        "KM": {"type": "STRING"},
                                        "VALOR": {"type": "STRING"},
                                        "POSTO": {"type": "STRING"}
                                    }
                                }
                            }
                        }
                    }
                    
                    # Passamos o esquema rígido na configuração da API
                    configuracao_json = types.GenerateContentConfig(
                        response_mime_type="application/json",
                        response_schema=esquema_exigido, # <-- Força a IA a usar a nossa forma de bolo
                        temperature=0.1
                    )
                    
                    # Chamada do Gemini
                    resposta = cliente_gemini.models.generate_content(
                        model='gemini-3.5-flash',
                        contents=[imagem, instrucao],
                        config=configuracao_json
                    )
                    
                    # Agora o texto vem 100% limpo, sem caracteres extras!
                    dados_texto = resposta.text.strip()
                    dados_json = json.loads(dados_texto)
                    
                    # ----------------------------------------------------------
                    # CRIANDO AS TABELAS COM PANDAS
                    # ----------------------------------------------------------
                    df_viagens = pd.DataFrame(dados_json.get("viagens", []))
                    df_abastecimento = pd.DataFrame(dados_json.get("abastecimento", []))
                    
                    colunas_viagens = ["PLACA", "MOTORISTA", "DATA", "PESO", "DE", "PARA", "VALOR FRETE"]
                    colunas_abastecimento = ["DATA", "PLACA", "LITROS", "KM", "VALOR", "POSTO"]
                    
                    if df_viagens.empty:
                        df_viagens = pd.DataFrame(columns=colunas_viagens)
                    if df_abastecimento.empty:
                        df_abastecimento = pd.DataFrame(columns=colunas_abastecimento)
                        
                    df_viagens = df_viagens.reindex(columns=colunas_viagens).fillna("")
                    df_abastecimento = df_abastecimento.reindex(columns=colunas_abastecimento).fillna("")
                    
                    # Exibe os resultados na tela
                    st.write("### 🚛 Tabela de Viagens")
                    st.dataframe(df_viagens, use_container_width=True)
                    
                    st.write("### ⛽ Tabela de Abastecimento")
                    st.dataframe(df_abastecimento, use_container_width=True)
                    
                    # ----------------------------------------------------------
                    # GERANDO O EXCEL
                    # ----------------------------------------------------------
                    output = BytesIO()
                    with pd.ExcelWriter(output, engine='openpyxl') as writer:
                        df_viagens.to_excel(writer, sheet_name='Viagens', index=False)
                        df_abastecimento.to_excel(writer, sheet_name='Abastecimento', index=False)
                    
                    dados_excel = output.getvalue()
                    
                    st.markdown("---")
                    st.success("📊 Tabelas geradas com sucesso!")
                    
                    st.download_button(
                        label="📥 Baixar Dados em Excel (.xlsx)",
                        data=dados_excel,
                        file_name="controle_motoristas.xlsx",
                        mime="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )
                    
                except Exception as erro:
                    st.error(f"Erro no processamento: {erro}")
else:
    st.info("💡 Aguardando o upload de uma ficha para organizar as planilhas.")