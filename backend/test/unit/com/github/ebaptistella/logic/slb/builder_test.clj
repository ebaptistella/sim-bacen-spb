(ns com.github.ebaptistella.logic.slb.builder-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.logic.slb.builder :as builder]
            [clojure.string :as str]))

(deftest slb0001-xml-construction
  (testing "Build valid SLB0001 XML"
    (let [data {:NumCtrlSLB "NC-001"
                :ISPBPart "12345678"
                :DtVenc "20260328"
                :VlrLanc "1000.50"
                :FIndddSLB "05"}
          xml (builder/build-slb0001-xml data)]
      (is (str/includes? xml "<?xml"))
      (is (str/includes? xml "<SLB0001>"))
      (is (str/includes? xml "<CodMsg>SLB0001</CodMsg>"))
      (is (str/includes? xml "<NumCtrlSLB>NC-001</NumCtrlSLB>"))
      (is (str/includes? xml "<VlrLanc>1000.50</VlrLanc>"))
      (is (str/includes? xml "</SLB0001>"))))

  (testing "XML special character escaping"
    (let [data {:NumCtrlSLB "NC<001>"
                :ISPBPart "12345678"
                :DtVenc "20260328"
                :VlrLanc "1000.50"
                :FIndddSLB "05"}
          xml (builder/build-slb0001-xml data)]
      (is (str/includes? xml "&lt;"))
      (is (str/includes? xml "&gt;")))))

(deftest slb0006-xml-construction
  (testing "Build SLB0006 XML with optional filters"
    (let [data {:NumCtrlPart "QUERY-001"
                :ISPBPart "12345678"
                :DtRef "20260328"
                :TpDeb_Cred "Débito"
                :NumCtrlSLB "NC-001"}
          xml (builder/build-slb0006-xml data)]
      (is (str/includes? xml "<CodMsg>SLB0006</CodMsg>"))
      (is (str/includes? xml "<NumCtrlPart>QUERY-001</NumCtrlPart>"))
      (is (str/includes? xml "<DtRef>20260328</DtRef>")))))

(deftest get-builder-dispatch
  (testing "Get builder function for each SLB type"
    (is (fn? (builder/get-builder "SLB0001")))
    (is (fn? (builder/get-builder "SLB0002")))
    (is (fn? (builder/get-builder "SLB0006")))
    (is (nil? (builder/get-builder "SLB9999")))))
