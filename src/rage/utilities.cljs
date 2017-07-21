(ns rage.utilities
  (:require [cljs.js :as c]))


;; (cljs.js/analyze-str (cljs.js/empty-state) "(+ 2 (* 5 5))" nil {:eval cljs.js/js-eval :context :expr} identity)

(defn ast [code-str]
  (c/analyze-str
    (c/empty-state)
    code-str
    nil
    {:eval c/js-eval :context :expr}
    identity))
