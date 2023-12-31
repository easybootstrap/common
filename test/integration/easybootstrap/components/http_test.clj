(ns integration.easybootstrap.components.http-test
  (:require [com.stuartsierra.component :as component]
            [easybootstrap.components.clj-http :as clj-http]
            [easybootstrap.components.config :as config]
            [easybootstrap.components.server :as component.server]
            [easybootstrap.test.state-flow.http :as state-flow.http]
            [integration.easybootstrap.util :as util]
            [state-flow.api :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.core :as state-flow :refer [flow]]))

(defflow
  flow-integration-database-test
  {:init (util/start-system!
          {:config (config/new-config)
           :http (clj-http/new-http-mock {"https://duckduckgo.com" {:status 200}})
           :webserver (component/using (component.server/new-webserver) [:config :http])})
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "start a system with http mock and update/check http-out-responses"
    (flow "do request in existing configured mock response"
      (match? {:status 200}
              (state-flow.http/request! {:method :get
                                         :url "https://duckduckgo.com"})))
    (flow "do request in non-existing configured mock response"
      (match? {:status 500}
              (state-flow.http/request! {:method :get
                                         :url "https://goosegoosego.com"})))

    (state-flow.http/set-http-out-responses! {"https://goosegoosego.com"
                                              {:body {:msg "quack?"}
                                               :status 200}})

    (flow "do request in new existing configured mock response"
      (match? {:status 200
               :body {:msg "quack?"}}
              (state-flow.http/request! {:method :get
                                         :url "https://goosegoosego.com"})))

    (flow "get all out request into mock component"
      (match? [{:method :get
                :url "https://duckduckgo.com"
                :instant int?}
               {:method :get
                :url "https://goosegoosego.com"
                :instant int?}
               {:method :get
                :url "https://goosegoosego.com"
                :instant int?}]
              (state-flow.http/http-out-requests)))

    (flow "get out request into mock component using filter function"
      (match? [{:method :get
                :url "https://duckduckgo.com"
                :instant int?}]
              (state-flow.http/http-out-requests
               #(filter (fn [req] (= (:url req) "https://duckduckgo.com")) %))))))
