(ns com.github.ebaptistella.wire.in.str.str0008-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.wire.in.str.str :as wire.in.str]
            com.github.ebaptistella.wire.in.str.str0008))

(deftest parse-inbound-str0008-test
  (testing "full XML — all extra fields populated"
    (let [xml  (str "<CodMsg>STR0008</CodMsg>"
                    "<NumCtrlIF>ABC123</NumCtrlIF>"
                    "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
                    "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
                    "<VlrLanc>1500.00</VlrLanc>"
                    "<FinlddCli>01</FinlddCli>"
                    "<DtMovto>20240115</DtMovto>")
          raw  {:queue-name "QL.REQ.00000000.99999999.01"
                :message-id "mid-1"
                :body       xml}
          m    (wire.in.str/parse-inbound raw)]
      (is (= :STR0008 (:type m)))
      (is (= "ABC123" (:num-ctrl-if m)))
      (is (= "00000000" (:ispb-if-debtd m)))
      (is (= "11111111" (:ispb-if-credtd m)))
      (is (= "1500.00" (:vlr-lanc m)))
      (is (= "01" (:finldd-cli m)))
      (is (= "20240115" (:dt-movto m)))
      (is (= :pending (:status m)))
      (is (= :inbound (:direction m)))
      (is (= "mid-1" (:message-id m)))
      (is (= "00000000" (:participant m)))))
  (testing "XML missing FinlddCli — :finldd-cli is nil"
    (let [xml (str "<CodMsg>STR0008</CodMsg>"
                   "<NumCtrlIF>X</NumCtrlIF>"
                   "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
                   "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
                   "<VlrLanc>1.00</VlrLanc>"
                   "<DtMovto>20240115</DtMovto>")
          m   (wire.in.str/parse-inbound {:queue-name "QL.REQ.00000000.99999999.01"
                                          :message-id "mid-2"
                                          :body       xml})]
      (is (nil? (:finldd-cli m)))))
  (testing "empty body \"\" — no exception; dispatches to :default (no CodMsg), all fields nil"
    (let [m (wire.in.str/parse-inbound {:queue-name "QL.REQ.00000000.99999999.01"
                                        :message-id "mid-3"
                                        :body       ""})]
      (is (= :unknown (:type m)))
      (is (nil? (:num-ctrl-if m)))
      (is (nil? (:ispb-if-debtd m)))
      (is (nil? (:finldd-cli m)))
      (is (= :pending (:status m))))))
