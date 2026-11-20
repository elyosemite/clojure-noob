(ns clojure-noob.service.user-service-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure-noob.repository.db :as db]
            [clojure-noob.service.user-service :as users]))

(use-fixtures :each (fn [t] (db/reset-db!) (t) (db/reset-db!)))

(deftest register-user-pure-test
  (testing "creates user and wallet with sequential ids"
    (let [{:keys [state result]} (users/register-user db/initial-state "Ana" 20000)]
      (is (= {:id 1 :name "Ana"} (:ok result)))
      (is (= 20000M (get-in state [:wallets 1 :balance])))
      (is (= 2 (:next-user-id state))))))

(deftest register-user!-test
  (testing "stores user and wallet with initial stock"
    (let [r (users/register-user! "Bruno" 0 {"PETR4" 500 "VALE3" 100})]
      (is (= {:id 1 :name "Bruno"} (:ok r)))
      (is (= {:id 1 :name "Bruno"} (users/find-user 1)))
      (is (= {:balance 0M :reserved-cash 0M
              :stock {"PETR4" 500 "VALE3" 100} :reserved-stock {}}
             (users/find-wallet 1)))
      (is (= 2 (get-in (users/register-user! "Carla" 1000) [:ok :id]))))))

(deftest register-user-validation-test
  (testing "invalid input is rejected without changing state"
    (doseq [[args code] [[["  " 100]                    :invalid-name]
                         [[nil 100]                     :invalid-name]
                         [["Ana" -1]                    :invalid-balance]
                         [["Ana" "100"]                 :invalid-balance]
                         [["Ana" 100 {"XPTO3" 10}]      :invalid-stock]
                         [["Ana" 100 {"PETR4" -5}]      :invalid-stock]
                         [["Ana" 100 {"PETR4" 1.5}]     :invalid-stock]]]
      (let [before @db/db]
        (is (= code (get-in (apply users/register-user! args) [:error :code]))
            (pr-str args))
        (is (= before @db/db))))))

(deftest unknown-user-test
  (is (nil? (users/find-user 42)))
  (is (nil? (users/find-wallet 42))))
