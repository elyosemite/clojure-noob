(ns clojure-noob.core
  (:gen-class)
  (:require [clojure-noob.presentation.terminal :as term]
            [clojure-noob.service.trading-service :as svc]))

(def demo-buy-orders
  [{:symbol "PETR4" :quantity 100 :price 30.50}
   {:symbol "PETR4" :quantity 50  :price 31.00}
   {:symbol "VALE3" :quantity 10  :price 65.00}
   {:symbol "PETR4" :quantity 200 :price 30.50}
   {:symbol "VALE3" :quantity 0   :price 65.00}
   {:symbol "XPTO3" :quantity 10  :price 10.00}])

(defn -main
  "Demo: sends some buy orders and prints the resulting book."
  [& _args]
  (doseq [params demo-buy-orders]
    (println (term/format-buy-result params (svc/place-buy-order! params))))
  (doseq [symbol ["PETR4" "VALE3"]]
    (println)
    (println (term/format-bids symbol (svc/bids symbol)))))
