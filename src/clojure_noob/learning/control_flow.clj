(ns clojure-noob.learning.control-flow)

;  As expressões de fluxo em Clojure
;
;  ┌────────────────────┬──────────────────────────┬──────────────────────────────────────┐
;  │      Clojure       │   Haskell equivalente    │             Quando usar              │
;  ├────────────────────┼──────────────────────────┼──────────────────────────────────────┤
;  │ if                 │ if/then/else             │ Uma condição, dois ramos             │
;  ├────────────────────┼──────────────────────────┼──────────────────────────────────────┤
;  │ when               │ if sem else              │ Uma condição, sem ramo negativo      │
;  ├────────────────────┼──────────────────────────┼──────────────────────────────────────┤
;  │ cond               │ guards (|)               │ Múltiplas condições arbitrárias      │
;  ├────────────────────┼──────────────────────────┼──────────────────────────────────────┤
;  │ case               │ case em literais         │ Match em valores constantes (rápido) │
;  ├────────────────────┼──────────────────────────┼──────────────────────────────────────┤
;  │ condp              │ —                        │ cond com um predicado fixo           │
;  ├────────────────────┼──────────────────────────┼──────────────────────────────────────┤
;  │ match (core.match) │ case com desestruturação │ Match estrutural em qualquer forma   │
;  └────────────────────┴──────────────────────────┴──────────────────────────────────────┘

; If
(defn is_maria_mother_of_jesus []
    (if true
        (println "Maria is Jesus's mother")
        (println "Maria is not Jesus's mother??"))
    (if false
        (println "Fatima is arabic name")
        (println "Fatima is not arabic name")))

; recursion
(defn what_is_your_role
    ([] (what_is_your_role "Dev"))
    ([role] role))

(defn what_is_your_name
    ([]    (what_is_your_name "Yuri Melo"))
    ([msg] (println "My name is" msg (what_is_your_role "Engineer"))))

; Cond
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

; When
(defn can-withdraw [name amount limit]
    (when (<= amount limit)
        (println "Sr." name ", you can withdraw!")))

; Cond
(defn classifyBalance [balance]
    (cond
        (<= balance 0)   "Negative"
        (< balance 1000) "Low"
        :else            "Healthy"))

;; -> passa o resultado como primeiro argumento:
;; => ["YURI" "MELO"]
(-> " yuri melo "
    clojure.string/trim
    clojure.string/upper-case
    (clojure.string/split #" "))
