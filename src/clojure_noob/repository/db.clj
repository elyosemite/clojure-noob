(ns clojure-noob.repository.db
  "In-memory database: a single atom holding the whole application state."
  (:require [clojure-noob.domain.order-book :as book]))

(def initial-state
  {:book          book/empty-book
   :orders        {}          ; id -> order (latest status)
   :trades        []          ; in execution order
   :next-id       1
   :next-trade-id 1})

(defonce db (atom initial-state))

(defn reset-db! []
  (reset! db initial-state))
