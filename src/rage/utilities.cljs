(ns rage.utilities
  (:require [cljs.reader :as reader]
            [cljs.js :as c]
            [cljs.analyzer :as ana]))

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
    (clj->js (to-ast (reader/read-string code-str)))
    (catch js/Error e (str e))))

; (defn ast [code-str]
;   (c/analyze-str
;     (c/empty-state)
;     code-str
;     nil
;     {:eval c/js-eval :context :expr}
;     identity))
