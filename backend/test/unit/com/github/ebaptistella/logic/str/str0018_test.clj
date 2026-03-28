(ns com.github.ebaptistella.logic.str.str0018-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.logic.str.str0018 :as str0018]))

(deftest build-message-test
  (testing "contains CodMsg STR0018"
    (let [xml (str0018/build-message {} {})]
      (is (str/includes? xml "<CodMsg>STR0018</CodMsg>"))))
  (testing "uses :ispb-participante from params"
    (let [xml (str0018/build-message {:ispb-participante "12345678"} {})]
      (is (str/includes? xml "<ISPBParticipante>12345678</ISPBParticipante>"))))
  (testing "falls back to :simulator-ispb from config"
    (let [xml (str0018/build-message {} {:simulator-ispb "99999999"})]
      (is (str/includes? xml "<ISPBParticipante>99999999</ISPBParticipante>"))))
  (testing "returns non-empty XML string"
    (let [xml (str0018/build-message {} {})]
      (is (string? xml))
      (is (str/starts-with? xml "<STR0018>"))
      (is (str/ends-with? xml "</STR0018>")))))

(deftest queue-name-test
  (testing "derives outbound queue name"
    (is (= "QR.REQ.99999999.00000000.01"
           (str0018/queue-name "99999999" "00000000")))))
