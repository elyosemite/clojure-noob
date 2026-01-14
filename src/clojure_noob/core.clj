(ns clojure-noob.core
  (:gen-class))

(if true
  (println "Maria is Jesus's mother")
  (println "Maria is not Jesus's mother??"))

(if false
  (println "Fatima is arabic name")
  (println "Fatima is not arabic name"))

(if true
  (do (println "Clojure is fun")
      (println "Another text")
      (println "By Stackoverflow survey"))
  (do (println "This is false")
      (println "By batman")))

(and :feeling_super_cool nil false)

(def severity :mild)

; Functions
(defn error-message
  [severity]
  (str "Oh GOD! IT IS A DESASTER! WE ARE "
       (if (= severity :mild)
         "MILDY INCONVECIENCED!"
         "DOOOOOMED")))

(when true
  (println "Thamirys is my pretty wife!")
  "She is software engineer")

(defn error-message2
  [severity]
  (str "Oh god! It is a disaster! We are "
       (if (= severity :mild)
         "MILDY INCONVECIENCED!"
         "DOOOOOMED!")))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (println error-message severity))
(println (error-message2 severity))
(println (error-message2 :mild))
