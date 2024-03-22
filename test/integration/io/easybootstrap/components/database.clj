(ns io.easybootstrap.components.database
  (:require [com.stuartsierra.component :as component]
            [integration.io.easybootstrap.utils :as util]
            [io.easybootstrap.aux.db :as state-flow.db]
            [io.easybootstrap.components.config :as config]
            [io.easybootstrap.components.database :as database]
            [state-flow.api :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.core :as state-flow :refer [flow]]))

(defflow
  flow-integration-database-test
  {:init (util/start-system!
          {:config (config/new-config)
           :database (component/using (database/new-database) [:config])})
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "creates a table, insert data and checks return in the database"
    (state-flow.db/execute! ["create table if not exists address (
                               id serial primary key,
                               name varchar(32),
                               email varchar(255))"])

    (state-flow.db/execute! ["insert into address(name,email)
                               values('Sam Campos de Milho','sammilhoso@email.com')"])

    (match? [#:address{:id 1
                       :name "Sam Campos de Milho"
                       :email "sammilhoso@email.com"}]
            (state-flow.db/execute! ["select * from address"]))))
