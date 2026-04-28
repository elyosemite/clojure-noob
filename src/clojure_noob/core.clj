(ns clojure-noob.core
  (:gen-class)
  (:require [clojure-noob.learning.datatypes :as dt]
            [clojure-noob.learning.format :as fmt]
            [clojure-noob.learning.control-flow :as cf]
            [clojure-noob.learning.pattern-matching :as pm]))

(and :feeling_super_cool nil false)

;; (def severity :mild) ;; you just define that severity is :mild
;; (def randVar 10)
;; (def google false)

; Functions
;; (defn error-message
;;   [severity]
;;   (str "Oh GOD! IT IS A DISASTER! WE ARE "
;;        (if (= severity :mild)
;;          "MILDY INCONVECIENCED!"
;;          "DOOOOOMED")))

;; (when true
;;   (println "Thamirys is my pretty wife!")
;;   "She is software engineer")

;; (defn error-message2
;;   [severity]
;;   (str "Oh god! It is a disaster! We are "
;;        (if (= severity :mild)
;;          "MILDY INCONVECIENCED!"
;;          "DOOOOOMED!")))

;; (defn greeting
;;   [name]
;;   (str "teste" name))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println dt/my-string)
  (println dt/my-integer)
  (println dt/my-float)
  (println dt/my-boolean)
  (println dt/my-keyword)
  (println dt/my-symbol)
  (println dt/my-list)
  (println dt/my-vector)
  (println dt/my-map)
  (println dt/my-set)
  (println dt/my-nil)
  (fmt/format_with_println)
  (cf/is_maria_mother_of_jesus)
  (cf/what_is_your_name)
  (cf/what_is_your_name "Thams")
  (println (cf/classify-age 22))
  (cf/can-withdraw "Yuri" 90 90)
  (println (cf/classifyBalance 90000))
  (println (pm/classify-transaction :deposit  98790))
  (cf/can-withdraw "Yuri Melo" -200 1000)
)
