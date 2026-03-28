(ns com.github.ebaptistella.wire.in.str.str0046-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0046
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-046"
   :body       (str "<STR0046>"
                    "<CodMsg>STR0046</CodMsg>"
                    "<NumCtrlIF>NC-046</NumCtrlIF>"
                    "<NumCtrlSTROr>NC-020</NumCtrlSTROr>"
                    "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
                    "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
                    "<VlrLanc>500.00</VlrLanc>"
                    "<FinlddIF>00046</FinlddIF>"
                    "<CodDevTransf>MD01</CodDevTransf>"
                    "<DtMovto>20260327</DtMovto>"
                    "</STR0046>")})

(deftest str0046-wire-in-type-test
  (testing ":type is :STR0046"
    (is (= :STR0046 (:type (parse-inbound raw-input))))))

(deftest str0046-wire-in-devolucao-fields-test
  (testing "devolução fields mapped"
    (let [m (parse-inbound raw-input)]
      (is (= "NC-020" (:num-ctrl-str-or m)))
      (is (= "MD01" (:cod-dev-transf m)))
      (is (= "00046" (:finldd-if m))))))
