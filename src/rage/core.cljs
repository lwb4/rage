(ns rage.core
  (:require
    [reagent.core :as reagent :refer [atom]]
    [rage.utilities :refer [ast to-ast clj-to-js expand-str]]
    [clojure.pprint :as pp]
    [cljs.js :as cljs]))

(enable-console-print!)

(def source-code (atom ""))
(def ast-text (atom ""))
(def expanded-code (atom ""))

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
               :tabSize 2
               :autofocus true})]
      (.on cm "change" #(reset! source-code (.getValue %))))))

(defn code-input-area []
  (reagent/create-class
    {:render (fn [] [:textarea#code-input])
     :component-did-mount (editor-did-mount)}))

(defn expanded-code-area []
  [:div#expanded-code-area
    [:div "The macroexpanded code will be shown in this box."]
    [:pre @expanded-code]])

(defn on-submit []
  (let [ast-raw (ast @source-code)]
    (.jsonViewer (js/jQuery "#ast-output") (clj-to-js ast-raw))
    (reset! ast-text (with-out-str (pp/pprint ast-raw)))
    (reset! expanded-code (expand-str @source-code))))

(defn submit-bar []
  [:div#submit-bar
    [:input {:type "submit"
             :on-click on-submit}]
    [:div#github-link>a {:href "https://github.com/lincoln-b/rage"} "view the source for this page"]])

(defn ast-output-area []
  [:div#ast-output])

(defn left-pane []
  [:div#left-pane
    [information-bar]
    [code-input-area]
    [expanded-code-area]])

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
