(ns clojure-noob.domain.wallet-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure-noob.domain.wallet :as w]))

(def wallet (w/create-wallet 10000 {"PETR4" 100}))

(deftest create-wallet-test
  (testing "cash as bigdec with 2 decimals, nothing reserved"
    (is (= {:balance 10000.00M :reserved-cash 0.00M :stock {"PETR4" 100} :reserved-stock {}}
           wallet))
    (is (= 2 (.scale (:balance wallet)))))

  (testing "zero positions are dropped; stock defaults to empty"
    (is (= {} (:stock (w/create-wallet 0 {"VALE3" 0}))))
    (is (= {} (:stock (w/create-wallet 50))))))

(deftest reserve-cash-test
  (testing "moves cash from balance to reserved"
    (let [r (:ok (w/reserve-cash wallet 3050.0M))]
      (is (= [6950M 3050M] [(:balance r) (:reserved-cash r)]))))

  (testing "whole balance can be reserved"
    (is (= 0M (:balance (:ok (w/reserve-cash wallet 10000M))))))

  (testing "insufficient balance"
    (let [r (w/reserve-cash wallet 10000.01M)]
      (is (= :insufficient-balance (get-in r [:error :code])))
      (is (= "saldo insuficiente: necessário 10000.01, disponível 10000.00"
             (get-in r [:error :message]))))))

(deftest release-cash-test
  (let [r (w/release-cash (:ok (w/reserve-cash wallet 3000M)) 1000M)]
    (is (= [8000M 2000M] [(:balance r) (:reserved-cash r)]))))

(deftest reserve-stock-test
  (testing "moves stock from available to reserved"
    (let [r (:ok (w/reserve-stock wallet "PETR4" 30))]
      (is (= [{"PETR4" 70} {"PETR4" 30}] [(:stock r) (:reserved-stock r)]))))

  (testing "reserving everything drops the available key"
    (is (= {} (:stock (:ok (w/reserve-stock wallet "PETR4" 100))))))

  (testing "insufficient stock, including a symbol not held"
    (is (= :insufficient-stock (get-in (w/reserve-stock wallet "PETR4" 101) [:error :code])))
    (is (= "ativos insuficientes: VALE3 necessário 10, disponível 0"
           (get-in (w/reserve-stock wallet "VALE3" 10) [:error :message])))))

(deftest release-stock-test
  (let [r (w/release-stock (:ok (w/reserve-stock wallet "PETR4" 30)) "PETR4" 30)]
    (is (= [{"PETR4" 100} {}] [(:stock r) (:reserved-stock r)]))))

(deftest settle-buy-test
  (testing "pays trade price, receives stock, returns the limit difference"
    ;; reserved 100 @ 31.30 = 3130; executed @ 31.20 = 3120 -> 10 back
    (let [reserved (:ok (w/reserve-cash wallet 3130M))
          r        (w/settle-buy reserved "VALE3" 100 31.20M 31.30M)]
      (is (= 6880M (:balance r)))          ; 10000 - 3130 + 10
      (is (= 0M (:reserved-cash r)))
      (is (= {"PETR4" 100 "VALE3" 100} (:stock r)))))

  (testing "partial settlement keeps the rest reserved"
    (let [reserved (:ok (w/reserve-cash wallet 3100M))      ; 100 @ 31.00
          r        (w/settle-buy reserved "PETR4" 40 31.00M 31.00M)]
      (is (= 1860M (:reserved-cash r)))
      (is (= 6900M (:balance r)))
      (is (= {"PETR4" 140} (:stock r))))))

(deftest settle-sell-test
  (testing "reserved stock leaves and cash comes in at trade price"
    (let [reserved (:ok (w/reserve-stock wallet "PETR4" 80))
          r        (w/settle-sell reserved "PETR4" 50 31.20M)]
      (is (= {"PETR4" 30} (:reserved-stock r)))
      (is (= {"PETR4" 20} (:stock r)))
      (is (= 11560M (:balance r))))))
