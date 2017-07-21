(ns rage.core
  (:require
    [reagent.core :as reagent :refer [atom]]
    [rage.utilities :refer [ast]]
    [clojure.pprint :as pp]))

(enable-console-print!)

(def source-code (atom ""))

(defn code-input-area []
  [:textarea#code-input
    {:on-change #(reset! source-code (-> % .-target .-value))}])

(defn ast-output-area []
  [:div#ast-output>pre
    (with-out-str (pp/pprint (ast @source-code)))])

(defn app []
  [:div#container
    [code-input-area]
    [ast-output-area]])

(reagent/render-component [app]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
