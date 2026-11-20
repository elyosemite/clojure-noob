(ns clojure-noob.presentation.terminal
  "Pure formatting functions: data in, string out. No printing here."
  (:require [clojure.string :as str]))

(defn- pad-left [width s]
  (let [s (str s)]
    (str (apply str (repeat (- width (count s)) \space)) s)))

(defn- fmt-price
  "Two decimals with a dot, regardless of the system locale."
  [p]
  (if (number? p)
    (.toPlainString (.setScale (bigdec p) 2 java.math.RoundingMode/HALF_UP))
    (pr-str p)))

(defn- fmt-line [id symbol quantity price]
  (str (pad-left 4 id) " " symbol " " (pad-left 5 quantity) " @ " (fmt-price price)))

(defn format-buy-result
  "One line describing a buy attempt: `params` sent and `result` received."
  [{:keys [symbol quantity price]} {:keys [ok error]}]
  (if ok
    (str "[COMPRA] " (fmt-line (str "#" (:id ok)) (:symbol ok) (:quantity ok) (:price ok))
         "  OK registrada")
    (str "[COMPRA] " (fmt-line "" symbol quantity price)
         "  REJEITADA: " (:message error))))

(defn format-bids
  "Buy side of `symbol`'s book, best price first, showing remaining quantity."
  [symbol bids]
  (str/join "\n"
            (cons (str "=== LIVRO " symbol " - COMPRA ===")
                  (if (empty? bids)
                    ["  (vazio)"]
                    (map (fn [{:keys [id remaining price]}]
                           (fmt-line (str "#" id) "" remaining price))
                         bids)))))
