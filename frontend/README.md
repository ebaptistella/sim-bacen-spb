# Simulador BACEN — Frontend

Frontend ClojureScript do Simulador BACEN/SPB para mensagens STR de TED.

## Stack

- **ClojureScript** — linguagem
- **Reagent** — React wrapper
- **Re-frame** — state management (events, subscriptions, effects)
- **Shadow-CLJS** — build tool (compilação, hot reload, npm interop)
- **Tailwind CSS** — utility-first CSS (classes inline nos componentes)
- **core.async** — polling e retry com go-loops

## Estrutura de diretórios

```
src/com/github/ebaptistella/frontend/
├── core.cljs                      # Entry point: init, mount, router
├── db.cljs                        # Agregador de default-db
├── events.cljs                    # Agregador de event handlers
├── subs.cljs                      # Agregador de subscriptions
│
├── pages/
│   └── messages.cljs              # Page container (lifecycle, layout grid)
│
├── components/
│   ├── message_list.cljs          # Lista de mensagens (table desktop, cards mobile)
│   ├── message_detail_panel.cljs  # Painel lateral de detalhes
│   ├── respond_modal.cljs         # Modal de escolha de resposta (R1/reject)
│   ├── confirmation_modal.cljs    # Modal de confirmação antes de enviar
│   └── toast_notification.cljs    # Toast de feedback (success/error)
│
├── events/
│   ├── messages_events.cljs       # Fetch, polling, select/deselect
│   ├── respond_events.cljs        # Abrir/fechar modais, submit resposta
│   ├── router_events.cljs         # Navegação hash-based
│   └── toast_events.cljs          # Show/dismiss toast
│
├── subs/
│   ├── messages_subs.cljs         # Derivações do estado de mensagens
│   ├── respond_subs.cljs          # Estado dos modais de resposta
│   ├── router_subs.cljs           # Página/rota atual
│   └── toast_subs.cljs            # Toast atual
│
├── db/
│   ├── messages_db.cljs           # Initial state de mensagens
│   ├── respond_db.cljs            # Initial state dos modais
│   └── toast_db.cljs              # Initial state do toast
│
└── util/
    ├── http.cljs                  # Fetch com retry + timeout (AbortController)
    ├── format.cljs                # Formatação de datas (dd/mm/yyyy HH:mm) e valores (R$)
    └── router.cljs                # Hash-based routing + helpers de navegação
```

## Como rodar

```bash
# Instalar dependências Node (Shadow-CLJS)
npm install

# Desenvolvimento com hot reload
npx shadow-cljs watch app
# → http://localhost:3000

# Compilar (dev)
npx shadow-cljs compile app

# Build de produção (otimizado)
npx shadow-cljs release app

# Via lein aliases
lein dev        # watch
lein compile    # compile
lein build      # clean + release
```

## Styling — Tailwind CSS

**Iter 1 (atual):** Tailwind CDN via `<script src="https://cdn.tailwindcss.com"></script>` em `index.html`.
- ✅ Rápido para dev e prototipagem
- ⚠️ Overhead no browser (runtime JIT compilation)

**Iter 2 (planejado):** Build estático com PostCSS
- Configuração pronta em `tailwind.config.js` e `postcss.config.js`
- Adicionar devDependencies: `tailwindcss`, `postcss`, `autoprefixer`
- Compilar CSS: `npm run css:build` (ou integrar no pipeline de build)
- Servir `resources/public/css/style.min.css` em vez de CDN
- Reduz bundle size e melhora performance

Instruções para Iter 2:
```bash
npm install --save-dev tailwindcss postcss autoprefixer
npm run css:build
# → Gera resources/public/css/style.min.css
# Atualizar index.html: <link rel="stylesheet" href="/css/style.min.css">
```

## Rotas

| Hash | Página | Comportamento |
|------|--------|---------------|
| `#/messages` | Lista de mensagens | Default ao boot |
| `#/messages/:id` | Lista + detail panel aberto | Seta `selected-id` automaticamente |

## Fluxo de dados (Re-frame)

```
[UI Component] → dispatch [:event] → [Event Handler] → atualiza db
                                                           ↓
[UI Component] ← subscribe [:sub]  ← [Subscription]  ← db atualizado
```

### Ciclo principal

1. `core/init` → `dispatch-sync [:initialize-db]` → `router/init!` → `mount-root`
2. `messages-page` monta → `dispatch [:messages/fetch-initial]` + `[:messages/start-polling]`
3. Polling: `go-loop` a cada 4s faz `GET /api/v1/messages`
4. Clique em row → `dispatch [:messages/select-message id]` → detail panel abre
5. "Responder" → respond modal → confirmação → `POST /api/v1/messages/:id/respond`
6. Sucesso → toast verde + refresh da lista

## Como adicionar um novo tipo STR (Iter 2+)

A estrutura foi desenhada para reutilização. Para suportar um novo tipo (ex: STR0005):

### 1. Backend primeiro
Implemente `logic/str/str0005.clj` com geração de R1/R2 — o frontend não muda a API.

### 2. Frontend — o que NÃO precisa mudar
- **`message_list.cljs`** — já exibe qualquer tipo via campo `:type`
- **`message_detail_panel.cljs`** — já exibe campos genéricos
- **`util/http.cljs`** — endpoints são os mesmos (`/api/v1/messages`)
- **Polling, toast, router** — reutilizados integralmente

### 3. Frontend — o que PODE precisar de ajuste
- **`respond_modal.cljs`** — se o novo tipo tiver opções de resposta diferentes (ex: campos extras além de MotivoRejeicao), adicionar condição por `:type`
- **`message_detail_panel.cljs`** — se houver campos específicos do novo tipo que não existem em STR0008, adicionar renderização condicional
- **Novo componente (opcional)** — se o tipo tiver UI muito diferente, crie `components/str0005_detail.cljs` e renderize condicionalmente no detail panel

### Exemplo de extensão no respond_modal.cljs

```clojure
;; No respond-modal, adicionar opções por tipo:
(defn- response-options [msg-type]
  (case msg-type
    "STR0008" [{:type :accept :label "Aceitar (R1+R2)"}
               {:type :reject :label "Rejeitar (R1)"}]
    "STR0005" [{:type :accept :label "Aceitar (R1+R2)"}
               {:type :reject :label "Rejeitar (R1)"}]
    ;; Default genérico
    [{:type :accept :label "Aceitar"}
     {:type :reject :label "Rejeitar"}]))
```

### Padrão geral
```
Novo tipo STR → backend logic → mesmos endpoints → frontend renderiza automaticamente
                                                     ↳ ajustar modal/detail se campos diferem
```
