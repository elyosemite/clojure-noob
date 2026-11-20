(ns clojure-noob.core-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure-noob.core :as core]
            [clojure-noob.repository.db :as db]))

(use-fixtures :each (fn [t] (db/reset-db!) (t) (db/reset-db!)))

(deftest main-demo-test
  (testing "demo prints accepted and rejected orders and both books"
    (let [out (with-out-str (core/-main))]
      (is (str/includes? out "[COMPRA]   #1 PETR4   100 @ 30.50  OK registrada"))
      (is (str/includes? out "[VENDA]    #5 PETR4    80 @ 31.20  OK registrada"))
      (is (str/includes? out "REJEITADA: quantidade"))
      (is (str/includes? out "REJEITADA: preço deve ser > 0"))
      (is (str/includes? out "REJEITADA: ativo desconhecido: XPTO3"))
      (is (str/includes? out (str "=== LIVRO PETR4 ===\n"
                                  "        COMPRA         |        VENDA\n"
                                  "   #2     50 @   31.00 |    #5     80 @   31.20\n"
                                  "   #1    100 @   30.50 |    #7     30 @   31.20\n"
                                  "   #4    200 @   30.50 |    #6    100 @   31.50\n"
                                  "spread: 0.20")))
      (is (str/includes? out (str "=== LIVRO VALE3 ===\n"
                                  "        COMPRA         |        VENDA\n"
                                  "   #3     10 @   65.00 |    #8     20 @   66.00\n"
                                  "spread: 1.00"))))))
