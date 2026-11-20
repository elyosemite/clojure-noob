(ns clojure-noob.domain.order-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure-noob.domain.order :as order]))

(def valid-params
  {:id 1 :user-id 7 :symbol "PETR4" :quantity 100 :price 30.50 :timestamp 1000})

(defn- error-code [result]
  (get-in result [:error :code]))

(deftest create-buy-order-valid-test
  (testing "valid params create an open buy order"
    (let [{:keys [ok error]} (order/create-buy-order valid-params)]
      (is (nil? error))
      (is (= {:id 1
              :user-id 7
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

(deftest create-sell-order-valid-test
  (testing "valid params create an open sell order"
    (is (= {:id 1
            :user-id 7
            :side :sell
            :symbol "PETR4"
            :quantity 100
            :remaining 100
            :price 30.50M
            :status :open
            :timestamp 1000}
           (:ok (order/create-sell-order valid-params))))))

(deftest create-sell-order-validation-test
  (testing "sell orders follow the same validation rules as buy orders"
    (doseq [[field value code] [[:symbol "  "     :invalid-symbol]
                                [:symbol "XPTO3"  :unknown-symbol]
                                [:quantity 0      :invalid-quantity]
                                [:quantity 1.5    :invalid-quantity]
                                [:price -1        :invalid-price]
                                [:price "30"      :invalid-price]]]
      (is (= code (error-code (order/create-sell-order (assoc valid-params field value))))
          (str field " " (pr-str value))))))

;; --- fill (US-03) ----------------------------------------------------------

(def open-order (:ok (order/create-buy-order valid-params)))

(deftest fill-test
  (testing "partial fill decreases remaining and marks :partially-filled"
    (let [o (order/fill open-order 30)]
      (is (= 70 (:remaining o)))
      (is (= 100 (:quantity o)))
      (is (= :partially-filled (:status o)))))

  (testing "filling the whole remaining marks :filled"
    (let [o (order/fill (order/fill open-order 30) 70)]
      (is (= 0 (:remaining o)))
      (is (= :filled (:status o)))))

  (testing "rejects zero, negative, fractional or excessive quantity"
    (doseq [q [0 -1 1.5 101]]
      (is (thrown? clojure.lang.ExceptionInfo (order/fill open-order q))
          (str "quantity " q)))))

;; --- cancel (US-04) --------------------------------------------------------

(deftest cancel-test
  (testing "open order becomes :cancelled keeping remaining"
    (is (= [:cancelled 100] ((juxt :status :remaining) (:ok (order/cancel open-order))))))

  (testing "partially filled order becomes :cancelled keeping what was left"
    (is (= [:cancelled 70]
           ((juxt :status :remaining) (:ok (order/cancel (order/fill open-order 30))))))))

(deftest cancel-rejection-test
  (testing "filled order cannot be cancelled"
    (let [r (order/cancel (order/fill open-order 100))]
      (is (= :not-cancellable (error-code r)))
      (is (= "ordem já executada" (get-in r [:error :message])))))

  (testing "cancelled order cannot be cancelled again"
    (let [r (order/cancel (:ok (order/cancel open-order)))]
      (is (= :not-cancellable (error-code r)))
      (is (= "ordem já cancelada" (get-in r [:error :message]))))))

;; --- owner (US-05) ---------------------------------------------------------

(deftest user-required-test
  (testing "order without user-id is rejected before any other rule"
    (let [r (order/create-sell-order (dissoc valid-params :user-id :symbol))]
      (is (= :invalid-user (error-code r)))
      (is (= "usuário não informado" (get-in r [:error :message]))))))
