(ns clojure-noob.domain.order-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure-noob.domain.order :as order]))

(def valid-params
  {:id 1 :symbol "PETR4" :quantity 100 :price 30.50 :timestamp 1000})

(defn- error-code [result]
  (get-in result [:error :code]))

(deftest create-buy-order-valid-test
  (testing "valid params create an open buy order"
    (let [{:keys [ok error]} (order/create-buy-order valid-params)]
      (is (nil? error))
      (is (= {:id 1
              :side :buy
              :symbol "PETR4"
              :quantity 100
              :remaining 100
              :price 30.50M
              :status :open
              :timestamp 1000}
             ok))))

  (testing "price is stored as bigdec"
    (is (decimal? (get-in (order/create-buy-order valid-params) [:ok :price])))
    (is (= 30M (get-in (order/create-buy-order (assoc valid-params :price 30)) [:ok :price])))))

(deftest create-buy-order-symbol-test
  (testing "rejects blank symbol"
    (is (= :invalid-symbol (error-code (order/create-buy-order (assoc valid-params :symbol "  ")))))
    (is (= :invalid-symbol (error-code (order/create-buy-order (dissoc valid-params :symbol))))))

  (testing "rejects unknown symbol"
    (is (= :unknown-symbol (error-code (order/create-buy-order (assoc valid-params :symbol "XPTO3")))))))

(deftest create-buy-order-quantity-test
  (testing "rejects zero, negative, fractional and missing quantity"
    (doseq [q [0 -10 1.5 "100" nil]]
      (is (= :invalid-quantity
             (error-code (order/create-buy-order (assoc valid-params :quantity q))))
          (str "quantity " (pr-str q))))))

(deftest create-buy-order-price-test
  (testing "rejects zero, negative and non-numeric price"
    (doseq [p [0 -1 0.0 "30" nil]]
      (is (= :invalid-price
             (error-code (order/create-buy-order (assoc valid-params :price p))))
          (str "price " (pr-str p))))))

(deftest error-has-message-test
  (testing "errors carry a human readable message"
    (is (string? (get-in (order/create-buy-order (assoc valid-params :quantity 0))
                         [:error :message])))))
