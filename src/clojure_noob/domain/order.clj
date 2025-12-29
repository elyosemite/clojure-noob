(ns clojure-noob.domain.order)

(defn create-order
  [{:keys [user-id type symbol quantity price]}]
  {
   :id (random-uuid)
   :user-id user-id
   :type type
   :symbol symbol
   :quantity quantity
   :price price
   :timestamp (System/currnetTimeMillis)})
