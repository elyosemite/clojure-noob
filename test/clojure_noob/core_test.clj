(ns clojure-noob.core-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure-noob.core :as core]
            [clojure-noob.repository.db :as db]))

(use-fixtures :each (fn [t] (db/reset-db!) (t) (db/reset-db!)))

(deftest main-demo-test
  ;; println emits \r\n on Windows; normalize so assertions are OS independent
  (let [out (str/replace (with-out-str (core/-main)) "\r\n" "\n")]
    (testing "accepted and rejected orders"
      (is (str/includes? out "[COMPRA]   #1 PETR4   100 @ 30.50  OK registrada"))
      (is (str/includes? out "[VENDA]    #5 PETR4    80 @ 31.20  OK registrada"))
      (is (str/includes? out "REJEITADA: quantidade"))
      (is (str/includes? out "REJEITADA: preço deve ser > 0"))
      (is (str/includes? out "REJEITADA: ativo desconhecido: XPTO3")))

    (testing "crossing orders print their status and trades"
      (is (str/includes? out "[COMPRA]   #9 PETR4   100 @ 31.30  OK executada"))
      (is (str/includes? out "TRADE PETR4    80 @ 31.20 (compra #9 x venda #5)"))
      (is (str/includes? out "TRADE PETR4    20 @ 31.20 (compra #9 x venda #7)"))
      (is (str/includes? out "[VENDA]   #10 PETR4   120 @ 30.80  OK executada parcialmente (resta 70)"))
      (is (str/includes? out "TRADE PETR4    50 @ 31.00 (compra #2 x venda #10)"))
      (is (str/includes? out "TRADE VALE3    20 @ 66.00 (compra #11 x venda #8)")))

    (testing "cancellations"
      (is (str/includes? out "[CANCELA]   #4 PETR4   200 @ 30.50  OK cancelada"))
      (is (str/includes? out "[CANCELA]   #7 PETR4    10 @ 31.20  OK cancelada"))
      (is (str/includes? out "[CANCELA]   #5  REJEITADO: ordem já executada"))
      (is (str/includes? out "[CANCELA]  #99  REJEITADO: ordem não encontrada: #99"))
      (is (str/includes? out "[CANCELA]   #4  REJEITADO: ordem já cancelada")))

    (testing "PETR4 book after cancellations"
      (is (str/includes? out (str "##### LIVROS DEPOIS DOS CANCELAMENTOS #####\n\n"
                                  "=== LIVRO PETR4 ===\n"
                                  "        COMPRA         |        VENDA\n"
                                  "   #1    100 @   30.50 |   #10     70 @   30.80\n"
                                  "                       |    #6    100 @   31.50\n"
                                  "spread: 0.30"))))

    (testing "books before cancellations"
      (is (str/includes? out (str "=== LIVRO PETR4 ===\n"
                                  "        COMPRA         |        VENDA\n"
                                  "   #1    100 @   30.50 |   #10     70 @   30.80\n"
                                  "   #4    200 @   30.50 |    #7     10 @   31.20\n"
                                  "                       |    #6    100 @   31.50\n"
                                  "spread: 0.30")))
      (is (str/includes? out (str "=== LIVRO VALE3 ===\n"
                                  "        COMPRA         |        VENDA\n"
                                  "  #11     10 @   66.00 |\n"
                                  "   #3     10 @   65.00 |\n"
                                  "spread: -"))))))
