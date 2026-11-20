(ns clojure-noob.service.trading-service
  (:require [clojure-noob.domain.matching :as matching]
            [clojure-noob.domain.order :as order]
            [clojure-noob.domain.order-book :as book]
            [clojure-noob.domain.trade :as trade]
            [clojure-noob.repository.db :refer [db]]))

;; ---------------------------------------------------------------------------
;; Functional core

(defn- fills->trades [first-id timestamp aggressor fills]
  (vec (map-indexed (fn [i {:keys [resting quantity]}]
                      (trade/create-trade {:id        (+ first-id i)
                                           :timestamp timestamp
                                           :aggressor aggressor
                                           :resting   resting
                                           :quantity  quantity}))
                    fills)))

(defn place-order
  "Pure use case: validates, matches against the book and records everything.
  Returns {:state new-state :result {:ok order :trades [...]}|{:error ...}}.
  `order` reflects its status after matching. On error the state is returned
  unchanged (the id is not consumed)."
  [state side params timestamp]
  (let [id     (:next-id state)
        result (order/create-order side (assoc params :id id :timestamp timestamp))]
    (if-let [incoming (:ok result)]
      (let [{:keys [book order fills]} (matching/match (:book state) incoming)
            trades  (fills->trades (:next-trade-id state) timestamp order fills)
            resting (into {} (map (juxt :id identity)) (map :resting fills))]
        {:state  (-> state
                     (assoc :book book)
                     (update :orders merge resting {id order})
                     (update :trades into trades)
                     (update :next-trade-id + (count trades))
                     (update :next-id inc))
         :result {:ok order :trades trades}})
      {:state state :result result})))

(defn cancel-order
  "Pure use case: cancels order `id` and removes it from the book.
  Returns {:state new-state :result {:ok order}|{:error ...}}.
  Errors: :order-not-found, :not-cancellable (state unchanged)."
  [state id]
  (if-let [existing (get-in state [:orders id])]
    (let [result (order/cancel existing)]
      (if-let [cancelled (:ok result)]
        {:state  (-> state
                     (update :book book/remove-order cancelled)
                     (assoc-in [:orders id] cancelled))
         :result result}
        {:state state :result result}))
    {:state  state
     :result {:error {:code    :order-not-found
                      :message (str "ordem não encontrada: #" id)}}}))

;; ---------------------------------------------------------------------------
;; Imperative shell

(defn now [] (System/currentTimeMillis))

(defn- place-order!
  [side params]
  (let [ts (now)
        ;; place-order is pure, so re-running it on the old state
        ;; yields exactly the result that produced the new state.
        [old _] (swap-vals! db #(:state (place-order % side params ts)))]
    (:result (place-order old side params ts))))

(defn place-buy-order!
  "Registers a buy limit order. `params`: {:symbol :quantity :price}.
  Returns {:ok order} or {:error {:code :message}}."
  [params]
  (place-order! :buy params))

(defn place-sell-order!
  "Registers a sell limit order. `params`: {:symbol :quantity :price}.
  Returns {:ok order} or {:error {:code :message}}."
  [params]
  (place-order! :sell params))

(defn bids [symbol]
  (book/bids (:book @db) symbol))

(defn asks [symbol]
  (book/asks (:book @db) symbol))

(defn spread [symbol]
  (book/spread (:book @db) symbol))

(defn find-order [id]
  (get-in @db [:orders id]))

(defn trades []
  (:trades @db))

(defn cancel-order!
  "Cancels order `id`. Returns {:ok order} or {:error {:code :message}}."
  [id]
  (let [[old _] (swap-vals! db #(:state (cancel-order % id)))]
    (:result (cancel-order old id))))
