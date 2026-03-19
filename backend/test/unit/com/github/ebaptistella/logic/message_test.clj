(ns com.github.ebaptistella.logic.message-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.logic.message :as logic.message]))

(deftest process-test
  (testing "processes a SPB message and returns status :processed"
    (let [msg    {:queue-name "QL.REQ.00000000.99999999.01"
                  :message-id "msg-001"
                  :body       "<xml>...</xml>"}
          result (logic.message/process msg)]
      (is (= :processed (:status result)))
      (is (= "msg-001" (:message-id result)))
      (is (= "QL.REQ.00000000.99999999.01" (:queue-name result))))))
