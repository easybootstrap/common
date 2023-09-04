(ns unit.easybootstrap.components.config-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.stuartsierra.component :as component]
            [easybootstrap.components.config :as config.aero]
            [matcher-combinators.test :refer [match?]]))

(defn- create-and-start-system!
  [{:keys [config]}]
  (component/start-system
   (component/system-map :config config)))

(deftest config-mock-component-test
  (testing "config should return mocked config"
    (let [system (create-and-start-system!
                  {:config (config.aero/new-config {:webserver/port 1234
                                                    :env :test})})]

      (is (match? {:config {:webserver/port 1234
                            :env :test}}
                  (:config system))))))
