(ns com.github.ebaptistella.logic.str.str0017-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.logic.str.str0017 :as str0017]))

(deftest build-message-test
  (testing "contains CodMsg STR0017"
    (let [xml (str0017/build-message {} {})]
      (is (str/includes? xml "<CodMsg>STR0017</CodMsg>"))))

  (testing "uses HrAbertura and HrFechamento from config when no params override"
    (let [xml (str0017/build-message {} {:str-horario-abertura "07:00"
                                         :str-horario-fechamento "17:30"})]
      (is (str/includes? xml "<HrAbertura>07:00</HrAbertura>"))
      (is (str/includes? xml "<HrFechamento>17:30</HrFechamento>"))))

  (testing "params :hr-abertura and :hr-fechamento override config values"
    (let [xml (str0017/build-message {:hr-abertura "06:30" :hr-fechamento "18:00"}
                                     {:str-horario-abertura "07:00"
                                      :str-horario-fechamento "17:30"})]
      (is (str/includes? xml "<HrAbertura>06:30</HrAbertura>"))
      (is (str/includes? xml "<HrFechamento>18:00</HrFechamento>"))
      (is (not (str/includes? xml "<HrAbertura>07:00</HrAbertura>")))
      (is (not (str/includes? xml "<HrFechamento>17:30</HrFechamento>")))))

  (testing "uses defaults 07:00 and 17:30 when no config nor params supplied"
    (let [xml (str0017/build-message {} {})]
      (is (str/includes? xml "<HrAbertura>07:00</HrAbertura>"))
      (is (str/includes? xml "<HrFechamento>17:30</HrFechamento>"))))

  (testing "returns non-empty string"
    (let [xml (str0017/build-message {} {})]
      (is (string? xml))
      (is (pos? (count xml))))))

(deftest queue-name-test
  (testing "derives outbound queue name in correct format"
    (is (= "QR.REQ.99999999.00000000.01"
           (str0017/queue-name "99999999" "00000000")))))
