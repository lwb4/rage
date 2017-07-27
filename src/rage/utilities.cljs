(ns rage.utilities
  (:require [cljs.reader :as reader]
            [cljs.js :as c]
            [cljs.analyzer :as ana]))

(declare clj-to-js)

(defn key-to-js [k]
  "Helper function for clj-to-js."
  (if (satisfies? IEncodeJS k)
    (-clj->js k)
    (if (or (string? k)
            (number? k)
            (keyword? k)
            (symbol? k))
      (clj-to-js k)
      (pr-str k))))

(defn clj-to-js
  "Exactly like clj->js, but use str instead of name on keywords."
   [x]
   (when-not (nil? x)
     (if (satisfies? IEncodeJS x)
       (-clj->js x)
       (cond
         (keyword? x) (str x)
         (symbol? x) (str x)
         (map? x) (let [m (js-obj)]
                    (doseq [[k v] x]
                      (aset m (key-to-js k) (clj-to-js v)))
                    m)
         (coll? x) (let [arr (array)]
                     (doseq [x (map clj->js x)]
                       (.push arr x))
                     arr)
         :else x))))

;; copied from https://github.com/clojure/clojurescript/blob/master/src/main/clojure/cljs/analyzer/utils.clj#L12-L22

(defn simplify-env [_ {:keys [op] :as ast}]
  (let [env (:env ast)
        ast (if (= op :fn)
              (assoc ast :methods
                (map #(simplify-env nil %) (:methods ast)))
              ast)]
    (assoc (dissoc ast :env)
      :env {:context (:context env)})))

(defn elide-children [_ ast]
  (dissoc ast :children))

(defn to-ast
  ([form] (to-ast 'cljs.user form))
  ([ns form]
    (let [env (assoc-in (ana/empty-env) [:ns :name] ns)]
      (binding [ana/*passes*
                (or ana/*passes*
                  [elide-children simplify-env ana/infer-type])]
        (ana/analyze env form)))))

(defn ast [code-str]
  (try
    (clj-to-js (to-ast (reader/read-string code-str)))
    (catch js/Error e (str e))))
