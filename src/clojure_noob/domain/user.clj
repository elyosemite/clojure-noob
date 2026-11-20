(ns clojure-noob.domain.user
  (:require [clojure.string :as str]))

(defn create-user [id name]
  {:id   id
   :name name})

(defn validate-name
  "Returns an error map when `name` is blank, nil otherwise."
  [name]
  (when (or (not (string? name)) (str/blank? name))
    {:error {:code :invalid-name :message "nome não informado"}}))
