(ns clojure-noob.service.user-service
  (:require [clojure-noob.domain.market :as market]
            [clojure-noob.domain.user :as user]
            [clojure-noob.domain.wallet :as wallet]
            [clojure-noob.repository.db :refer [db]]))

(defn- error [code message]
  {:error {:code code :message message}})

(defn- valid-stock? [stock]
  (and (map? stock)
       (every? (fn [[symbol quantity]]
                 (and (market/known-symbol? symbol) (int? quantity) (not (neg? quantity))))
               stock)))

(defn- validate [name balance stock]
  (or (user/validate-name name)
      (when-not (and (number? balance) (not (neg? balance)))
        (error :invalid-balance "saldo inicial deve ser >= 0"))
      (when-not (valid-stock? stock)
        (error :invalid-stock "posição inicial inválida"))))

;; ---------------------------------------------------------------------------
;; Functional core

(defn register-user
  "Pure use case: creates a user and its wallet.
  Returns {:state new-state :result {:ok user}|{:error ...}}."
  ([state name balance] (register-user state name balance {}))
  ([state name balance stock]
   (if-let [err (validate name balance stock)]
     {:state state :result err}
     (let [id (:next-user-id state)
           u  (user/create-user id name)]
       {:state  (-> state
                    (assoc-in [:users id] u)
                    (assoc-in [:wallets id] (wallet/create-wallet balance stock))
                    (update :next-user-id inc))
        :result {:ok u}}))))

;; ---------------------------------------------------------------------------
;; Imperative shell

(defn register-user!
  "Registers a user with an initial balance and optional stock positions.
  Returns {:ok user} or {:error {:code :message}}."
  ([name balance] (register-user! name balance {}))
  ([name balance stock]
   (let [[old _] (swap-vals! db #(:state (register-user % name balance stock)))]
     (:result (register-user old name balance stock)))))

(defn find-user [id]
  (get-in @db [:users id]))

(defn find-wallet [user-id]
  (get-in @db [:wallets user-id]))
