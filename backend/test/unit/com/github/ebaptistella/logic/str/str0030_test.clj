(ns com.github.ebaptistella.logic.str.str0030-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.logic.str.str0030 :as str0030]))

(deftest build-message-test
  (testing "contains CodMsg STR0030"
    (let [xml (str0030/build-message {} {})]
      (is (str/includes? xml "<CodMsg>STR0030</CodMsg>"))))
  (testing "returns well-formed XML"
    (let [xml (str0030/build-message {} {})]
      (is (str/starts-with? xml "<STR0030>"))
      (is (str/ends-with? xml "</STR0030>")))))

(deftest queue-name-test
  (testing "derives outbound queue name"
    (is (= "QR.REQ.99999999.00000000.01"
           (str0030/queue-name "99999999" "00000000")))))
