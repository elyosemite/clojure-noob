(ns clojure-noob.core-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure-noob.core :as core]
            [clojure-noob.repository.db :as db]))

(use-fixtures :each (fn [t] (db/reset-db!) (t) (db/reset-db!)))

(deftest main-demo-test
  (testing "demo prints accepted and rejected buys and the books"
    (let [out (with-out-str (core/-main))]
      (is (str/includes? out "#1 PETR4   100 @ 30.50  OK registrada"))
      (is (str/includes? out "REJEITADA: quantidade"))
      (is (str/includes? out "REJEITADA: ativo desconhecido: XPTO3"))
      (is (str/includes? out (str "=== LIVRO PETR4 - COMPRA ===\n"
                                  "  #2     50 @ 31.00\n"
                                  "  #1    100 @ 30.50\n"
                                  "  #4    200 @ 30.50")))
      (is (str/includes? out "=== LIVRO VALE3 - COMPRA ===\n  #3     10 @ 65.00")))))
