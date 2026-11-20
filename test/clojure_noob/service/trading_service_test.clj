(ns clojure-noob.service.trading-service-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure-noob.repository.db :as db]
            [clojure-noob.service.trading-service :as svc]))

(use-fixtures :each (fn [t] (db/reset-db!) (t) (db/reset-db!)))

(def valid {:symbol "PETR4" :quantity 100 :price 30.50})

;; --- functional core -------------------------------------------------------

(deftest place-order-success-test
  (testing "valid order gets next id, timestamp and lands in book and orders"
    (let [{:keys [state result]} (svc/place-order db/initial-state :buy valid 1000)
          o (:ok result)]
      (is (= 1 (:id o)))
      (is (= 1000 (:timestamp o)))
      (is (= o (get-in state [:orders 1])))
      (is (= 2 (:next-id state)))
      (is (= [o] (get-in state [:book "PETR4" :bids]))))))

(deftest place-order-error-test
  (testing "invalid order returns error and leaves state untouched"
    (let [{:keys [state result]} (svc/place-order db/initial-state :buy
                                                  (assoc valid :quantity 0) 1000)]
      (is (= :invalid-quantity (get-in result [:error :code])))
      (is (= db/initial-state state)))))

;; --- imperative shell ------------------------------------------------------

(deftest place-buy-order!-test
  (testing "registers orders with sequential ids and exposes them in the book"
    (let [r1 (svc/place-buy-order! valid)
          r2 (svc/place-buy-order! (assoc valid :price 31.00 :quantity 50))]
      (is (= [1 2] [(get-in r1 [:ok :id]) (get-in r2 [:ok :id])]))
      (is (= [2 1] (map :id (svc/bids "PETR4"))))
      (is (= (:ok r1) (svc/find-order 1))))))

(deftest place-buy-order!-rejection-test
  (testing "rejected order does not change state nor consume an id"
    (let [before @db/db
          r      (svc/place-buy-order! (assoc valid :symbol "XPTO3"))]
      (is (= :unknown-symbol (get-in r [:error :code])))
      (is (= before @db/db))
      (is (= 1 (get-in (svc/place-buy-order! valid) [:ok :id]))))))

(deftest place-buy-order!-uses-clock-test
  (testing "timestamp comes from the clock"
    (with-redefs [svc/now (constantly 42)]
      (is (= 42 (get-in (svc/place-buy-order! valid) [:ok :timestamp]))))))

;; --- sell side (US-02) -----------------------------------------------------

(deftest place-order-sell-test
  (testing "pure core puts sell orders on the asks side"
    (let [{:keys [state result]} (svc/place-order db/initial-state :sell valid 1000)]
      (is (= :sell (get-in result [:ok :side])))
      (is (= [(:ok result)] (get-in state [:book "PETR4" :asks]))))))

(deftest place-sell-order!-test
  (testing "registers sell orders sharing the id sequence with buys"
    (let [b (svc/place-buy-order! valid)
          s (svc/place-sell-order! (assoc valid :price 31.20 :quantity 80))]
      (is (= [1 2] [(get-in b [:ok :id]) (get-in s [:ok :id])]))
      (is (= [1] (map :id (svc/bids "PETR4"))))
      (is (= [2] (map :id (svc/asks "PETR4"))))
      (is (= (:ok s) (svc/find-order 2)))))

  (testing "spread reflects best bid and best ask"
    (is (= 0.70M (svc/spread "PETR4")))))

(deftest place-sell-order!-rejection-test
  (testing "rejected sell order leaves state untouched"
    (let [before @db/db
          r      (svc/place-sell-order! (assoc valid :price 0))]
      (is (= :invalid-price (get-in r [:error :code])))
      (is (= before @db/db))
      (is (nil? (svc/spread "PETR4"))))))

;; --- matching (US-03) ------------------------------------------------------

(deftest place-order-matching-test
  (testing "crossing order generates trades and updates resting orders"
    (let [s1 (svc/place-sell-order! {:symbol "PETR4" :quantity 80 :price 31.20})
          s2 (svc/place-sell-order! {:symbol "PETR4" :quantity 30 :price 31.20})
          r  (svc/place-buy-order!  {:symbol "PETR4" :quantity 100 :price 31.30})]
      (is (= [1 2] [(get-in s1 [:ok :id]) (get-in s2 [:ok :id])]))
      (testing "incoming order is returned after matching"
        (is (= [3 :filled 0] ((juxt :id :status :remaining) (:ok r)))))
      (testing "trades are returned and stored with sequential ids"
        (is (= [[1 80 31.20M 3 1] [2 20 31.20M 3 2]]
               (map (juxt :id :quantity :price :buy-order-id :sell-order-id) (:trades r))))
        (is (= (:trades r) (svc/trades))))
      (testing "resting orders reflect their fills"
        (is (= :filled (:status (svc/find-order 1))))
        (is (= [:partially-filled 10] ((juxt :status :remaining) (svc/find-order 2))))
        (is (= :filled (:status (svc/find-order 3)))))
      (testing "book keeps only what was not executed"
        (is (= [[2 10]] (map (juxt :id :remaining) (svc/asks "PETR4"))))
        (is (= [] (svc/bids "PETR4")))))))

(deftest place-order-no-trade-test
  (testing "non-crossing order returns empty trades"
    (is (= [] (:trades (svc/place-buy-order! valid))))
    (is (= [] (svc/trades)))))

(deftest rejection-keeps-trade-sequence-test
  (testing "rejected order creates no trade and does not touch the trade sequence"
    (svc/place-sell-order! {:symbol "PETR4" :quantity 10 :price 30.00})
    (let [before @db/db]
      (is (contains? (svc/place-buy-order! (assoc valid :quantity -1)) :error))
      (is (= before @db/db)))))
