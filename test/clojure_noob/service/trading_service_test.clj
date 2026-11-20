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
