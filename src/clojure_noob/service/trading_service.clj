(ns clojure-noob.service.trading-service
  (:require [clojure-noob.domain.matching :as matching]
            [clojure-noob.domain.order :as order]
            [clojure-noob.domain.order-book :as book]
            [clojure-noob.domain.trade :as trade]
            [clojure-noob.domain.wallet :as wallet]
            [clojure-noob.repository.db :refer [db]]))

;; ---------------------------------------------------------------------------
;; Functional core

(defn- error [code message]
  {:error {:code code :message message}})

(defn- fills->trades [first-id timestamp aggressor fills]
  (vec (map-indexed (fn [i {:keys [resting quantity]}]
                      (trade/create-trade {:id        (+ first-id i)
                                           :timestamp timestamp
                                           :aggressor aggressor
                                           :resting   resting
                                           :quantity  quantity}))
                    fills)))

(defn- reserve
  "Locks what the order may need: cash for buys (quantity x limit), stock for sells."
  [w {:keys [side symbol quantity price]}]
  (case side
    :buy  (wallet/reserve-cash w (* quantity price))
    :sell (wallet/reserve-stock w symbol quantity)))

(defn- release
  "Unlocks what is still reserved for the order's remaining quantity."
  [w {:keys [side symbol remaining price]}]
  (case side
    :buy  (wallet/release-cash w (* remaining price))
    :sell (wallet/release-stock w symbol remaining)))

(defn- settle-trades
  "Settles each trade in both wallets. Buy reservations were made at the buy
  order's limit: the incoming price when it is the buyer, otherwise the resting
  buy price (which is the trade price)."
  [wallets incoming trades]
  (reduce (fn [ws {:keys [buyer-id seller-id symbol quantity price]}]
            (let [limit (if (= :buy (:side incoming)) (:price incoming) price)]
              (-> ws
                  (update buyer-id wallet/settle-buy symbol quantity price limit)
                  (update seller-id wallet/settle-sell symbol quantity price))))
          wallets
          trades))

(defn place-order
  "Pure use case: validates, reserves funds, matches against the book and
  settles trades. Returns {:state new-state :result {:ok order :trades [...]}|{:error ...}}.
  `order` reflects its status after matching. On error the state is returned
  unchanged (the id is not consumed).
  Errors: order validation, :user-not-found, :insufficient-balance, :insufficient-stock."
  [state side params timestamp]
  (let [id       (:next-id state)
        result   (order/create-order side (assoc params :id id :timestamp timestamp))
        incoming (:ok result)
        w        (when incoming (get-in state [:wallets (:user-id incoming)]))
        reserved (when w (reserve w incoming))]
    (cond
      (:error result)   {:state state :result result}
      (nil? w)          {:state  state
                         :result (error :user-not-found
                                        (str "usuário não encontrado: #" (:user-id incoming)))}
      (:error reserved) {:state state :result reserved}
      :else
      (let [{:keys [book order fills]} (matching/match (:book state) incoming)
            trades  (fills->trades (:next-trade-id state) timestamp order fills)
            resting (into {} (map (juxt :id identity)) (map :resting fills))]
        {:state  (-> state
                     (assoc-in [:wallets (:user-id order)] (:ok reserved))
                     (update :wallets settle-trades order trades)
                     (assoc :book book)
                     (update :orders merge resting {id order})
                     (update :trades into trades)
                     (update :next-trade-id + (count trades))
                     (update :next-id inc))
         :result {:ok order :trades trades}}))))

(defn cancel-order
  "Pure use case: cancels order `id`, removes it from the book and releases
  its remaining reservation. Returns {:state new-state :result {:ok order}|{:error ...}}.
  Errors: :order-not-found, :not-cancellable (state unchanged)."
  [state id]
  (if-let [existing (get-in state [:orders id])]
    (let [result (order/cancel existing)]
      (if-let [cancelled (:ok result)]
        {:state  (-> state
                     (update :book book/remove-order cancelled)
                     (assoc-in [:orders id] cancelled)
                     (update-in [:wallets (:user-id cancelled)] release cancelled))
         :result result}
        {:state state :result result}))
    {:state  state
     :result (error :order-not-found (str "ordem não encontrada: #" id))}))

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
  "Registers a buy limit order. `params`: {:user-id :symbol :quantity :price}.
  Returns {:ok order} or {:error {:code :message}}."
  [params]
  (place-order! :buy params))

(defn place-sell-order!
  "Registers a sell limit order. `params`: {:user-id :symbol :quantity :price}.
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
