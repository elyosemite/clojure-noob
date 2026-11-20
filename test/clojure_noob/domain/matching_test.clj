(ns clojure-noob.domain.matching-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure-noob.domain.matching :as m]
            [clojure-noob.domain.order :as order]
            [clojure-noob.domain.order-book :as book]))

(defn- buy [id quantity price ts & [symbol]]
  (:ok (order/create-buy-order {:id id :symbol (or symbol "PETR4") :quantity quantity
                                :price price :timestamp ts})))

(defn- sell [id quantity price ts & [symbol]]
  (:ok (order/create-sell-order {:id id :symbol (or symbol "PETR4") :quantity quantity
                                 :price price :timestamp ts})))

(defn- book-of [& orders]
  (reduce book/add-order book/empty-book orders))

(defn- summary
  "[[resting-id quantity price] ...] for compact assertions."
  [fills]
  (mapv (fn [{:keys [resting quantity]}] [(:id resting) quantity (:price resting)]) fills))

(deftest no-cross-test
  (testing "buy below best ask just rests in the book"
    (let [{:keys [book order fills]} (m/match (book-of (sell 1 100 31.20 1)) (buy 2 50 31.00 2))]
      (is (empty? fills))
      (is (= :open (:status order)))
      (is (= [2] (map :id (book/bids book "PETR4"))))
      (is (= [1] (map :id (book/asks book "PETR4"))))))

  (testing "empty opposite side: order rests"
    (let [{:keys [book fills]} (m/match book/empty-book (sell 1 10 30 1))]
      (is (empty? fills))
      (is (= [1] (map :id (book/asks book "PETR4")))))))

(deftest full-fill-test
  (testing "same quantity: both orders filled and removed from the book"
    (let [{:keys [book order fills]} (m/match (book-of (sell 1 100 31.20 1)) (buy 2 100 31.20 2))]
      (is (= [[1 100 31.20M]] (summary fills)))
      (is (= :filled (:status order)))
      (is (= :filled (get-in fills [0 :resting :status])))
      (is (= [] (book/bids book "PETR4")))
      (is (= [] (book/asks book "PETR4"))))))

(deftest partial-incoming-test
  (testing "incoming bigger than resting: remaining rests on its side"
    (let [{:keys [book order fills]} (m/match (book-of (sell 1 30 31.00 1)) (buy 2 100 31.50 2))]
      (is (= [[1 30 31.00M]] (summary fills)))
      (is (= [:partially-filled 70] [(:status order) (:remaining order)]))
      (is (= [] (book/asks book "PETR4")))
      (is (= [[2 70]] (map (juxt :id :remaining) (book/bids book "PETR4")))))))

(deftest partial-resting-test
  (testing "incoming smaller than resting: resting stays with its remaining"
    (let [{:keys [book order fills]} (m/match (book-of (buy 1 100 31.00 1)) (sell 2 40 30.00 2))]
      (is (= [[1 40 31.00M]] (summary fills)))
      (is (= :filled (:status order)))
      (is (= [[1 60 :partially-filled]]
             (map (juxt :id :remaining :status) (book/bids book "PETR4"))))
      (is (= [] (book/asks book "PETR4"))))))

(deftest multiple-levels-test
  (testing "walks price levels until incoming limit stops it"
    (let [b (book-of (sell 1 80 31.20 1) (sell 2 30 31.20 2) (sell 3 100 31.50 3) (sell 4 10 31.30 4))
          {:keys [book order fills]} (m/match b (buy 5 150 31.30 5))]
      (is (= [[1 80 31.20M] [2 30 31.20M] [4 10 31.30M]] (summary fills)))
      (is (= [:partially-filled 30] [(:status order) (:remaining order)]))
      (is (= [3] (map :id (book/asks book "PETR4"))))
      (is (= [[5 30]] (map (juxt :id :remaining) (book/bids book "PETR4")))))))

(deftest time-priority-test
  (testing "same price: oldest resting order is executed first"
    (let [b (book-of (buy 1 50 31.00 2000) (buy 2 50 31.00 1000))
          {:keys [fills]} (m/match b (sell 3 60 30.00 3000))]
      (is (= [[2 50 31.00M] [1 10 31.00M]] (summary fills))))))

(deftest resting-price-test
  (testing "trade price is the resting price, not the aggressor limit"
    (let [{:keys [fills]} (m/match (book-of (buy 1 10 31.00 1)) (sell 2 10 29.00 2))]
      (is (= 31.00M (get-in fills [0 :resting :price]))))))

(deftest other-symbol-test
  (testing "orders of other symbols are never matched"
    (let [{:keys [book fills]} (m/match (book-of (sell 1 10 60.00 1 "VALE3")) (buy 2 10 65.00 2))]
      (is (empty? fills))
      (is (= [1] (map :id (book/asks book "VALE3"))))
      (is (= [2] (map :id (book/bids book "PETR4")))))))
