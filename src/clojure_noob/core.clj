(ns clojure-noob.core
  (:gen-class)
  (:require [clojure-noob.presentation.terminal :as term]
            [clojure-noob.service.trading-service :as svc]))

(def demo-orders
  [[:buy  {:symbol "PETR4" :quantity 100 :price 30.50}]
   [:buy  {:symbol "PETR4" :quantity 50  :price 31.00}]
   [:buy  {:symbol "VALE3" :quantity 10  :price 65.00}]
   [:buy  {:symbol "PETR4" :quantity 200 :price 30.50}]
   [:sell {:symbol "PETR4" :quantity 80  :price 31.20}]
   [:sell {:symbol "PETR4" :quantity 100 :price 31.50}]
   [:sell {:symbol "PETR4" :quantity 30  :price 31.20}]
   [:sell {:symbol "VALE3" :quantity 20  :price 66.00}]
   [:buy  {:symbol "VALE3" :quantity 0   :price 65.00}]
   [:sell {:symbol "PETR4" :quantity 10  :price -1}]
   [:buy  {:symbol "XPTO3" :quantity 10  :price 10.00}]])

(def ^:private place!
  {:buy  svc/place-buy-order!
   :sell svc/place-sell-order!})

(defn -main
  "Demo: sends some buy and sell orders and prints the resulting books."
  [& _args]
  (doseq [[side params] demo-orders]
    (println (term/format-order-result side params ((place! side) params))))
  (doseq [symbol ["PETR4" "VALE3"]]
    (println)
    (println (term/format-book symbol (svc/bids symbol) (svc/asks symbol) (svc/spread symbol)))))
