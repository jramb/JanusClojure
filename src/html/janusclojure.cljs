(ns janusclojure
  (:require
    [cljs.reader :as reader]
    [goog.dom :as dom]
    [goog.net.XhrIo :as ajax2]
    [goog.events :as events]
    [goog.events.EventType :as event-type]
  ))

(defn log [str]
    (js* "console.log(~{str})"))

(def random-text-input
    (dom/getElement "random-text"))

(defn fetch-clicked [e]
   (goog.net.XhrIo/send
       "/uuid"
       (fn [e]
         (let [xhr (.target e)
               respTxt (. xhr (getResponseText))
               data (reader/read-string respTxt)
               ]
           ;(log respTxt)
           (set! (.value random-text-input) (:uuid data))))))

(defn receive-result [e]
    (.setText (dom/getElement "info")))

(defn send-button-clicked [e]
    (let [your-name (dom/getElement "your-name")
          your-number (dom/getElement "your-number")
          name (.value your-name)
          num  (.value your-number)]
          (goog.net.XhrIo/send
            "/process"
            receive-result
            ; FIXME body
            )
          ))


(events/listen
    (dom/getElement "getUUID")
    event-type/CLICK
    fetch-clicked)

(events/listen
    (dom/getElement "sendBtn")
    event-type/CLICK
    send-button-clicked)


(comment
; HOWTO compile:
; In NORMAL ClojureScript REPL: script/repl
; perform this require once
  (require '[cljs.closure :as cljsc])
;
; then for recompiling the cljs file call this:
  (cljsc/build "janusclojure.cljs" {:optimizations :advanced :pretty-print false :output-to "janusclojure.js"})
)
