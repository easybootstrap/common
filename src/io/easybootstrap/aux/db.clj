(ns io.easybootstrap.aux.db
  (:require [io.easybootstrap.components.database :as c.database]
            [state-flow.api :as state-flow.api]
            [state-flow.core :as state-flow :refer [flow]]))

(defn execute!
  [commands]
  (flow "makes database execution"
    [database (state-flow.api/get-state :database)]
    (-> database
        (c.database/execute commands)
        state-flow.api/return)))
