(ns clojure-noob.core
  (:gen-class)
  (:require [clojure-noob.presentation.terminal :as term]
            [clojure-noob.service.trading-service :as svc]))

(def demo-orders
  [[:buy  {:user-id 1 :symbol "PETR4" :quantity 100 :price 30.50}]
   [:buy  {:user-id 1 :symbol "PETR4" :quantity 50  :price 31.00}]
   [:buy  {:user-id 1 :symbol "VALE3" :quantity 10  :price 65.00}]
   [:buy  {:user-id 1 :symbol "PETR4" :quantity 200 :price 30.50}]
   [:sell {:user-id 1 :symbol "PETR4" :quantity 80  :price 31.20}]
   [:sell {:user-id 1 :symbol "PETR4" :quantity 100 :price 31.50}]
   [:sell {:user-id 1 :symbol "PETR4" :quantity 30  :price 31.20}]
   [:sell {:user-id 1 :symbol "VALE3" :quantity 20  :price 66.00}]
   ;; crossing orders (US-03)
   [:buy  {:user-id 1 :symbol "PETR4" :quantity 100 :price 31.30}]   ; fills #5 and part of #7
   [:sell {:user-id 1 :symbol "PETR4" :quantity 120 :price 30.80}]   ; fills #2, rest goes to the book
   [:buy  {:user-id 1 :symbol "VALE3" :quantity 30  :price 66.00}]   ; fills #8, rest goes to the book
   ;; rejected
   [:buy  {:user-id 1 :symbol "VALE3" :quantity 0   :price 65.00}]
   [:sell {:user-id 1 :symbol "PETR4" :quantity 10  :price -1}]
   [:buy  {:user-id 1 :symbol "XPTO3" :quantity 10  :price 10.00}]])

(def demo-cancels
  [4    ; open
   7    ; partially filled
   5    ; already filled  -> error
   99   ; does not exist   -> error
   4])  ; already cancelled -> error

(def ^:private place!
  {:buy  svc/place-buy-order!
   :sell svc/place-sell-order!})

(defn- print-books [title]
  (println)
  (println (str "##### " title " #####"))
  (doseq [symbol ["PETR4" "VALE3"]]
    (println)
    (println (term/format-book symbol (svc/bids symbol) (svc/asks symbol) (svc/spread symbol)))))

(defn -main
  "Demo: sends buy and sell orders, cancels some of them and prints the books."
  [& _args]
  (doseq [[side params] demo-orders]
    (let [result ((place! side) params)]
      (println (term/format-order-result side params result))
      (doseq [t (:trades result)]
        (println (term/format-trade t)))))
  (print-books "LIVROS ANTES DOS CANCELAMENTOS")
  (println)
  (doseq [id demo-cancels]
    (println (term/format-cancel-result id (svc/cancel-order! id))))
  (print-books "LIVROS DEPOIS DOS CANCELAMENTOS"))
