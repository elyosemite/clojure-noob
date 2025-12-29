(ns clojure-noob.domain.market)

(def fixed-prices
  {"PETR4" 30.0
   "VALE3" 65.0})

(defn get-price [symbol]
  (get fixed-prices symbol))

;; Random version
(defn random-price [base]
  (+ base (- (rand 10) 5)))