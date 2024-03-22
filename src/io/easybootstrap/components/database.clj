(ns io.easybootstrap.components.database
  (:require [com.stuartsierra.component :as component]
            [io.easybootstrap.helpers.logs :as logs]
            [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection])
  (:import (com.zaxxer.hikari HikariDataSource)))

(defprotocol DatabaseProvider
  (execute [self sql-params]
    [self sql-params opts]
    "Low-level API to execute a command in the database"))

(defrecord Database [config ^HikariDataSource datasource]
  component/Lifecycle
  (start [this]
    (let [{:keys [host port dbtype] :as db-spec} (get-in config [:config :database])]
      (logs/log :info :database :start {:host host :port port :dbtype dbtype})
      (if datasource
        this
        (assoc this :datasource (connection/->pool HikariDataSource db-spec)))))
  (stop [this]
    (logs/log :info :database :stop)
    (if datasource
      (do
        (.close datasource)
        (assoc this :datasource nil))
      this))

  DatabaseProvider
  (execute [this sql-params]
    (jdbc/execute! (:datasource this) sql-params))
  (execute [this sql-params opts]
    (jdbc/execute! (:datasource this) sql-params opts)))

(defn new-database []
  (map->Database {}))
