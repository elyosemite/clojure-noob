(ns clojure-noob.learning.format)

(def aString "Yuri Melo")
(def aDouble 1.6543)
(def aLong 15)

(defn format_with_println []
  (println "\nYou are using the format namespace function")
  (println (format "This is a string %s" aString))
  (println (format "5 spaces and %5d" aLong))
  (println (format "Leading zeros %010d" aLong))
  (println (format "%-4d left justified" aLong))
  (println (format "3 decimals %.3f" aDouble)))

(defn format_with_printf []
  (println "You are using the format namespace function")
  (printf "This is a string %s" aString)
  (printf "5 spaces and %5d" aLong)
  (printf "Leading zeros %04d" aLong)
  (printf "%-4d left justified" aLong)
  (printf "3 decimals %.3f" aDouble))
