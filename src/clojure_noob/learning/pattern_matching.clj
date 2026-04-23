(ns clojure-noob.learning.pattern-matching
    (:require [clojure.core.match :refer [match]]))

;; ordens de mercado
(defn describe-order [order]
    (match order
        [:market :buy  qty]        (str "Buy "  qty " shares at market price")
        [:market :sell qty]        (str "Sell " qty " shares at market price")
        [:limit  :buy  qty price]  (str "Buy "  qty " shares at limit R$" price)
        [:limit  :sell qty price]  (str "Sell " qty " shares at limit R$" price)
        :else "Unknown order format"))
