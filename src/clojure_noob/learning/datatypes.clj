(ns clojure-noob.learning.datatypes)

;; Clojure has a rich set of data types. Here are some of the most commonly used ones:
;; 1. Numbers: Clojure supports various numeric types, including integers, floating-point numbers, and ratios.
(def my-integer 42)
(def my-float 3.14)
;; 2. Strings: Strings are sequences of characters enclosed in double quotes.
(def my-string "Hello, Clojure!")
;; 3. Booleans: Clojure has two boolean values: true and false.
(def my-boolean true)
;; 4. Keywords: Keywords are used as identifiers and are prefixed with a colon.
(def my-keyword :my-key)
;; 5. Symbols: Symbols are used to represent variables and functions.
(def my-symbol 'my-symbol)
;; 6. Lists: Lists are ordered collections of items enclosed in parentheses.
(def my-list '(1 2 3 4 5))
;; 7. Vectors: Vectors are similar to lists but are enclosed in square brackets and provide efficient random access.
(def my-vector [1 2 3 4 5])
;; 8. Maps: Maps are collections of key-value pairs enclosed in curly braces.
(def my-map {:name "Alice" :age 30 :city "New York"})
;; 9. Sets: Sets are collections of unique items enclosed in curly braces with a hash symbol.
(def my-set #{1 2 3 4 5})
;; 10. Nil: Clojure uses nil to represent the absence of a value.
(def my-nil nil)

;; Some function that you can use to manipulate these data types:

;; 1. `str`: Concatenates strings and other values into a single string.
(def my-concatenated-string (str "Hello, " "Clojure!"))
;; 2. `inc` and `dec`: Increment and decrement numbers.
(def my-incremented-number (inc my-integer))
(def my-decremented-number (dec my-integer))
;; 3. `conj`: Adds an element to a collection (like a list, vector, or set).
(def my-new-list (conj my-list 6))
(def my-new-vector (conj my-vector 6))
(def my-new-set (conj my-set 6))
(def my-new-map (conj my-map {:country "USA"}))
;; 4. `get`: Retrieves a value from a map using a key.
(def my-name (get my-map :name))
;; 5. `assoc`: Associates a new key-value pair in a map.
(def my-updated-map (assoc my-map :age 31))
;; 6. `dissoc`: Removes a key-value pair from a map.
(def my-reduced-map (dissoc my-map :city))
;; 7. `first`, `second`, `nth`: Access elements in a collection.
(def my-first-element (first my-list))
(def my-second-element (second my-list))
(def my-third-element (nth my-list 2))
;; 8. `count`: Counts the number of elements in a collection.
(def my-list-count (count my-list))
;; 9. `empty?`: Checks if a collection is empty.
(def my-is-empty (empty? my-list))
;; 10. `map`: Applies a function to each element in a collection.
(def my-squared-list (map #(* % %) my-list))
;; 11. `filter`: Filters elements in a collection based on a predicate function.
(def my-even-list (filter even? my-list))
;; 12. `reduce`: Reduces a collection to a single value using a binary function.
(def my-sum (reduce + my-list))
;; 13. `contains?`: Checks if a collection contains a specific element.
(def my-contains-two (contains? my-set 2))
;; 14. `keys` and `vals`: Retrieves the keys and values from a map.
(def my-map-keys (keys my-map))
(def my-map-values (vals my-map))
;; 15. `merge`: Merges two maps together.
(def my-merged-map (merge my-map {:country "USA" :age 31}))
;; 16. `distinct`: Returns a sequence of unique elements from a collection.
(def my-distinct-list (distinct [1 2 2 3 3 3 4 5]))
;; 17. `sort`: Sorts a collection of elements.
(def my-sorted-list (sort [5 3 1 4 2]))
;; 18. `reverse`: Reverses the order of elements in a collection.
(def my-reversed-list (reverse my-list))
;; 19. `take` and `drop`: Takes or drops a specified number of elements from a collection.
(def my-taken-list (take 3 my-list))
(def my-dropped-list (drop 3 my-list))
;; 20. `partition`: Partitions a collection into subcollections of a specified size.
(def my-partitioned-list (partition 2 my-list))
;; 21. `group-by`: Groups elements in a collection based on a specified function.
(def my-grouped-list (group-by even? my-list))
;; 22. `frequencies`: Counts the frequency of each element in a collection.
(def my-frequencies (frequencies [1 2 2 3 3 3 4 5]))
;; 23. `zipmap`: Creates a map from two collections, one of keys and
;;    one of values.
(def my-zipped-map (zipmap [:a :b :c] [1 2 3]))
;; 24. `partition-by`: Partitions a collection into subcollections based on a specified
;;    function.
(def my-partitioned-by-list (partition-by even? my-list))
;; 25. `group-by`: Groups elements in a collection based on a specified function.
(def my-grouped-by-list (group-by even? my-list))
;; 26. `frequencies`: Counts the frequency of each element in a collection.
(def my-frequencies-list (frequencies [1 2 2 3 3 3 4 5]))
;; 27. `zipmap`: Creates a map from two collections, one of keys and one of values.
(def my-zipped-map-list (zipmap [:a :b :c] [1 2 3]))
;; 28. `interleave`: Interleaves two or more collections together.
(def my-interleaved-list (interleave [1 2 3] [:a :b :c]))
;; 29. `pos?` and `neg?`: Checks if a number is positive or negative.
(def my-is-positive (pos? my-integer))
(def my-is-negative (neg? my-integer))
;; 30. `zero?`: Checks if a number is zero.
(def my-is-zero (zero? my-integer))
;; 31. `even?` and `odd?`: Checks if a number is even
(def my-is-even (even? my-integer))
(def my-is-odd (odd? my-integer))
