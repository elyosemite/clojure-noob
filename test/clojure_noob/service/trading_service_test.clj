(ns clojure-noob.service.trading-service-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure-noob.repository.db :as db]
            [clojure-noob.service.trading-service :as svc]
            [clojure-noob.service.user-service :as users]))

(def rich-stock {"PETR4" 10000 "VALE3" 10000})

(def seeded
  "State with user #1 holding plenty of cash and stock."
  (:state (users/register-user db/initial-state "Teste" 1000000 rich-stock)))

(use-fixtures :each (fn [t] (reset! db/db seeded) (t) (db/reset-db!)))

(def valid {:user-id 1 :symbol "PETR4" :quantity 100 :price 30.50})

;; --- functional core -------------------------------------------------------

(deftest place-order-success-test
  (testing "valid order gets next id, timestamp and lands in book and orders"
    (let [{:keys [state result]} (svc/place-order seeded :buy valid 1000)
          o (:ok result)]
      (is (= 1 (:id o)))
      (is (= 1000 (:timestamp o)))
      (is (= o (get-in state [:orders 1])))
      (is (= 2 (:next-id state)))
      (is (= [o] (get-in state [:book "PETR4" :bids]))))))

(deftest place-order-error-test
  (testing "invalid order returns error and leaves state untouched"
    (let [{:keys [state result]} (svc/place-order seeded :buy
                                                  (assoc valid :quantity 0) 1000)]
      (is (= :invalid-quantity (get-in result [:error :code])))
      (is (= seeded state)))))

;; --- imperative shell ------------------------------------------------------

(deftest place-buy-order!-test
  (testing "registers orders with sequential ids and exposes them in the book"
    (let [r1 (svc/place-buy-order! valid)
          r2 (svc/place-buy-order! (assoc valid :price 31.00 :quantity 50))]
      (is (= [1 2] [(get-in r1 [:ok :id]) (get-in r2 [:ok :id])]))
      (is (= [2 1] (map :id (svc/bids "PETR4"))))
      (is (= (:ok r1) (svc/find-order 1))))))

(deftest place-buy-order!-rejection-test
  (testing "rejected order does not change state nor consume an id"
    (let [before @db/db
          r      (svc/place-buy-order! (assoc valid :symbol "XPTO3"))]
      (is (= :unknown-symbol (get-in r [:error :code])))
      (is (= before @db/db))
      (is (= 1 (get-in (svc/place-buy-order! valid) [:ok :id]))))))

(deftest place-buy-order!-uses-clock-test
  (testing "timestamp comes from the clock"
    (with-redefs [svc/now (constantly 42)]
      (is (= 42 (get-in (svc/place-buy-order! valid) [:ok :timestamp]))))))

;; --- sell side (US-02) -----------------------------------------------------

(deftest place-order-sell-test
  (testing "pure core puts sell orders on the asks side"
    (let [{:keys [state result]} (svc/place-order seeded :sell valid 1000)]
      (is (= :sell (get-in result [:ok :side])))
      (is (= [(:ok result)] (get-in state [:book "PETR4" :asks]))))))

(deftest place-sell-order!-test
  (testing "registers sell orders sharing the id sequence with buys"
    (let [b (svc/place-buy-order! valid)
          s (svc/place-sell-order! (assoc valid :price 31.20 :quantity 80))]
      (is (= [1 2] [(get-in b [:ok :id]) (get-in s [:ok :id])]))
      (is (= [1] (map :id (svc/bids "PETR4"))))
      (is (= [2] (map :id (svc/asks "PETR4"))))
      (is (= (:ok s) (svc/find-order 2)))))

  (testing "spread reflects best bid and best ask"
    (is (= 0.70M (svc/spread "PETR4")))))

(deftest place-sell-order!-rejection-test
  (testing "rejected sell order leaves state untouched"
    (let [before @db/db
          r      (svc/place-sell-order! (assoc valid :price 0))]
      (is (= :invalid-price (get-in r [:error :code])))
      (is (= before @db/db))
      (is (nil? (svc/spread "PETR4"))))))

;; --- matching (US-03) ------------------------------------------------------

(deftest place-order-matching-test
  (testing "crossing order generates trades and updates resting orders"
    (let [s1 (svc/place-sell-order! {:user-id 1 :symbol "PETR4" :quantity 80 :price 31.20})
          s2 (svc/place-sell-order! {:user-id 1 :symbol "PETR4" :quantity 30 :price 31.20})
          r  (svc/place-buy-order!  {:user-id 1 :symbol "PETR4" :quantity 100 :price 31.30})]
      (is (= [1 2] [(get-in s1 [:ok :id]) (get-in s2 [:ok :id])]))
      (testing "incoming order is returned after matching"
        (is (= [3 :filled 0] ((juxt :id :status :remaining) (:ok r)))))
      (testing "trades are returned and stored with sequential ids"
        (is (= [[1 80 31.20M 3 1] [2 20 31.20M 3 2]]
               (map (juxt :id :quantity :price :buy-order-id :sell-order-id) (:trades r))))
        (is (= (:trades r) (svc/trades))))
      (testing "resting orders reflect their fills"
        (is (= :filled (:status (svc/find-order 1))))
        (is (= [:partially-filled 10] ((juxt :status :remaining) (svc/find-order 2))))
        (is (= :filled (:status (svc/find-order 3)))))
      (testing "book keeps only what was not executed"
        (is (= [[2 10]] (map (juxt :id :remaining) (svc/asks "PETR4"))))
        (is (= [] (svc/bids "PETR4")))))))

(deftest place-order-no-trade-test
  (testing "non-crossing order returns empty trades"
    (is (= [] (:trades (svc/place-buy-order! valid))))
    (is (= [] (svc/trades)))))

(deftest rejection-keeps-trade-sequence-test
  (testing "rejected order creates no trade and does not touch the trade sequence"
    (svc/place-sell-order! {:user-id 1 :symbol "PETR4" :quantity 10 :price 30.00})
    (let [before @db/db]
      (is (contains? (svc/place-buy-order! (assoc valid :quantity -1)) :error))
      (is (= before @db/db)))))

;; --- cancel (US-04) --------------------------------------------------------

(deftest cancel-open-order-test
  (testing "open order is cancelled and leaves the book"
    (svc/place-buy-order! valid)
    (svc/place-buy-order! (assoc valid :price 31.00))
    (let [r (svc/cancel-order! 1)]
      (is (= [1 :cancelled 100] ((juxt :id :status :remaining) (:ok r))))
      (is (= :cancelled (:status (svc/find-order 1))))
      (is (= [2] (map :id (svc/bids "PETR4")))))))

(deftest cancel-partially-filled-order-test
  (testing "partially filled order is cancelled and its remaining leaves the book"
    (svc/place-sell-order! {:user-id 1 :symbol "PETR4" :quantity 100 :price 31.00})
    (svc/place-buy-order!  {:user-id 1 :symbol "PETR4" :quantity 40  :price 31.00})
    (let [r (svc/cancel-order! 1)]
      (is (= [:cancelled 60] ((juxt :status :remaining) (:ok r))))
      (is (= [] (svc/asks "PETR4"))))))

(deftest cancelled-order-does-not-match-test
  (testing "a new crossing order does not trade with a cancelled one"
    (svc/place-sell-order! {:user-id 1 :symbol "PETR4" :quantity 100 :price 31.00})
    (svc/cancel-order! 1)
    (let [r (svc/place-buy-order! {:user-id 1 :symbol "PETR4" :quantity 100 :price 31.00})]
      (is (= [] (:trades r)))
      (is (= :open (get-in r [:ok :status]))))))

(deftest cancel-errors-test
  (svc/place-sell-order! {:user-id 1 :symbol "PETR4" :quantity 10 :price 31.00})
  (svc/place-buy-order!  {:user-id 1 :symbol "PETR4" :quantity 10 :price 31.00})   ; fills #1 and #2
  (svc/place-buy-order!  valid)                                          ; #3 open

  (testing "unknown id"
    (let [before @db/db
          r      (svc/cancel-order! 99)]
      (is (= :order-not-found (get-in r [:error :code])))
      (is (= "ordem não encontrada: #99" (get-in r [:error :message])))
      (is (= before @db/db))))

  (testing "filled order"
    (let [before @db/db]
      (is (= :not-cancellable (get-in (svc/cancel-order! 1) [:error :code])))
      (is (= before @db/db))))

  (testing "cancelling twice"
    (is (contains? (svc/cancel-order! 3) :ok))
    (let [before @db/db]
      (is (= "ordem já cancelada" (get-in (svc/cancel-order! 3) [:error :message])))
      (is (= before @db/db)))))

;; --- settlement (US-05) ----------------------------------------------------

(defn- register! [name balance & [stock]]
  (get-in (users/register-user! name balance (or stock {})) [:ok :id]))

(defn- wallet [id] (users/find-wallet id))

(deftest unknown-user-test
  (testing "order from an unregistered user is rejected"
    (let [before @db/db
          r      (svc/place-buy-order! (assoc valid :user-id 99))]
      (is (= :user-not-found (get-in r [:error :code])))
      (is (= "usuário não encontrado: #99" (get-in r [:error :message])))
      (is (= before @db/db)))))

(deftest insufficient-funds-test
  (let [carla (register! "Carla" 1000)]
    (testing "buy needs quantity x limit in available balance"
      (let [before @db/db
            r      (svc/place-buy-order! {:user-id carla :symbol "PETR4" :quantity 100 :price 31.00})]
        (is (= :insufficient-balance (get-in r [:error :code])))
        (is (= before @db/db))))

    (testing "sell needs available stock"
      (let [before @db/db
            r      (svc/place-sell-order! {:user-id carla :symbol "VALE3" :quantity 10 :price 66.00})]
        (is (= :insufficient-stock (get-in r [:error :code])))
        (is (= before @db/db))))))

(deftest reservation-test
  (let [carla (register! "Carla" 1000 {"VALE3" 5})]
    (testing "open buy locks quantity x limit"
      (svc/place-buy-order! {:user-id carla :symbol "PETR4" :quantity 10 :price 30.00})
      (is (= [700M 300M] ((juxt :balance :reserved-cash) (wallet carla)))))

    (testing "open sell locks stock"
      (svc/place-sell-order! {:user-id carla :symbol "VALE3" :quantity 5 :price 70.00})
      (is (= [{} {"VALE3" 5}] ((juxt :stock :reserved-stock) (wallet carla)))))))

(deftest buy-aggressor-settlement-test
  (testing "buyer pays the resting price and gets the limit difference back"
    (let [ana   (register! "Ana" 10000)
          bruno (register! "Bruno" 0 {"PETR4" 500})]
      (svc/place-sell-order! {:user-id bruno :symbol "PETR4" :quantity 80 :price 31.20})
      (svc/place-buy-order!  {:user-id ana   :symbol "PETR4" :quantity 100 :price 31.30})
      ;; Ana reserved 3130; paid 80 x 31.20 = 2496; 80 x 0.10 = 8 back; 20 x 31.30 = 626 still reserved
      (is (= {:balance 6878M :reserved-cash 626M :stock {"PETR4" 80} :reserved-stock {}}
             (wallet ana)))
      (is (= {:balance 2496M :reserved-cash 0M :stock {"PETR4" 420} :reserved-stock {}}
             (wallet bruno))))))

(deftest sell-aggressor-settlement-test
  (testing "seller receives the resting buy price; its remaining stays reserved"
    (let [ana   (register! "Ana" 10000)
          bruno (register! "Bruno" 0 {"PETR4" 500})]
      (svc/place-buy-order!  {:user-id ana   :symbol "PETR4" :quantity 50  :price 31.00})
      (svc/place-sell-order! {:user-id bruno :symbol "PETR4" :quantity 120 :price 30.80})
      (is (= {:balance 8450M :reserved-cash 0M :stock {"PETR4" 50} :reserved-stock {}}
             (wallet ana)))
      (is (= {:balance 1550M :reserved-cash 0M :stock {"PETR4" 380} :reserved-stock {"PETR4" 70}}
             (wallet bruno))))))

(deftest cancel-releases-reservation-test
  (let [ana   (register! "Ana" 10000)
        bruno (register! "Bruno" 0 {"PETR4" 500})
        buy   (get-in (svc/place-buy-order! {:user-id ana :symbol "PETR4" :quantity 100 :price 31.00})
                      [:ok :id])]
    (svc/place-sell-order! {:user-id bruno :symbol "PETR4" :quantity 40 :price 31.00})
    (testing "cancelled buy returns the cash reserved for its remaining"
      (svc/cancel-order! buy)
      (is (= [8760M 0M] ((juxt :balance :reserved-cash) (wallet ana)))))

    (testing "cancelled sell returns its remaining stock"
      (let [sell (get-in (svc/place-sell-order! {:user-id bruno :symbol "PETR4" :quantity 100 :price 32.00})
                         [:ok :id])]
        (is (= {"PETR4" 100} (:reserved-stock (wallet bruno))))
        (svc/cancel-order! sell)
        (is (= [{"PETR4" 460} {}] ((juxt :stock :reserved-stock) (wallet bruno))))))))

(defn- totals
  "Total cash (available + reserved) and stock per symbol across all wallets."
  []
  (let [ws (vals (:wallets @db/db))]
    {:cash  (reduce + (mapcat (juxt :balance :reserved-cash) ws))
     :stock (apply merge-with + (mapcat (juxt :stock :reserved-stock) ws))}))

(deftest conservation-test
  (testing "trading and cancelling never create or destroy cash or stock"
    (let [ana   (register! "Ana" 20000)
          bruno (register! "Bruno" 5000 {"PETR4" 500 "VALE3" 100})
          start (totals)]
      (svc/place-buy-order!  {:user-id ana   :symbol "PETR4" :quantity 100 :price 30.50})
      (svc/place-sell-order! {:user-id bruno :symbol "PETR4" :quantity 80  :price 31.20})
      (svc/place-buy-order!  {:user-id ana   :symbol "PETR4" :quantity 150 :price 31.40})
      (svc/place-sell-order! {:user-id bruno :symbol "PETR4" :quantity 200 :price 30.00})
      (svc/place-sell-order! {:user-id bruno :symbol "VALE3" :quantity 20  :price 66.00})
      (svc/place-buy-order!  {:user-id ana   :symbol "VALE3" :quantity 30  :price 67.00})
      (doseq [id (keys (:orders @db/db))] (svc/cancel-order! id))
      (is (seq (svc/trades)))
      (is (= (:cash start) (:cash (totals))))
      (is (= (:stock start) (:stock (totals))))
      (testing "after cancelling everything nothing stays reserved"
        (is (every? #(and (zero? (:reserved-cash %)) (empty? (:reserved-stock %)))
                    (vals (:wallets @db/db))))))))
