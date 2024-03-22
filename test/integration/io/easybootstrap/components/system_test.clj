(ns io.easybootstrap.components.system-test
  (:require [clojure.test :refer [use-fixtures]]
            [com.stuartsierra.component :as component]
            [io.easybootstrap.components.clj-http :as clj-http]
            [io.easybootstrap.components.config :as config]
            [io.easybootstrap.components.database :as database]
            [io.easybootstrap.components.router :as router]
            [io.easybootstrap.components.server :as component.server]
            [io.easybootstrap.helpers.malli :as helpers.malli]
            [io.easybootstrap.aux.db :as state-flow.db]
            [io.easybootstrap.aux.http :as state-flow.http]
            [io.easybootstrap.aux.server :as state-flow.server]
            [integration.io.easybootstrap.utils :as util]
            [state-flow.api :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.core :as state-flow :refer [flow]]))

(use-fixtures :once helpers.malli/with-intrumentation)

(defn do-deposit!
  [{{{:keys [btc]} :body} :parameters
    {:keys [http database]} :components}]
  (let [response (clj-http/request http {:url "http://coinbase.org" :method :get})
        rate (get-in response [:body :rate])
        price (* rate btc)]
    (database/execute database [(str "insert into wallet(price) values('" price "')")])
    {:status 201
     :body {:usd price}}))

(defn get-wallet
  [{{:keys [database]} :components}]
  (let [wallet (database/execute database ["select * from wallet"])]
    {:status 200
     :body (map (fn [{:wallet/keys [id price]}]
                  {:id id
                   :amount price})
                wallet)}))

(def test-routes
  [["/wallet"
    {:swagger {:tags ["wallet"]}}

    ["/deposit"
     {:post {:summary "deposit btc and return value in usd"
             :parameters {:body [:map [:btc :double]]}
             :responses {201 {:body [:map [:usd :double]]}}
             :handler do-deposit!}}]

    ["/list"
     {:get {:summary "list deposits in wallet"
            :responses {200 {:body [:vector [:map [:id :int] [:amount decimal?]]]}}
            :handler get-wallet}}]]])

(defflow
  flow-integration-malli-system-test
  {:init (util/start-system!
          {:config (config/new-config)
           :http (clj-http/new-http-mock {})
           :database (component/using (database/new-database) [:config])
           :router (router/new-router test-routes)
           :webserver (component/using (component.server/new-webserver)
                                       [:config :http :router :database])})
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system"

    (flow "prepare system with http-out mocks and creating tables"
      (state-flow.http/set-http-out-responses! {"http://coinbase.org" {:body {:rate 35000.0M}
                                                                       :status 200}})

      (state-flow.db/execute! ["create table if not exists wallet (
                                  id serial primary key,
                                  price decimal)"])

      (flow "should insert deposit into wallet"
        (match? {:status 201
                 :body {:usd 70000.0}}
                (state-flow.server/request! {:method :post
                                             :uri    "/wallet/deposit"
                                             :body   {:btc 2M}})))

      (flow "should list wallet deposits"
        (match? {:status 200
                 :body [{:id 1
                         :amount 70000.0}]}
                (state-flow.server/request! {:method :get
                                             :uri    "/wallet/list"}))))))
