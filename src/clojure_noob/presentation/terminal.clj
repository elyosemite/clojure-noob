(ns clojure-noob.presentation.terminal
  "Pure formatting functions: data in, string out. No printing here."
  (:require [clojure.string :as str]))

(defn- pad-left [width s]
  (let [s (str s)]
    (str (apply str (repeat (- width (count s)) \space)) s)))

(defn- pad-right [width s]
  (let [s (str s)]
    (str s (apply str (repeat (- width (count s)) \space)))))

(defn- fmt-price
  "Two decimals with a dot, regardless of the system locale."
  [p]
  (if (number? p)
    (.toPlainString (.setScale (bigdec p) 2 java.math.RoundingMode/HALF_UP))
    (pr-str p)))

(defn- fmt-line [id symbol quantity price]
  (str (pad-left 4 id) " " symbol " " (pad-left 5 quantity) " @ " (fmt-price price)))

(def ^:private side-label
  {:buy  "[COMPRA]"
   :sell "[VENDA] "})

(defn- status-text [{:keys [status remaining]}]
  (case status
    :open             "OK registrada"
    :partially-filled (str "OK executada parcialmente (resta " remaining ")")
    :filled           "OK executada"
    (str "OK " (name status))))

(defn format-order-result
  "One line describing an order attempt: `side`, `params` sent and `result` received."
  [side {:keys [symbol quantity price]} {:keys [ok error]}]
  (str (side-label side) " "
       (if ok
         (str (fmt-line (str "#" (:id ok)) (:symbol ok) (:quantity ok) (:price ok))
              "  " (status-text ok))
         (str (fmt-line "" symbol quantity price)
              "  REJEITADA: " (:message error)))))

(defn format-cancel-result
  "One line describing a cancel attempt of order `id`."
  [id {:keys [ok error]}]
  (str "[CANCELA] "
       (if ok
         (str (fmt-line (str "#" (:id ok)) (:symbol ok) (:remaining ok) (:price ok))
              "  OK cancelada")
         (str (pad-left 4 (str "#" id)) "  REJEITADO: " (:message error)))))

(defn format-trade
  [{:keys [symbol quantity price buy-order-id sell-order-id]}]
  (str "    TRADE " symbol " " (pad-left 5 quantity) " @ " (fmt-price price)
       " (compra #" buy-order-id " x venda #" sell-order-id ")"))

;; --- book ------------------------------------------------------------------

(def ^:private cell-width 22)

(defn- book-cell [{:keys [id remaining price]}]
  (str (pad-left 5 (str "#" id)) " " (pad-left 6 remaining) " @ " (pad-left 7 (fmt-price price))))

(defn- book-row [bid ask]
  (str/trimr (str (pad-right cell-width (some-> bid book-cell))
                  " | "
                  (some-> ask book-cell))))

(defn format-book
  "Both sides of `symbol`'s book side by side (best prices on top) plus the spread."
  [symbol bids asks spread]
  (let [rows (max (count bids) (count asks))]
    (str/join "\n"
              (concat [(str "=== LIVRO " symbol " ===")
                       (str (pad-right cell-width "        COMPRA") " |        VENDA")]
                      (if (zero? rows)
                        ["  (vazio)"]
                        (map book-row
                             (concat bids (repeat (- rows (count bids)) nil))
                             (concat asks (repeat (- rows (count asks)) nil))))
                      [(str "spread: " (if spread (fmt-price spread) "-"))]))))

;; --- users and wallets (US-05) ---------------------------------------------

(defn- fmt-stock [stock]
  (apply str (for [[symbol quantity] (sort stock)] (str "  " symbol " " quantity))))

(defn format-user-result
  "One line describing a user registration attempt."
  [{:keys [name balance stock]} {:keys [ok error]}]
  (str "[USUÁRIO] "
       (if ok
         (str (pad-left 4 (str "#" (:id ok))) " " (:name ok)
              "  saldo " (fmt-price balance) (fmt-stock stock))
         (str (pad-left 4 "") "  REJEITADO: " (:message error) " (" (pr-str name) ")"))))

(defn- reserved-suffix [x fmt]
  (when (and x (pos? x)) (str " (reservado " (fmt x) ")")))

(defn format-wallet
  "Wallet statement: available/reserved cash and stock positions."
  [{:keys [id name]} {:keys [balance reserved-cash stock reserved-stock]}]
  (let [symbols (sort (distinct (concat (keys stock) (keys reserved-stock))))]
    (str/join "\n"
              (concat [(str "=== CARTEIRA #" id " " name " ===")
                       (str "  saldo: " (fmt-price balance) (reserved-suffix reserved-cash fmt-price))]
                      (if (empty? symbols)
                        ["  (sem ativos)"]
                        (for [s symbols]
                          (str "  " s ": " (pad-left 6 (get stock s 0))
                               (reserved-suffix (get reserved-stock s) str))))))))
