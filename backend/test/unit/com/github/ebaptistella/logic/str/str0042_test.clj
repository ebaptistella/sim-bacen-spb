(ns com.github.ebaptistella.logic.str.str0042-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.logic.str.str0042 :as str0042]))

(deftest build-message-test
  (testing "contains CodMsg STR0042"
    (let [xml (str0042/build-message {} {})]
      (is (str/includes? xml "<CodMsg>STR0042</CodMsg>"))))
  (testing "IndOtimizacao defaults to I"
    (let [xml (str0042/build-message {} {})]
      (is (str/includes? xml "<IndOtimizacao>I</IndOtimizacao>"))))
  (testing "IndOtimizacao from params"
    (let [xml (str0042/build-message {:ind-otimizacao "F"} {})]
      (is (str/includes? xml "<IndOtimizacao>F</IndOtimizacao>"))))
  (testing "returns well-formed XML"
    (let [xml (str0042/build-message {} {})]
      (is (str/starts-with? xml "<STR0042>"))
      (is (str/ends-with? xml "</STR0042>")))))

(deftest queue-name-test
  (testing "derives outbound queue name"
    (is (= "QR.REQ.99999999.00000000.01"
           (str0042/queue-name "99999999" "00000000")))))
