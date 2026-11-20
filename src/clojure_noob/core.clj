(ns clojure-noob.core
  (:gen-class)
  (:require [clojure-noob.presentation.terminal :as term]
            [clojure-noob.service.trading-service :as svc]
            [clojure-noob.service.user-service :as users]))

(def demo-users
  [{:name "Ana"   :balance 20000 :stock {}}                          ; #1 buyer
   {:name "Bruno" :balance 0     :stock {"PETR4" 500 "VALE3" 100}}   ; #2 seller
   {:name "Carla" :balance 1000  :stock {}}])                        ; #3 little cash

(def demo-orders
  [[:buy  {:user-id 1 :symbol "PETR4" :quantity 100 :price 30.50}]
   [:buy  {:user-id 1 :symbol "PETR4" :quantity 50  :price 31.00}]
   [:buy  {:user-id 1 :symbol "VALE3" :quantity 10  :price 65.00}]
   [:buy  {:user-id 1 :symbol "PETR4" :quantity 200 :price 30.50}]
   [:sell {:user-id 2 :symbol "PETR4" :quantity 80  :price 31.20}]
   [:sell {:user-id 2 :symbol "PETR4" :quantity 100 :price 31.50}]
   [:sell {:user-id 2 :symbol "PETR4" :quantity 30  :price 31.20}]
   [:sell {:user-id 2 :symbol "VALE3" :quantity 20  :price 66.00}]
   ;; crossing orders (US-03)
   [:buy  {:user-id 1 :symbol "PETR4" :quantity 100 :price 31.30}]   ; fills #5 and part of #7
   [:sell {:user-id 2 :symbol "PETR4" :quantity 120 :price 30.80}]   ; fills #2, rest goes to the book
   [:buy  {:user-id 1 :symbol "VALE3" :quantity 30  :price 66.00}]   ; fills #8, rest goes to the book
   ;; rejected
   [:buy  {:user-id 1 :symbol "VALE3" :quantity 0   :price 65.00}]
   [:sell {:user-id 2 :symbol "PETR4" :quantity 10  :price -1}]
   [:buy  {:user-id 1 :symbol "XPTO3" :quantity 10  :price 10.00}]
   [:buy  {:user-id 3 :symbol "PETR4" :quantity 100 :price 31.00}]   ; insufficient balance
   [:sell {:user-id 3 :symbol "VALE3" :quantity 10  :price 66.00}]   ; insufficient stock
   [:buy  {:user-id 99 :symbol "PETR4" :quantity 10 :price 30.00}]]) ; unknown user

(def demo-cancels
  [4    ; open
   7    ; partially filled
   5    ; already filled  -> error
   99   ; does not exist   -> error
   4])  ; already cancelled -> error

(def ^:private place!
  {:buy  svc/place-buy-order!
   :sell svc/place-sell-order!})

(defn- print-title [title]
  (println)
  (println (str "##### " title " #####")))

(defn- print-books [title]
  (print-title title)
  (doseq [symbol ["PETR4" "VALE3"]]
    (println)
    (println (term/format-book symbol (svc/bids symbol) (svc/asks symbol) (svc/spread symbol)))))

(defn- print-wallets [user-ids]
  (print-title "CARTEIRAS")
  (doseq [id user-ids]
    (println)
    (println (term/format-wallet (users/find-user id) (users/find-wallet id)))))

(defn -main
  "Demo: registers investors, sends orders, cancels some of them and prints
  the books and the wallets."
  [& _args]
  (let [user-ids (vec (for [{:keys [name balance stock] :as u} demo-users
                            :let [r (users/register-user! name balance stock)]]
                        (do (println (term/format-user-result u r))
                            (get-in r [:ok :id]))))]
    (println)
    (doseq [[side params] demo-orders]
      (let [result ((place! side) params)]
        (println (term/format-order-result side params result))
        (doseq [t (:trades result)]
          (println (term/format-trade t)))))
    (print-books "LIVROS ANTES DOS CANCELAMENTOS")
    (println)
    (doseq [id demo-cancels]
      (println (term/format-cancel-result id (svc/cancel-order! id))))
    (print-books "LIVROS DEPOIS DOS CANCELAMENTOS")
    (print-wallets (remove nil? user-ids))))
