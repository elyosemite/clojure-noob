(ns clojure-noob.domain.order
  (:require [clojure.string :as str]
            [clojure-noob.domain.market :as market]))

(defn- error [code message]
  {:error {:code code :message message}})

(defn- validate
  "Returns an error map for the first broken rule, or nil when params are valid."
  [{:keys [symbol quantity price]}]
  (cond
    (or (not (string? symbol)) (str/blank? symbol))
    (error :invalid-symbol "ativo não informado")

    (not (market/known-symbol? symbol))
    (error :unknown-symbol (str "ativo desconhecido: " symbol))

    (not (and (int? quantity) (pos? quantity)))
    (error :invalid-quantity "quantidade deve ser um inteiro > 0")

    (not (and (number? price) (pos? price)))
    (error :invalid-price "preço deve ser > 0")))

(defn create-order
  "Builds a limit order for `side` (:buy or :sell).
  `id` and `timestamp` are injected to keep the function pure.
  Returns {:ok order} or {:error {:code kw :message str}}."
  [side {:keys [id symbol quantity price timestamp] :as params}]
  (or (validate params)
      {:ok {:id        id
            :side      side
            :symbol    symbol
            :quantity  quantity
            :remaining quantity
            :price     (bigdec price)
            :status    :open
            :timestamp timestamp}}))

(defn create-buy-order [params]
  (create-order :buy params))

(defn create-sell-order [params]
  (create-order :sell params))
