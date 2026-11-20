(ns clojure-noob.presentation.terminal-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure-noob.presentation.terminal :as term]))

(def accepted
  {:ok {:id 1 :side :buy :symbol "PETR4" :quantity 100 :remaining 100
        :price 30.5M :status :open :timestamp 1000}})

(deftest format-buy-result-test
  (testing "accepted order shows id, symbol, quantity and price with 2 decimals"
    (is (= "[COMPRA]   #1 PETR4   100 @ 30.50  OK registrada"
           (term/format-buy-result {:symbol "PETR4" :quantity 100 :price 30.5} accepted))))

  (testing "rejected order shows the sent params and the reason"
    (is (= "[COMPRA]      VALE3     0 @ 65.00  REJEITADA: quantidade deve ser um inteiro > 0"
           (term/format-buy-result {:symbol "VALE3" :quantity 0 :price 65}
                                   {:error {:code :invalid-quantity
                                            :message "quantidade deve ser um inteiro > 0"}}))))

  (testing "non-numeric price does not break formatting"
    (is (re-find #"@ \"abc\""
                 (term/format-buy-result {:symbol "PETR4" :quantity 1 :price "abc"}
                                         {:error {:code :invalid-price :message "x"}})))))

(deftest format-bids-test
  (testing "lists bids in the given order using remaining quantity"
    (is (= (str "=== LIVRO PETR4 - COMPRA ===\n"
                "  #2     50 @ 31.00\n"
                "  #1    100 @ 30.50")
           (term/format-bids "PETR4" [{:id 2 :remaining 50 :price 31M}
                                      {:id 1 :remaining 100 :price 30.5M}]))))

  (testing "empty side"
    (is (= "=== LIVRO VALE3 - COMPRA ===\n  (vazio)"
           (term/format-bids "VALE3" [])))))
