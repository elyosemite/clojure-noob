(ns clojure-noob.core
  (:gen-class))

(if true
  (do (println "This is true")
      "By aquaman")
  (do (println "This is false")
      "By batman")
)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
