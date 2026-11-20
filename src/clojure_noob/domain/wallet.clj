(ns clojure-noob.domain.wallet
  "Pure wallet: available vs reserved cash and stock.

  Shape: {:balance        available cash (bigdec)
          :reserved-cash  cash locked by open buy orders
          :stock          {symbol available-quantity}
          :reserved-stock {symbol quantity locked by open sell orders}}")

(defn money
  "Cash amount as bigdec with 2 decimals."
  [x]
  (.setScale (bigdec x) 2 java.math.RoundingMode/HALF_UP))

(defn- fmt [x] (.toPlainString (money x)))

(defn- error [code message]
  {:error {:code code :message message}})

(defn- adjust
  "Adds `delta` to `k` in `m`, dropping the key when it reaches zero."
  [m k delta]
  (let [v (+ (get m k 0) delta)]
    (if (zero? v) (dissoc m k) (assoc m k v))))

(defn create-wallet
  ([balance] (create-wallet balance {}))
  ([balance stock]
   {:balance        (money balance)
    :reserved-cash  (money 0)
    :stock          (into {} (remove (comp zero? val)) stock)
    :reserved-stock {}}))

;; --- reservations ----------------------------------------------------------

(defn reserve-cash
  "Moves `amount` from available balance to reserved cash."
  [{:keys [balance] :as wallet} amount]
  (if (>= balance amount)
    {:ok (-> wallet
             (update :balance - amount)
             (update :reserved-cash + amount))}
    (error :insufficient-balance
           (str "saldo insuficiente: necessário " (fmt amount)
                ", disponível " (fmt balance)))))

(defn release-cash
  "Returns `amount` of reserved cash to the available balance."
  [wallet amount]
  (-> wallet
      (update :reserved-cash - amount)
      (update :balance + amount)))

(defn reserve-stock
  "Moves `quantity` of `symbol` from available stock to reserved stock."
  [wallet symbol quantity]
  (let [available (get-in wallet [:stock symbol] 0)]
    (if (>= available quantity)
      {:ok (-> wallet
               (update :stock adjust symbol (- quantity))
               (update :reserved-stock adjust symbol quantity))}
      (error :insufficient-stock
             (str "ativos insuficientes: " symbol " necessário " quantity
                  ", disponível " available)))))

(defn release-stock
  "Returns `quantity` of reserved `symbol` to available stock."
  [wallet symbol quantity]
  (-> wallet
      (update :reserved-stock adjust symbol (- quantity))
      (update :stock adjust symbol quantity)))

;; --- settlement ------------------------------------------------------------

(defn settle-buy
  "Buyer side of a trade. The reservation was made at `limit-price`; the trade
  happened at `trade-price` (<= limit), so the difference returns to balance."
  [wallet symbol quantity trade-price limit-price]
  (-> wallet
      (update :reserved-cash - (* quantity limit-price))
      (update :balance + (* quantity (- limit-price trade-price)))
      (update :stock adjust symbol quantity)))

(defn settle-sell
  "Seller side of a trade: reserved stock leaves, cash comes in."
  [wallet symbol quantity trade-price]
  (-> wallet
      (update :reserved-stock adjust symbol (- quantity))
      (update :balance + (* quantity trade-price))))
