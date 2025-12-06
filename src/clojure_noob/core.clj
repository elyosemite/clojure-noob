(ns clojure-noob.core
  (:gen-class))

(if true
  (do (println "This is true")
      "By aquaman")
  (do (println "This is false")
      "By batman"))

(when true
  (println "Thamirys is my pretty wife!")
  "She is software engineer")

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
