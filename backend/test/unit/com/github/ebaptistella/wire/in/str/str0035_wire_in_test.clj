(ns com.github.ebaptistella.wire.in.str.str0035-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0035
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))\

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-035"
   :body       (str "<STR0035>"
                    "<CodMsg>STR0035</CodMsg>"
                    "<NumCtrlIF>NC-035</NumCtrlIF>"
                    "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
                    "<DtRef>20260327</DtRef>"
                    "<HrIni>07:00</HrIni>"
                    "<HrFim>17:30</HrFim>"
                    "</STR0035>")})

(deftest str0035-wire-in-type-test
  (testing ":type is :STR0035"
    (is (= :STR0035 (:type (parse-inbound raw-input))))))

(deftest str0035-wire-in-fields-test
  (testing "fields mapped"
    (let [m (parse-inbound raw-input)]
      (is (= "NC-035" (:num-ctrl-if m)))
      (is (= "00000000" (:ispb-if-debtd m)))
      (is (= "20260327" (:dt-ref m)))
      (is (= "07:00" (:hr-ini m)))
      (is (= "17:30" (:hr-fim m)))
      (is (= :auto-responded (:status m))))))
