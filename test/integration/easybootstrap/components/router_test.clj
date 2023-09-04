(ns easybootstrap.components.router-test
  (:require [clojure.test :refer [use-fixtures]]
            [com.stuartsierra.component :as component]
            [easybootstrap.components.clj-http :as clj-http]
            [easybootstrap.components.config :as config]
            [easybootstrap.components.database :as database]
            [easybootstrap.components.router :as router]
            [easybootstrap.components.server :as component.server]
            [easybootstrap.helpers.malli :as helpers.malli]
            [easybootstrap.test.state-flow.server :as state-flow.server]
            [integration.easybootstrap.util :as util]
            [state-flow.api :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.core :as state-flow :refer [flow]]))

(use-fixtures :once helpers.malli/with-intrumentation)

(def test-routes
  [["/plus"
    {:get {:summary "plus with spec query parameters"
           :parameters {:query [:map
                                [:x :int]
                                [:y :int]]}
           :responses {200 {:body [:map [:total :int]]}}
           :handler (fn [{{{:keys [x y]} :query} :parameters}]
                      {:status 200
                       :body {:total (+ x y)}})}
     :post {:summary "plus with spec body parameters"
            :parameters {:body [:map
                                [:x :int]
                                [:y :int]]}
            :responses {200 {:body [:map [:total :int]]}}
            :handler (fn [{{{:keys [x y]} :body} :parameters}]
                       {:status 200
                        :body {:total (+ x y)}})}}]])

(defflow
  flow-integration-webserver-test
  {:init (util/start-system!
          {:config (config/new-config)
           :http (clj-http/new-http-mock {})
           :database (component/using (database/new-database) [:config])
           :router (router/new-router test-routes)
           :webserver (component/using (component.server/new-webserver)
                                       [:config :http :router :database])})
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact test-routes"
    (flow "should sum the get params x & y via get"
      (match? {:status 200
               :body {:total 7}}
              (state-flow.server/request! {:method  :get
                                           :uri     (str "/plus?x=" 3 "&y=" 4)})))
    (flow "should sum the body x & y via post"
      (match? {:status 200
               :body {:total 7}}
              (state-flow.server/request! {:method  :post
                                           :uri     "/plus"
                                           :body    {:x 4
                                                     :y 3}})))))
