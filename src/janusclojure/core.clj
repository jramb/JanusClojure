(ns janusclojure.core
    (:require [ring.adapter.jetty :as jetty])
    (:require [compojure.core :as compojure])
    (:require [compojure.route :as route])
    )

(defn gen-uuid
  "Returns a new UUID without the dashes. Take good care of it, it is unique!"
  []
  (apply str (re-seq #"[^-]" (str (java.util.UUID/randomUUID)))))

(defn redirect-to
  "Generates a redirect response"
  [loc]
  {:status 302
   :headers {"Location" loc}})

(defn read-body
    "Reads the body (of the request), returns a clojure data structure."
    [body]
    (let [b (slurp body)]
        (when-not (empty? b)
           (read-string b))))

(defn wrap-clojure
   "Ring wrapper to decode body from and encode response to serialized Clojure"
   [handler]
   (fn [req]
       (let [body (read-body (:body req))
             resp (handler (assoc req :body body))]
         (if resp
           (assoc resp :body (str (:body resp)))))))

(compojure/defroutes app-routes
    (compojure/GET   "/"      _   (redirect-to "/index.html"))
    (compojure/GET   "/hello" _   "Hello, world!")
    (compojure/GET   "/uuid"  _   (str {:uuid (gen-uuid)}))
    (route/resources "/" {:root "html"})
    (route/not-found "Page not found")
    )

(defn -main []
    (let [port (Integer/parseInt (get (System/getenv) "PORT" "8080"))]
        (jetty/run-jetty (var app-routes) {:port port})))
