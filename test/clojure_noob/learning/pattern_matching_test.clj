(ns clojure-noob.learning.pattern-matching-test
    (:require [clojure.test :refer [deftest is testing run-tests]]
              [clojure-noob.learning.pattern-matching :refer [describe-order]]))

(deftest describe-order-test
    (testing "market orders"
      (is (= "Buy 100 shares at market price"
             (describe-order [:market :buy 100])))
      (is (= "Sell 50 shares at market price"
             (describe-order [:market :sell 50])))))
