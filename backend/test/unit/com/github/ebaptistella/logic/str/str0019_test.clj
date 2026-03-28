(ns com.github.ebaptistella.logic.str.str0019-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.logic.str.str0019 :as str0019]))

(deftest build-message-test
  (testing "contains CodMsg STR0019"
    (let [xml (str0019/build-message {} {})]
      (is (str/includes? xml "<CodMsg>STR0019</CodMsg>"))))
  (testing "uses :ispb-participante from params"
    (let [xml (str0019/build-message {:ispb-participante "12345678"} {})]
      (is (str/includes? xml "<ISPBParticipante>12345678</ISPBParticipante>"))))
  (testing "returns non-empty XML string"
    (let [xml (str0019/build-message {} {})]
      (is (string? xml))
      (is (str/starts-with? xml "<STR0019>"))
      (is (str/ends-with? xml "</STR0019>")))))

(deftest queue-name-test
  (testing "derives outbound queue name"
    (is (= "QR.REQ.99999999.00000000.01"
           (str0019/queue-name "99999999" "00000000")))))
