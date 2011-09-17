(ns janusclojure.wrappers
  )

; little helper to implement a call counter
(let [i (atom 1)]
    (defn get-call-counter
        []
        @i)
    (defn inc-call-counter
          "Returns an incremeted call counter for each call."
          []
          (swap! i inc)))

(defn wrap-timer [handler]
  "A ring middleware: adds an execution timer."
  (fn [req]
    (let [start (. System (nanoTime))
          resp  (handler req)]
      (if resp
        (let [time-ms (int (/ (double (- (. System (nanoTime)) start)) 1000000.0))
              resp (assoc-in resp [:headers "X-execution-time-ms"] (str time-ms))
              resp (assoc-in resp [:headers "X-call-counter"] (str (inc-call-counter)))
              ]
          resp)))))


(defn read-body
    "Reads the body (of the request), returns a clojure data structure."
    [body]
    (let [b (slurp body)]
        (when-not (empty? b)
           (read-string b))))

(defn wrap-clojure-simple [handler]
  "Ring middleware: automatically decode/encode clojure.
  Simplified version, skips the header handling (Content-Type, etc)
  Reads :body to Clojure.
  If the body of the response is automatically converted again to Clojure."
  (fn [req]
    (let [;removed for simplicity;req-type (:content-type req)
          decoded-body (read-body (:body req))
          resp (handler (assoc req :body decoded-body))]
      (when resp
        (assoc resp
          :body (str (:body resp))
          :content-type "application/clojure")))))


