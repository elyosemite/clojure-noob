(ns clojure-noob.presentation.terminal-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure-noob.presentation.terminal :as term]))

(defn- accepted [side id]
  {:ok {:id id :side side :symbol "PETR4" :quantity 100 :remaining 100
        :price 30.5M :status :open :timestamp 1000}})

(deftest format-order-result-test
  (testing "accepted buy shows id, symbol, quantity and price with 2 decimals"
    (is (= "[COMPRA]   #1 PETR4   100 @ 30.50  OK registrada"
           (term/format-order-result :buy {:symbol "PETR4" :quantity 100 :price 30.5}
                                     (accepted :buy 1)))))

  (testing "accepted sell uses the VENDA label with the same alignment"
    (is (= "[VENDA]    #2 PETR4   100 @ 30.50  OK registrada"
           (term/format-order-result :sell {:symbol "PETR4" :quantity 100 :price 30.5}
                                     (accepted :sell 2)))))

  (testing "rejected order shows the sent params and the reason"
    (is (= "[COMPRA]      VALE3     0 @ 65.00  REJEITADA: quantidade deve ser um inteiro > 0"
           (term/format-order-result :buy {:symbol "VALE3" :quantity 0 :price 65}
                                     {:error {:code :invalid-quantity
                                              :message "quantidade deve ser um inteiro > 0"}}))))

  (testing "non-numeric price does not break formatting"
    (is (re-find #"@ \"abc\""
                 (term/format-order-result :sell {:symbol "PETR4" :quantity 1 :price "abc"}
                                           {:error {:code :invalid-price :message "x"}})))))

(deftest format-book-test
  (testing "bids and asks side by side, padded when one side is shorter, with spread"
    (is (= (str "=== LIVRO PETR4 ===\n"
                "        COMPRA         |        VENDA\n"
                "   #2     50 @   31.00 |    #5     80 @   31.20\n"
                "   #1    100 @   30.50 |    #6    100 @   31.50\n"
                "                       |    #7     30 @   32.00\n"
                "spread: 0.20")
           (term/format-book "PETR4"
                             [{:id 2 :remaining 50 :price 31M}
                              {:id 1 :remaining 100 :price 30.5M}]
                             [{:id 5 :remaining 80 :price 31.2M}
                              {:id 6 :remaining 100 :price 31.5M}
                              {:id 7 :remaining 30 :price 32M}]
                             0.2M))))

  (testing "only bids: right column empty and no spread"
    (is (= (str "=== LIVRO VALE3 ===\n"
                "        COMPRA         |        VENDA\n"
                "   #3     10 @   65.00 |\n"
                "spread: -")
           (term/format-book "VALE3" [{:id 3 :remaining 10 :price 65M}] [] nil))))

  (testing "empty book"
    (is (= (str "=== LIVRO VALE3 ===\n"
                "        COMPRA         |        VENDA\n"
                "  (vazio)\n"
                "spread: -")
           (term/format-book "VALE3" [] [] nil)))))

;; --- matching (US-03) ------------------------------------------------------

(deftest format-order-result-status-test
  (testing "filled order"
    (is (= "[COMPRA]   #9 PETR4   100 @ 30.50  OK executada"
           (term/format-order-result :buy {} (assoc-in (accepted :buy 9) [:ok :status] :filled)))))

  (testing "partially filled order shows what is left"
    (is (= "[VENDA]   #10 PETR4   100 @ 30.50  OK executada parcialmente (resta 70)"
           (term/format-order-result :sell {}
                                     (update (accepted :sell 10) :ok assoc
                                             :status :partially-filled :remaining 70))))))

(deftest format-trade-test
  (is (= "    TRADE PETR4    80 @ 31.20 (compra #9 x venda #5)"
         (term/format-trade {:id 1 :symbol "PETR4" :quantity 80 :price 31.2M
                             :buy-order-id 9 :sell-order-id 5 :timestamp 1}))))

;; --- cancel (US-04) --------------------------------------------------------

(deftest format-cancel-result-test
  (testing "cancelled order shows the remaining quantity that left the book"
    (is (= "[CANCELA]   #7 PETR4    10 @ 31.20  OK cancelada"
           (term/format-cancel-result 7 {:ok {:id 7 :side :sell :symbol "PETR4" :quantity 30
                                              :remaining 10 :price 31.2M :status :cancelled}}))))

  (testing "rejected cancel shows the id and the reason"
    (is (= "[CANCELA]  #99  REJEITADO: ordem não encontrada: #99"
           (term/format-cancel-result 99 {:error {:code :order-not-found
                                                  :message "ordem não encontrada: #99"}})))))
