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

 (def severity :mild)
 (def error-message "OH MY GOSH! IT'S A DISASTER! WE ARE ")
 (if (= severity :mild)
   (def error-message (str error-message "MILDLY INCONVENIENCED!"))
   (def error-message (str error-message "DOOOOOMED!")))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (println error-message :mild))
