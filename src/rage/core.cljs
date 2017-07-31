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
    "Type some ClojureScript into the box below. Press the button to view the abstract syntax tree."])

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

(defn update-view []
  (let [select (.getElementById js/document "select-view")
        raw (.getElementById js/document "raw-view")
        json (.getElementById js/document "json-view")
        canvas (.getElementById js/document "canvas-view")]
    (doseq [i [raw json canvas]] (set! (-> i .-style .-display) "none"))
    (->> (condp = (.-value select) "Raw" raw "JSON" json "Canvas" canvas)
         (#(set! (-> % .-style .-display) "block")))))

(defn on-submit []
  (let [ast-raw (ast @source-code)]
    (.jsonViewer (js/jQuery "#json-view") (clj-to-js ast-raw))
    (reset! ast-text (with-out-str (pp/pprint ast-raw)))
    (reset! expanded-code (expand-str @source-code))
    (update-view)))

(defn choose-output-format []
  [:div#choose-output-format
    [:select#select-view {:on-change update-view}
      [:option {:value "Raw"} "Raw"]
      [:option {:value "JSON"} "JSON"]
      [:option {:value "Canvas"} "Canvas"]]])

(defn submit-bar []
  [:div#submit-bar
    [:input {:type "submit"
             :on-click on-submit}]
    [choose-output-format]
    [:div#github-link>a {:href "https://github.com/lincoln-b/rage"}
      "view the source for this page"]])

(defn ast-output-area []
  [:div#ast-output
    [:div#json-view]
    [:pre#raw-view @ast-text]
    [:div#canvas-view "coming soon!"]])

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
