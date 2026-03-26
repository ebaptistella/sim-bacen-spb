(ns com.github.ebaptistella.logic.str.str0012-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.logic.str.str0012 :as str0012]))

(def ^:private msg-pending
  {:id           "uuid-1"
   :type         "STR0008"
   :status       :pending
   :num-ctrl-if  "CTRL001"
   :ispb-if-debtd "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc     "1000.00"
   :received-at  "20240115120000"
   :response     nil})

(def ^:private msg-responded
  {:id           "uuid-2"
   :type         "STR0008"
   :status       :responded
   :num-ctrl-if  "CTRL002"
   :ispb-if-debtd "00000000"
   :ispb-if-credtd "22222222"
   :vlr-lanc     "2000.00"
   :received-at  "20240115130000"
   :response     {:num-ctrl-str "abcdef1234567890abcd" :sent-at "20240115130500"}})

(def ^:private msg-auto-responded
  {:id           "uuid-3"
   :type         "STR0008"
   :status       :auto-responded
   :num-ctrl-if  "CTRL003"
   :ispb-if-debtd "33333333"
   :ispb-if-credtd "44444444"
   :vlr-lanc     "3000.00"
   :received-at  "20240115140000"
   :response     {:num-ctrl-str "fedcba9876543210fedc" :sent-at "20240115140500"}})

(def ^:private store-msgs [msg-pending msg-responded msg-auto-responded])

(deftest filter-lancamentos-sem-filtros-test
  (testing "without filters returns all messages unchanged"
    (let [parsed-msg {:num-ctrl-if "Q" :ispb-if-debtd "00000000"
                      :num-ctrl-str-or nil :sit-lanc-str nil}
          result     (str0012/filter-lancamentos store-msgs parsed-msg)]
      (is (= 3 (count result)))
      (is (= store-msgs result)))))

(deftest filter-lancamentos-num-ctrl-str-or-test
  (testing "filters by :num-ctrl-str-or matching response :num-ctrl-str"
    (let [parsed-msg {:num-ctrl-if "Q" :ispb-if-debtd "00000000"
                      :num-ctrl-str-or "abcdef1234567890abcd" :sit-lanc-str nil}
          result     (str0012/filter-lancamentos store-msgs parsed-msg)]
      (is (= 1 (count result)))
      (is (= "uuid-2" (:id (first result))))))
  (testing "no match for non-existent NumCtrlSTROr returns empty list"
    (let [parsed-msg {:num-ctrl-if "Q" :ispb-if-debtd "00000000"
                      :num-ctrl-str-or "0000000000000000zzzz" :sit-lanc-str nil}
          result     (str0012/filter-lancamentos store-msgs parsed-msg)]
      (is (empty? result)))))

(deftest filter-lancamentos-sit-lanc-str-test
  (testing "sit-lanc-str=PENDENTE filters only :pending messages"
    (let [parsed-msg {:num-ctrl-if "Q" :ispb-if-debtd "00000000"
                      :num-ctrl-str-or nil :sit-lanc-str "PENDENTE"}
          result     (str0012/filter-lancamentos store-msgs parsed-msg)]
      (is (= 1 (count result)))
      (is (= :pending (:status (first result))))))
  (testing "sit-lanc-str=LQDADO filters :responded and :auto-responded messages"
    (let [parsed-msg {:num-ctrl-if "Q" :ispb-if-debtd "00000000"
                      :num-ctrl-str-or nil :sit-lanc-str "LQDADO"}
          result     (str0012/filter-lancamentos store-msgs parsed-msg)]
      (is (= 2 (count result)))
      (is (every? #(contains? #{:responded :auto-responded} (:status %)) result)))))

(deftest r1-response-lista-vazia-test
  (testing "empty lancamentos list produces QtdLanc=0 and no Lancamento blocks"
    (let [parsed-msg {:num-ctrl-if "CTRL0012" :ispb-if-debtd "55555555"}
          xml        (str0012/r1-response parsed-msg [])]
      (is (str/includes? xml "<QtdLanc>0</QtdLanc>"))
      (is (not (str/includes? xml "<Lancamento>"))))))

(deftest r1-response-com-lancamentos-test
  (testing "2 lancamentos produce QtdLanc=2 and 2 Lancamento blocks"
    (let [parsed-msg {:num-ctrl-if "CTRL0012" :ispb-if-debtd "55555555"}
          lancamentos [msg-responded msg-auto-responded]
          xml         (str0012/r1-response parsed-msg lancamentos)]
      (is (str/includes? xml "<QtdLanc>2</QtdLanc>"))
      (is (= 2 (count (re-seq #"<Lancamento>" xml))))))
  (testing "Lancamento blocks contain NumCtrlSTRLanc from response"
    (let [parsed-msg {:num-ctrl-if "CTRL0012" :ispb-if-debtd "55555555"}
          xml        (str0012/r1-response parsed-msg [msg-responded])]
      (is (str/includes? xml "<NumCtrlSTRLanc>abcdef1234567890abcd</NumCtrlSTRLanc>")))))

(deftest r1-response-ecoa-campos-test
  (testing "NumCtrlIF from parsed-msg is echoed in XML"
    (let [parsed-msg {:num-ctrl-if "ECHOCTRL" :ispb-if-debtd "66666666"}
          xml        (str0012/r1-response parsed-msg [])]
      (is (str/includes? xml "<NumCtrlIF>ECHOCTRL</NumCtrlIF>"))))
  (testing "ISPBIFDebtd from parsed-msg is echoed in XML"
    (let [parsed-msg {:num-ctrl-if "X" :ispb-if-debtd "77777777"}
          xml        (str0012/r1-response parsed-msg [])]
      (is (str/includes? xml "<ISPBIFDebtd>77777777</ISPBIFDebtd>"))))
  (testing "CodMsg is STR0012R1"
    (let [parsed-msg {:num-ctrl-if "X" :ispb-if-debtd "88888888"}
          xml        (str0012/r1-response parsed-msg [])]
      (is (str/includes? xml "<CodMsg>STR0012R1</CodMsg>"))))
  (testing "NumCtrlSTR has 20 alphanumeric chars"
    (let [parsed-msg {:num-ctrl-if "X" :ispb-if-debtd "99999999"}
          xml        (str0012/r1-response parsed-msg [])
          match      (re-find #"<NumCtrlSTR>([^<]+)</NumCtrlSTR>" xml)
          num-ctrl   (second match)]
      (is (some? num-ctrl))
      (is (= 20 (count num-ctrl))))))
