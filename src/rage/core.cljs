(ns rage.core
  (:require
    [reagent.core :as reagent :refer [atom]]
    [rage.utilities :refer [ast to-ast]]
    [clojure.pprint :as pp]))

(enable-console-print!)

(def source-code (atom ""))

(defn information-bar []
  [:div#information-bar
    "Type some ClojureScript into the box below. Press Submit to see the Abstract Syntax Tree."])

(defn editor-did-mount []
  (fn [this]
    (let
      [cm
        (.fromTextArea js/CodeMirror
          (reagent/dom-node this)
          #js {:mode "clojure"
               :lineNumbers true
               :tabSize 2})]
      (.on cm "change" #(reset! source-code (.getValue %))))))

(defn code-input-area []
  (reagent/create-class
    {:render (fn [] [:textarea#code-input])
     :component-did-mount (editor-did-mount)}))

(defn submit-bar []
  [:div#submit-bar
    [:input {:type "submit"
             :on-click #(.jsonViewer (js/jQuery "#ast-output") (clj->js (ast @source-code)))}]
    [:div#github-link>a {:href "https://github.com/lincoln-b/rage"} "view the source for this page"]])

(defn ast-output-area []
  [:div#ast-output])

(defn left-pane []
  [:div#left-pane
    [information-bar]
    [code-input-area]])

(defn right-pane []
  [:div#right-pane
    [submit-bar]
    [ast-output-area]])

(defn app []
  [:div#container
    [left-pane]
    [right-pane]])

(reagent/render-component [app]
  (. js/document (getElementById "app")))
