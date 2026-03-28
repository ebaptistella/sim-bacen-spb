(ns com.github.ebaptistella.logic.str.str0050-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.logic.str.str0050 :as str0050]))

(deftest build-message-test
  (testing "contains CodMsg STR0050"
    (let [xml (str0050/build-message {} {})]
      (is (str/includes? xml "<CodMsg>STR0050</CodMsg>"))))
  (testing "returns well-formed XML"
    (let [xml (str0050/build-message {} {})]
      (is (str/starts-with? xml "<STR0050>"))
      (is (str/ends-with? xml "</STR0050>")))))

(deftest queue-name-test
  (testing "derives outbound queue name"
    (is (= "QR.REQ.99999999.00000000.01"
           (str0050/queue-name "99999999" "00000000")))))
