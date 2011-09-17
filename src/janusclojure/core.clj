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

(defn get-uuid [req]
  ;(Thread/sleep 1000)
  {:body {:uuid (gen-uuid)}})

(defn process-info [req]
  (let [{:keys [your-name your-number random]} (:body req)]
    (format "Hi, %s! You sent me the number %d and also '%s'."
           your-name your-number random)))

(compojure/defroutes app-routes
    (compojure/GET   "/"      _   (redirect-to "/index.html"))
    (compojure/GET   "/hello" _   "Hello, world!")
    (->
      (compojure/routes
        (compojure/GET   "/uuid"      req (get-uuid req))
        (compojure/POST  "/echo"      req req)  ; simple echo service!
        ;(compojure/POST  "/backdoor"  req {:body (eval (:body req))}) ; do not enable this!! NOT!
        (compojure/POST  "/process"   req (process-info req))
        )
      mywraps/wrap-clojure-simple
      mywraps/wrap-timer)
    (route/resources "/" {:root "html"})
    (route/not-found "Page not found")
    )

(defn -main []
    (let [port (Integer/parseInt (get (System/getenv) "PORT" "8080"))]
        (jetty/run-jetty (var app-routes) {:port port})))
