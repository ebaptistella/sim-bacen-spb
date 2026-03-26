(ns com.github.ebaptistella.wire.in.str.str0011-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0011
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private sample-xml
  "<STR0011><CodMsg>STR0011</CodMsg><NumCtrlIF>NC-011</NumCtrlIF><ISPBIFDebtd>00000000</ISPBIFDebtd><NumCtrlSTROr>NC-001</NumCtrlSTROr></STR0011>")

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-1"
   :body       sample-xml})

(deftest str0011-wire-in-type-test
  (testing ":type is STR0011"
    (let [result (parse-inbound raw-input)]
      (is (= "STR0011" (:type result))))))

(deftest str0011-wire-in-status-test
  (testing ":status is :pending"
    (let [result (parse-inbound raw-input)]
      (is (= :pending (:status result))))))

(deftest str0011-wire-in-direction-test
  (testing ":direction is :inbound"
    (let [result (parse-inbound raw-input)]
      (is (= :inbound (:direction result))))))

(deftest str0011-wire-in-num-ctrl-str-or-test
  (testing ":num-ctrl-str-or echoes NumCtrlSTROr from XML"
    (let [result (parse-inbound raw-input)]
      (is (= "NC-001" (:num-ctrl-str-or result))))))

(deftest str0011-wire-in-num-ctrl-if-test
  (testing ":num-ctrl-if echoes NumCtrlIF from XML"
    (let [result (parse-inbound raw-input)]
      (is (= "NC-011" (:num-ctrl-if result))))))

(deftest str0011-wire-in-ispb-if-debtd-test
  (testing ":ispb-if-debtd echoes ISPBIFDebtd from XML"
    (let [result (parse-inbound raw-input)]
      (is (= "00000000" (:ispb-if-debtd result))))))

(deftest str0011-wire-in-no-original-msg-id-test
  (testing ":original-msg-id is not present in the result map"
    (let [result (parse-inbound raw-input)]
      (is (not (contains? result :original-msg-id))))))
