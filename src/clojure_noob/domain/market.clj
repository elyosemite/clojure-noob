(ns clojure-noob.domain.market)

(def fixed-prices
  {"PETR4" 30.0
   "VALE3" 65.0})

(defn get-price [symbol]
  (get fixed-prices symbol))

(defn random-price [base]
  (+ base (- (rand 10) 5)))
