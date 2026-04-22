(ns clojure-noob.learning.control-flow)

(defn is_maria_mother_of_jesus []
    (if true
        (println "Maria is Jesus's mother")
        (println "Maria is not Jesus's mother??"))
    (if false
        (println "Fatima is arabic name")
        (println "Fatima is not arabic name")))

(defn what_is_your_role
    ([] (what_is_your_role "Dev"))
    ([role] role))

(defn what_is_your_name
    ([]    (what_is_your_name "Yuri Melo"))
    ([msg] (println "My name is" msg (what_is_your_role "Engineer"))))

;; (if true
;;   (do (println "Clojure is fun")
;;       (println "Another text")
;;       (println "By Stackoverflow survey"))
;;   (do (println "This is false")
;;       (println "By batman")))

(defn classify-age [age]
    (cond
        (< age 13) "child"
        (< age 18) "teen"
        (< age 65) "adult"
        :else "senior"))

(defn describe-role [role]
    (case role
        :admin "Full access"
        :editor "Can edit"
        :viewer "Read only"
        "Unknow role"))

;; Diferença do Haskell: case em Clojure compara por igualdade de valor, não destrói estruturas. Para pattern matching estrutural, existe a lib core.match.

(defn classify-score [score]
    (condp <= score
        90 "A"
        75 "B"
        60 "C"
        "F"))

;; -> passa o resultado como primeiro argumento:
;; => ["YURI" "MELO"]
(-> " yuri melo "
    clojure.string/trim
    clojure.string/upper-case
    (clojure.string/split #" "))
