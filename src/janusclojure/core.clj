(ns janusclojure.core
    (:require [ring.adapter.jetty :as jetty])
    (:require [compojure.core :as compojure])
    (:require [compojure.route :as route])
    (:require [janusclojure.wrappers :as mywraps])
    )

(def START-TIME (.getTime (java.util.Date.)))

(defn gen-uuid
  "Returns a new UUID without the dashes. Take good care of it, it is unique!"
  []
  (apply str (re-seq #"[^-]" (str (java.util.UUID/randomUUID)))))

(defn redirect-to
  "Generates a redirect response"
  [loc]
  {:status 302
   :headers {"Location" loc}})

(defn get-uuid []
  ;(Thread/sleep 1000)
  {:body {:uuid (gen-uuid)}})

(compojure/defroutes app-routes
    (compojure/GET   "/"      _   (redirect-to "/index.html"))
    (compojure/GET   "/hello" _   "Hello, world!")
    (compojure/GET   "/uuidold"  _ (str {:uuid (gen-uuid)}))
    (compojure/GET   "/uuid2"  _ (str (get-uuid)))
    (->
      (compojure/routes
        (compojure/GET   "/uuid"  _ (get-uuid))
        )
      mywraps/wrap-clojure-simple
      mywraps/wrap-timer)
    (compojure/GET   "/uuid3"  _ (str (get-uuid)))
    (route/resources "/" {:root "html"})
    (route/not-found "Page not found")
    )

(defn -main []
    (let [port (Integer/parseInt (get (System/getenv) "PORT" "8080"))]
        (jetty/run-jetty (var app-routes) {:port port})))
