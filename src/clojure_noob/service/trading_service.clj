(ns clojure-noob.service.trading-service
  (:require [clojure-noob.domain.order :as order]
            [clojure-noob.domain.order-book :as book]
            [clojure-noob.repository.db :refer [db]]))

;; ---------------------------------------------------------------------------
;; Functional core

(defn place-order
  "Pure use case. Returns {:state new-state :result {:ok order}|{:error ...}}.
  On error the state is returned unchanged (the id is not consumed)."
  [state side params timestamp]
  (let [id     (:next-id state)
        result (order/create-order side (assoc params :id id :timestamp timestamp))]
    (if-let [o (:ok result)]
      {:state  (-> state
                   (update :book book/add-order o)
                   (assoc-in [:orders id] o)
                   (update :next-id inc))
       :result result}
      {:state state :result result})))

;; ---------------------------------------------------------------------------
;; Imperative shell

(defn now [] (System/currentTimeMillis))

(defn place-buy-order!
  "Registers a buy limit order. `params`: {:symbol :quantity :price}.
  Returns {:ok order} or {:error {:code :message}}."
  [params]
  (let [ts (now)
        ;; place-order is pure, so re-running it on the old state
        ;; yields exactly the result that produced the new state.
        [old _] (swap-vals! db #(:state (place-order % :buy params ts)))]
    (:result (place-order old :buy params ts))))

(defn bids [symbol]
  (book/bids (:book @db) symbol))

(defn find-order [id]
  (get-in @db [:orders id]))
