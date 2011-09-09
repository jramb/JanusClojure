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

(events/listen
    (dom/getElement "getUUID")
    event-type/CLICK
    fetch-clicked)

(comment
; HOWTO compile:
; In NORMAL ClojureScript REPL: script/repl
; perform this require once
  (require '[cljs.closure :as cljsc])
;
; then for recompiling the cljs file call this:
  (cljsc/build "janusclojure.cljs" {:optimizations :advanced :pretty-print false :output-to "janusclojure.js"})
)
