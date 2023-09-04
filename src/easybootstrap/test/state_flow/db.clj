(ns easybootstrap.test.state-flow.db
  (:require [easybootstrap.components.database :as database]
            [state-flow.api :as state-flow.api]
            [state-flow.core :as state-flow :refer [flow]]))

(defn execute!
  [commands]
  (flow "makes database execution"
    [database (state-flow.api/get-state :database)]
    (-> database
        (database/execute commands)
        state-flow.api/return)))
