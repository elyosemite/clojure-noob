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

(defn format-order-result
  "One line describing an order attempt: `side`, `params` sent and `result` received."
  [side {:keys [symbol quantity price]} {:keys [ok error]}]
  (str (side-label side) " "
       (if ok
         (str (fmt-line (str "#" (:id ok)) (:symbol ok) (:quantity ok) (:price ok))
              "  OK registrada")
         (str (fmt-line "" symbol quantity price)
              "  REJEITADA: " (:message error)))))

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
