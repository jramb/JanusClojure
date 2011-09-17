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
   (log "Hejho, hoppsan!")
   #_(log (:result (data-from-result e)))
   (dom/appendChild
     (dom/getElement "info")
     (dom/createDom "pre" nil (get (data-from-result e) :result)))
   (log "Det var allt"))


(defn send-button-clicked [e]
    (let [your-name (dom/getElement "your-name")
          your-number (dom/getElement "your-number")
          name (.value your-name)
          num  (.value your-number)
          random (.value random-text-input)]
          (goog.net.XhrIo/send
            "/process"
            receive-result
            "POST" ; defaults to "GET"
            (pr-str
                {:your-name name :your-number 123 :random random})
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
