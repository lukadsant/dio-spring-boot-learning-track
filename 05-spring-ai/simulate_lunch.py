#!/usr/bin/env python3
"""
Simulação Automatizada — Horário de Pico do Almoço no Izakaya (11:30 às 14:30)
Exercita todos os 8 Use Cases da aplicação Spring AI integrada à vitrine 3D Jidouhanbaiki:
  1. list-restaurant-menu
  2. persist-transaction
  3. create-restaurant-order
  4. list-restaurant-orders
  5. close-table-order
  6. list-transactions-by-category
  7. list-all-transactions
  8. summarize-transactions
"""

import urllib.request
import urllib.error
import json
import time
from datetime import datetime

BASE_URL = "http://localhost:8089"
LOG_FILE = "05-spring-ai/simulation-lunch-rush.log"

def http_req(path, method="GET", data=None):
    url = f"{BASE_URL}{path}"
    headers = {"Content-Type": "application/json"}
    body = json.dumps(data).encode("utf-8") if data else None
    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    t0 = time.perf_counter()
    try:
        with urllib.request.urlopen(req, timeout=90) as response:
            status = response.status
            resp_body = response.read().decode("utf-8")
            elapsed = time.perf_counter() - t0
            parsed = json.loads(resp_body) if resp_body else {}
            return status, parsed, elapsed, None
    except urllib.error.HTTPError as e:
        elapsed = time.perf_counter() - t0
        err_msg = e.read().decode("utf-8")
        return e.code, None, elapsed, err_msg
    except Exception as e:
        elapsed = time.perf_counter() - t0
        return 0, None, elapsed, str(e)

class Logger:
    def __init__(self, filepath):
        self.file = open(filepath, "w", encoding="utf-8")

    def log(self, text=""):
        print(text)
        self.file.write(text + "\n")
        self.file.flush()

    def step(self, step_num, sim_time, title, actor, use_case):
        bar = "=" * 78
        self.log(f"\n{bar}")
        self.log(f"⏰ [{sim_time}] ETAPA {step_num}: {title}")
        self.log(f"👤 Ator: {actor} | 🛠️ Use Case / Tool: {use_case}")
        self.log(f"{'-' * 78}")

    def close(self):
        self.file.close()

def run_simulation():
    logger = Logger(LOG_FILE)
    logger.log("=" * 78)
    logger.log("🏮 SIMULAÇÃO DE TURNO DE ALMOÇO — RESTAURANTE IZAKAYA & JIDOUHANBAIKI")
    logger.log(f"Início da Execução Real: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    logger.log("Servidor Alvo: http://localhost:8089")
    logger.log("Modelo de IA: hermes3:8b (Ollama local)")
    logger.log("=" * 78)

    # 1. 11:30 - Consulta do Cardápio
    logger.step(1, "11:30", "Abertura do Turno & Consulta ao Cardápio", "🧑‍🍳 Gerente de Salão", "ListRestaurantMenuUseCase")
    status, menu, elapsed, err = http_req("/api/restaurant/menu")
    logger.log(f"HTTP Status: {status} (tempo: {elapsed:.2f}s)")
    if menu:
        logger.log(f"Total de itens no cardápio: {len(menu)}")
        for item in menu:
            code = item.get("code")
            name = item.get("name")
            price = item.get("priceReais", item.get("priceCents", 0) / 100)
            tag = "[Vitrine 3D]" if code.startswith("A") or code.startswith("B") else "[Cozinha]"
            logger.log(f"  • {tag} {code:<3} | {name:<35} | R$ {price:.2f}")

    # 2. 11:45 - Insumos da Cozinha (PersistTransactionUseCase via IA)
    logger.step(2, "11:45", "Compra Emergencial de Insumos da Cozinha", "👨‍🍳 Chefe de Cozinha", "PersistTransactionUseCase")
    prompt = "Comprei 95 reais em peixe fresco no mercado para o almoço"
    logger.log(f"Mensagem enviada à IA: \"{prompt}\"")
    status, chat_resp, elapsed, err = http_req("/api/restaurant/chat", "POST", {
        "message": prompt,
        "role": "STAFF"
    })
    logger.log(f"HTTP Status: {status} (tempo IA: {elapsed:.2f}s)")
    logger.log(f"Resposta do Agente: {chat_resp.get('reply', err)}")

    # 3. 12:10 - Chegada da Mesa 1 (Cliente pede via totem/app)
    logger.step(3, "12:10", "Chegada e Pedido da Mesa 1", "🍜 Cliente na Mesa 1", "CreateRestaurantOrderUseCase")
    prompt_m1 = "Gostaria de pedir 2 Tonkotsu Ramen Especial e 2 Yuzu Citrus Milk para a nossa mesa"
    logger.log(f"Mensagem enviada pelo Cliente: \"{prompt_m1}\"")
    status, chat_resp, elapsed, err = http_req("/api/restaurant/chat", "POST", {
        "message": prompt_m1,
        "role": "CUSTOMER",
        "tableNumber": 1
    })
    logger.log(f"HTTP Status: {status} (tempo IA: {elapsed:.2f}s)")
    logger.log(f"Resposta do Agente: {chat_resp.get('reply', err)}")

    # 4. 12:30 - Chegada da Mesa 3 (Grupo Corporativo)
    logger.step(4, "12:30", "Pedido da Mesa 3 (Almoço de Equipe)", "🍜 Cliente na Mesa 3", "CreateRestaurantOrderUseCase")
    prompt_m3 = "Para a mesa 3: queremos 3 Katsu Sando Wagyu e 3 Matcha Latte Okinawa"
    logger.log(f"Mensagem enviada: \"{prompt_m3}\"")
    status, chat_resp, elapsed, err = http_req("/api/restaurant/chat", "POST", {
        "message": prompt_m3,
        "role": "CUSTOMER",
        "tableNumber": 3
    })
    logger.log(f"HTTP Status: {status} (tempo IA: {elapsed:.2f}s)")
    logger.log(f"Resposta do Agente: {chat_resp.get('reply', err)}")

    # 5. 12:50 - Mesa 4 pede adicionais via Garçom
    logger.step(5, "12:50", "Garçom Registra Pedido da Mesa 4", "🧑‍🍳 Garçom", "CreateRestaurantOrderUseCase")
    prompt_m4 = "Abrir pedido mesa 4: 2 Gyoza Tradicional e 2 Sakura Sparkling Soda"
    logger.log(f"Mensagem do Garçom: \"{prompt_m4}\"")
    status, chat_resp, elapsed, err = http_req("/api/restaurant/chat", "POST", {
        "message": prompt_m4,
        "role": "STAFF"
    })
    logger.log(f"HTTP Status: {status} (tempo IA: {elapsed:.2f}s)")
    logger.log(f"Resposta do Agente: {chat_resp.get('reply', err)}")

    # 6. 13:10 - Monitoramento do Salão (ListRestaurantOrdersUseCase)
    logger.step(6, "13:10", "Auditoria de Comandas Abertas no Salão", "🧑‍🍳 Gerente de Salão", "ListRestaurantOrdersUseCase")
    status, orders, elapsed, err = http_req("/api/restaurant/orders")
    logger.log(f"HTTP Status: {status} (tempo: {elapsed:.2f}s)")
    open_orders = [o for o in orders if o.get("status") == "OPEN"]
    logger.log(f"Total de comandas abertas no momento de pico: {len(open_orders)}")
    for o in open_orders:
        logger.log(f"  • Mesa {o.get('tableNumber')} | Itens: {o.get('itemsDescription')} | Total: R$ {o.get('totalAmountReais'):.2f}")

    # 7. 13:25 - Fechamento Mesa 1 (CloseTableOrderUseCase)
    logger.step(7, "13:25", "Cliente da Mesa 1 Pede Fechamento da Conta", "🍜 Cliente na Mesa 1", "CloseTableOrderUseCase")
    prompt_close_m1 = "Por favor, fechar a comanda da mesa 1"
    logger.log(f"Pedido de Fechamento: \"{prompt_close_m1}\"")
    status, chat_resp, elapsed, err = http_req("/api/restaurant/chat", "POST", {
        "message": prompt_close_m1,
        "role": "CUSTOMER",
        "tableNumber": 1
    })
    logger.log(f"HTTP Status: {status} (tempo IA: {elapsed:.2f}s)")
    logger.log(f"Resposta do Agente: {chat_resp.get('reply', err)}")

    # 8. 13:40 - Reposição da Vitrine 3D Jidouhanbaiki
    logger.step(8, "13:40", "Reposição de Bebidas da Vitrine Jidouhanbaiki", "📦 Estoquista / Caixa", "PersistTransactionUseCase")
    prompt_rep = "Comprei 120 reais em latas de refrigerante artesanal e cha para repor a vitrine 3D"
    logger.log(f"Instrução: \"{prompt_rep}\"")
    status, chat_resp, elapsed, err = http_req("/api/restaurant/chat", "POST", {
        "message": prompt_rep,
        "role": "STAFF"
    })
    logger.log(f"HTTP Status: {status} (tempo IA: {elapsed:.2f}s)")
    logger.log(f"Resposta do Agente: {chat_resp.get('reply', err)}")

    # 9. 14:05 - Fechamento das Mesas 3 e 4
    logger.step(9, "14:05", "Fechamento das Mesas 3 e 4 no Caixa", "🧑‍🍳 Operador de Caixa", "CloseTableOrderUseCase")
    for tbl in [3, 4]:
        status, closed_order, elapsed, err = http_req(f"/api/restaurant/orders/{tbl}/close", "POST")
        if status in [200, 201]:
            logger.log(f"  ✅ Mesa {tbl} liquidada com sucesso | Total: R$ {closed_order.get('totalAmountReais', 0):.2f} (lançado em RESTAURANT)")
        else:
            logger.log(f"  ⚠️ Mesa {tbl} retorno: {status} - {err}")

    # 10. 14:20 - Auditoria por Categoria (ListTransactionsByCategoryUseCase)
    logger.step(10, "14:20", "Auditoria de Entradas e Saídas por Categoria", "📊 Auditor Financeiro", "ListTransactionsByCategoryUseCase")
    for cat in ["RESTAURANT", "INGREDIENTS", "BEVERAGES"]:
        status, trans_list, elapsed, err = http_req(f"/transactions/{cat}")
        total_cat = sum(t.get("amount", 0) for t in (trans_list or [])) / 100.0
        logger.log(f"Categoria {cat:<12}: {len(trans_list or []):>2} lançamentos | Subtotal: R$ {total_cat:.2f}")

    # 11. 14:25 - Extrato Geral de Todas as Transações (ListAllTransactionsUseCase)
    logger.step(11, "14:25", "Extrato Geral Consolidado", "📊 Gerente Geral", "ListAllTransactionsUseCase")
    status, all_trans, elapsed, err = http_req("/transactions")
    logger.log(f"HTTP Status: {status} | Total de Lançamentos de Caixa: {len(all_trans or [])}")
    for t in (all_trans or []):
        cat = t.get("category")
        val = t.get("amount", 0) / 100.0
        desc = t.get("description", "")
        logger.log(f"  • [{cat:<10}] R$ {val:>7.2f} - {desc}")

    # 12. 14:30 - Fechamento Final do Turno de Almoço (SummarizeTransactionsUseCase)
    logger.step(12, "14:30", "Relatório de Fechamento do Turno de Almoço", "🧑‍🍳 Gerente Geral", "SummarizeTransactionsUseCase")
    status, summary, elapsed, err = http_req("/api/restaurant/summary")
    logger.log(f"HTTP Status: {status} (tempo: {elapsed:.2f}s)")

    logger.log("\n" + "=" * 78)
    logger.log("📈 BALANÇO FINANCEIRO FINAL DO TURNO DE ALMOÇO")
    logger.log("=" * 78)
    logger.log(f"Total Movimentado no Caixa: R$ {summary.get('totalAmountReais', 0):.2f}")
    logger.log(f"Quantidade de Lançamentos:   {summary.get('totalTransactions', 0)}")
    logger.log("Distribuição por Categoria:")
    cats = summary.get("totalsByCategoryReais", {})
    for k, v in cats.items():
        logger.log(f"  • {k:<15}: R$ {v:.2f}")

    vendas = cats.get("RESTAURANT", 0.0)
    custos = cats.get("INGREDIENTS", 0.0) + cats.get("BEVERAGES", 0.0)
    lucro = vendas - custos
    logger.log("-" * 78)
    logger.log(f"💵 Faturamento de Vendas (Almoço): R$ {vendas:.2f}")
    logger.log(f"🛒 Custos Operacionais (Insumos):   R$ {custos:.2f}")
    logger.log(f"⭐ Lucro Operacional do Almoço:     R$ {lucro:.2f}")
    logger.log("=" * 78)
    logger.log(f"Simulação finalizada com sucesso em: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    logger.close()
    print(f"\nArquivo de log gerado em: {LOG_FILE}")

if __name__ == "__main__":
    run_simulation()
