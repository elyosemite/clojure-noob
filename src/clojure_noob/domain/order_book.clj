(ns clojure-noob.domain.order-book
  "Pure order book: every function takes a book and returns a new one.

  Shape: {\"PETR4\" {:bids [order ...]}}
  Bids are kept sorted by price-time priority.")

(def empty-book {})

(def ^:private bid-priority
  "Highest price first; ties broken by oldest timestamp, then lowest id."
  (juxt (comp - :price) :timestamp :id))

(defn- insert-bid [bids order]
  (vec (sort-by bid-priority (conj (or bids []) order))))

(defn add-order
  "Adds a resting order to the book of its symbol."
  [book {:keys [side symbol] :as order}]
  (case side
    :buy (update-in book [symbol :bids] insert-bid order)))

(defn bids
  "Buy orders for `symbol`, best (highest price) first."
  [book symbol]
  (get-in book [symbol :bids] []))
