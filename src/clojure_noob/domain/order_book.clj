(ns clojure-noob.domain.order-book
  "Pure order book: every function takes a book and returns a new one.

  Shape: {\"PETR4\" {:bids [order ...] :asks [order ...]}}
  Both sides are kept sorted by price-time priority (best first).")

(def empty-book {})

(def ^:private priority
  "Sort key per side. Ties are broken by oldest timestamp, then lowest id."
  {:buy  (juxt (comp - :price) :timestamp :id)   ; highest price first
   :sell (juxt :price :timestamp :id)})           ; lowest price first

(def ^:private side-key {:buy :bids :sell :asks})

(defn- insert [side orders order]
  (vec (sort-by (priority side) (conj (or orders []) order))))

(defn add-order
  "Adds a resting order to its symbol's side (:bids for :buy, :asks for :sell)."
  [book {:keys [side symbol] :as order}]
  (update-in book [symbol (side-key side)] (partial insert side) order))

(defn bids
  "Buy orders for `symbol`, best (highest price) first."
  [book symbol]
  (get-in book [symbol :bids] []))

(defn asks
  "Sell orders for `symbol`, best (lowest price) first."
  [book symbol]
  (get-in book [symbol :asks] []))

(defn best-bid [book symbol] (first (bids book symbol)))

(defn best-ask [book symbol] (first (asks book symbol)))

(defn spread
  "Best ask price minus best bid price, or nil when either side is empty."
  [book symbol]
  (let [bid (best-bid book symbol)
        ask (best-ask book symbol)]
    (when (and bid ask)
      (- (:price ask) (:price bid)))))

(defn replace-best
  "Replaces the best order of `resting`'s side with `resting` (after a fill).
  A :filled order is removed from the book. Sorting is preserved because
  only :remaining/:status change."
  [book {:keys [side symbol status] :as resting}]
  (update-in book [symbol (side-key side)]
             (fn [orders]
               (if (= :filled status)
                 (vec (rest orders))
                 (assoc orders 0 resting)))))
