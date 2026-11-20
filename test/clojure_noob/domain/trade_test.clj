(ns clojure-noob.domain.trade-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure-noob.domain.order :as order]
            [clojure-noob.domain.trade :as trade]))

(def resting-sell
  (:ok (order/create-sell-order {:id 5 :symbol "PETR4" :quantity 80 :price 31.20 :timestamp 1})))

(def incoming-buy
  (:ok (order/create-buy-order {:id 9 :symbol "PETR4" :quantity 100 :price 31.30 :timestamp 2})))

(deftest create-trade-test
  (testing "buy aggressor against resting sell, priced at the resting order"
    (is (= {:id 1 :symbol "PETR4" :quantity 80 :price 31.20M
            :buy-order-id 9 :sell-order-id 5 :timestamp 10}
           (trade/create-trade {:id 1 :timestamp 10 :quantity 80
                                :aggressor incoming-buy :resting resting-sell}))))

  (testing "sell aggressor against resting buy keeps buy/sell ids in place"
    (let [resting-buy  (assoc incoming-buy :id 2 :price 31.00M)
          incoming-sell (assoc resting-sell :id 7 :price 30.80M)
          t (trade/create-trade {:id 2 :timestamp 11 :quantity 50
                                 :aggressor incoming-sell :resting resting-buy})]
      (is (= [2 7] [(:buy-order-id t) (:sell-order-id t)]))
      (is (= 31.00M (:price t))))))
