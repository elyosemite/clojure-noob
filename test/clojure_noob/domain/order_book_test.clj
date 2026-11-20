(ns clojure-noob.domain.order-book-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure-noob.domain.order :as order]
            [clojure-noob.domain.order-book :as book]))

(defn- buy [id symbol quantity price timestamp]
  (:ok (order/create-buy-order {:id id :symbol symbol :quantity quantity
                                :price price :timestamp timestamp})))

(defn- add-all [orders]
  (reduce book/add-order book/empty-book orders))

(deftest empty-book-test
  (testing "empty book has no bids for any symbol"
    (is (= [] (book/bids book/empty-book "PETR4")))))

(deftest add-single-order-test
  (testing "added buy order appears on the bids side of its symbol"
    (let [o (buy 1 "PETR4" 100 30.50 1000)]
      (is (= [o] (book/bids (book/add-order book/empty-book o) "PETR4"))))))

(deftest bids-price-priority-test
  (testing "highest price comes first"
    (let [b (add-all [(buy 1 "PETR4" 100 30.50 1000)
                      (buy 2 "PETR4" 50 31.00 1001)
                      (buy 3 "PETR4" 10 29.90 1002)])]
      (is (= [2 1 3] (map :id (book/bids b "PETR4")))))))

(deftest bids-time-priority-test
  (testing "same price: oldest order comes first"
    (let [b (add-all [(buy 1 "PETR4" 100 30.50 2000)
                      (buy 2 "PETR4" 50 30.50 1000)
                      (buy 3 "PETR4" 10 30.50 3000)])]
      (is (= [2 1 3] (map :id (book/bids b "PETR4"))))))

  (testing "same price and timestamp: lower id (arrived first) comes first"
    (let [b (add-all [(buy 2 "PETR4" 50 30.50 1000)
                      (buy 1 "PETR4" 100 30.50 1000)])]
      (is (= [1 2] (map :id (book/bids b "PETR4")))))))

(deftest multiple-symbols-test
  (testing "each symbol has its own book"
    (let [b (add-all [(buy 1 "PETR4" 100 30.50 1000)
                      (buy 2 "VALE3" 10 65.00 1001)
                      (buy 3 "PETR4" 50 31.00 1002)])]
      (is (= [3 1] (map :id (book/bids b "PETR4"))))
      (is (= [2] (map :id (book/bids b "VALE3")))))))

(deftest immutability-test
  (testing "add-order returns a new book and leaves the original untouched"
    (let [original (add-all [(buy 1 "PETR4" 100 30.50 1000)])
          updated  (book/add-order original (buy 2 "PETR4" 50 31.00 1001))]
      (is (= [1] (map :id (book/bids original "PETR4"))))
      (is (= [2 1] (map :id (book/bids updated "PETR4")))))))
