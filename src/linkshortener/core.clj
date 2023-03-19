(ns linkshortener.core
  (:require [ring.adapter.jetty :as ring-jetty]
            [reitit.ring :as ring]
            [ring.util.response :as r]
            [muuntaja.core :as m]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [linkshortener.db :as db]
            [linkshortener.slug :refer [generate-slug]]))

;; redirect using slug
(defn redirect [req]
  (let [slug (get-in req [:path-params :slug])
        url (db/get-url slug)]
    (if url
      (r/redirect url 307)
      (r/not-found "Not Found"))))

;; create a new redirect in the database
(defn create-redirect [req]
  (let [url (get-in req [:body-params :url])
        slug (generate-slug)]
    (db/insert-redirect! slug url)
    (r/response (str "created slug " slug))))

;; create the app and defines the routes
(def app
  (ring/ring-handler
   (ring/router
    ["/"
     [":slug/" redirect]
     ["api/"
      ["redirect/" {:post create-redirect}]]
     ["" {:handler (fn [req] {:body "Create Redirect" :status 200})}]]
    {:data {:muuntaja m/instance
            :middleware [muuntaja/format-middleware]}})))

;; starts the jetty server
(defn start []
  (ring-jetty/run-jetty #'app {:port 8888
                               :join? false}))

(def -main (start))