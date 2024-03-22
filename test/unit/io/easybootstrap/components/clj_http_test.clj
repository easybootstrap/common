(ns unit.io.easybootstrap.components.clj-http-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.stuartsierra.component :as component]
            [io.easybootstrap.components.clj-http :as clj-http]
            [matcher-combinators.test :refer [match?]]))

(defn- create-and-start-system!
  [{:keys [http]}]
  (component/start-system
   (component/system-map :http http)))

(deftest http-mock-component-test
  (testing "HttpMock should return mocked reponses and log requests in the atom"
    (let [system (create-and-start-system!
                  {:http (clj-http/new-http-mock
                          {"https://github.com" {:status 200}})})]

      (is (match? {:status 200}
                  (clj-http/request (:http system) {:url "https://github.com"})))

      (is (match? {:status 500}
                  (clj-http/request (:http system) {:url "https://google.com"})))

      (is (match? ["https://github.com"
                   "https://google.com"]
                  (map :url (deref (get-in system [:http :requests]))))))))
