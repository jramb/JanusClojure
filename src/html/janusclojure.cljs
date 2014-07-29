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


(defn data-from-result [e]
    (let [xhr (.target e)
          respTxt (. xhr (getResponseText))]
      (log respTxt)
      (reader/read-string respTxt)))

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
   (dom/appendChild
     (dom/getElement "info")
     (dom/createDom "pre" nil (get (data-from-result e) :result))))


(defn send-button-clicked [e]
    (let [name (.value (dom/getElement "your-name"))
          num  (.value (dom/getElement "your-number"))
          random (.value random-text-input)]
          (goog.net.XhrIo/send
            "/process"
            receive-result
            "POST" ; default would have been "GET"
            (pr-str
                {:your-name name :your-number (int num) :random random}))))


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
