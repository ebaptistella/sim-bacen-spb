(ns com.github.ebaptistella.logic.str.str0016-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.logic.str.str0016 :as str0016]))

(deftest build-message-test
  (testing "contains CodMsg STR0016"
    (let [xml (str0016/build-message {} "00000000" {})]
      (is (str/includes? xml "<CodMsg>STR0016</CodMsg>"))))

  (testing "uses SldCntRsv from config when no params override"
    (let [xml (str0016/build-message {} "00000000" {:str-saldo-simulado "999.99"})]
      (is (str/includes? xml "<SldCntRsv>999.99</SldCntRsv>"))))

  (testing "params :saldo overrides config value"
    (let [xml (str0016/build-message {:saldo "500.00"} "00000000" {:str-saldo-simulado "999.99"})]
      (is (str/includes? xml "<SldCntRsv>500.00</SldCntRsv>"))
      (is (not (str/includes? xml "<SldCntRsv>999.99</SldCntRsv>")))))

  (testing "uses default 99999999.99 when no config nor params supplied"
    (let [xml (str0016/build-message {} "00000000" {})]
      (is (str/includes? xml "<SldCntRsv>99999999.99</SldCntRsv>"))))

  (testing "includes ISPBIFDebtd with the participant ISPB passed"
    (let [xml (str0016/build-message {} "00000000" {})]
      (is (str/includes? xml "<ISPBIFDebtd>00000000</ISPBIFDebtd>"))))

  (testing "returns non-empty string"
    (let [xml (str0016/build-message {} "00000000" {})]
      (is (string? xml))
      (is (pos? (count xml))))))

(deftest queue-name-test
  (testing "derives outbound queue name in correct format"
    (is (= "QR.REQ.99999999.00000000.01"
           (str0016/queue-name "99999999" "00000000")))))
