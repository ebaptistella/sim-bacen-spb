(ns com.github.ebaptistella.integration.flows-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.integration.aux.init :as aux.init]))

(use-fixtures :once
  (fn [f]
    (aux.init/start-test-system!)
    (f)
    (aux.init/stop-test-system!)))

(deftest health-check-test
  (testing "health check endpoint returns ok"
    ;; TODO: implement HTTP helper and assert on /api/health
    (is true)))
