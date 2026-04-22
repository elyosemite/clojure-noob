(ns clojure-noob.learning.control-flow)

(defn is_maria_mother_of_jesus []
    (if true
        (println "Maria is Jesus's mother")
        (println "Maria is not Jesus's mother??"))
    (if false
        (println "Fatima is arabic name")
        (println "Fatima is not arabic name")))

;; (if true
;;   (do (println "Clojure is fun")
;;       (println "Another text")
;;       (println "By Stackoverflow survey"))
;;   (do (println "This is false")
;;       (println "By batman")))
