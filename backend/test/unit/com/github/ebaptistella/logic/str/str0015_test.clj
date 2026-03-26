(ns com.github.ebaptistella.logic.str.str0015-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.logic.str.str0015 :as str0015]))

(deftest build-message-test
  (testing "contains CodMsg STR0015"
    (let [xml (str0015/build-message {} {})]
      (is (str/includes? xml "<CodMsg>STR0015</CodMsg>"))))

  (testing "uses HrFechamento from config when no params override"
    (let [xml (str0015/build-message {} {:str-horario-fechamento "17:30"})]
      (is (str/includes? xml "<HrFechamento>17:30</HrFechamento>"))))

  (testing "params :hr-fechamento overrides config value"
    (let [xml (str0015/build-message {:hr-fechamento "16:00"} {:str-horario-fechamento "17:30"})]
      (is (str/includes? xml "<HrFechamento>16:00</HrFechamento>"))
      (is (not (str/includes? xml "<HrFechamento>17:30</HrFechamento>")))))

  (testing "uses default 17:30 when no config nor params supplied"
    (let [xml (str0015/build-message {} {})]
      (is (str/includes? xml "<HrFechamento>17:30</HrFechamento>"))))

  (testing "returns non-empty string"
    (let [xml (str0015/build-message {} {})]
      (is (string? xml))
      (is (pos? (count xml))))))

(deftest queue-name-test
  (testing "derives outbound queue name in correct format"
    (is (= "QR.REQ.99999999.00000000.01"
           (str0015/queue-name "99999999" "00000000")))))
