(ns clojure-noob.domain.matching
  "Pure matching engine (price-time priority)."
  (:require [clojure-noob.domain.order :as order]
            [clojure-noob.domain.order-book :as book]))

(defn- best-opposite [book {:keys [side symbol]}]
  (case side
    :buy  (book/best-ask book symbol)
    :sell (book/best-bid book symbol)))

(defn crosses?
  "True when `incoming` accepts `resting`'s price."
  [incoming resting]
  (case (:side incoming)
    :buy  (>= (:price incoming) (:price resting))
    :sell (<= (:price incoming) (:price resting))))

(defn match
  "Matches `incoming` against the opposite side of `book`.
  Returns {:book   new book (remaining of incoming added if any)
           :order  incoming after fills
           :fills  [{:resting resting-after-fill :quantity q} ...] in execution order}.
  Each fill is priced at the resting order's price."
  [book incoming]
  (loop [book book, incoming incoming, fills []]
    (let [resting (best-opposite book incoming)]
      (if (and resting
               (pos? (:remaining incoming))
               (crosses? incoming resting))
        (let [q        (min (:remaining incoming) (:remaining resting))
              resting' (order/fill resting q)]
          (recur (book/replace-best book resting')
                 (order/fill incoming q)
                 (conj fills {:resting resting' :quantity q})))
        {:book  (if (pos? (:remaining incoming))
                  (book/add-order book incoming)
                  book)
         :order incoming
         :fills fills}))))
