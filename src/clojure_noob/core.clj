(ns clojure-noob.core
  (:gen-class))

(if true
  (println "Maria is Jesus's mother")
  (println "Maria is not Jesus's mother"))

(if false
  (println "Fatima is arabic name")
  (println "Fatima is not arabic name"))

(if true
  (do (println "Clojure is fun")
      (println "Another text")
      "By Stackoverflow survey")
  (do (println "This is false")
      "By batman"))

(when true
  (println "Thamirys is my pretty wife!")
  "She is software engineer")

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
