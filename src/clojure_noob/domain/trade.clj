(ns clojure-noob.domain.trade)

(defn create-trade
  "A trade between an incoming (aggressor) order and a resting order.
  The trade price is always the resting order's price."
  [{:keys [id timestamp aggressor resting quantity]}]
  (let [{buy :buy sell :sell} {(:side aggressor) aggressor
                               (:side resting)   resting}]
    {:id            id
     :symbol        (:symbol resting)
     :quantity      quantity
     :price         (:price resting)
     :buy-order-id  (:id buy)
     :sell-order-id (:id sell)
     :timestamp     timestamp}))
