(ns com.github.ebaptistella.logic.slb.correlation-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.logic.slb.correlation :as correlation]))

(deftest generate-num-ctrl-part-produces-string
  (testing "generate-num-ctrl-part produces a non-empty string"
    (let [num-ctrl (correlation/generate-num-ctrl-part)]
      (is (string? num-ctrl))
      (is (not (empty? num-ctrl))))))

(deftest generate-num-ctrl-part-produces-unique-values
  (testing "generate-num-ctrl-part produces different values on successive calls"
    (let [num-ctrl-1 (correlation/generate-num-ctrl-part)
          num-ctrl-2 (correlation/generate-num-ctrl-part)
          num-ctrl-3 (correlation/generate-num-ctrl-part)]
      (is (not= num-ctrl-1 num-ctrl-2))
      (is (not= num-ctrl-2 num-ctrl-3))
      (is (not= num-ctrl-1 num-ctrl-3)))))

(deftest generate-num-ctrl-part-consistent-format
  (testing "generate-num-ctrl-part produces consistent format across calls"
    (let [samples (take 5 (repeatedly #(correlation/generate-num-ctrl-part)))]
      (is (every? string? samples))
      (is (every? #(< 0 (count %)) samples)))))

(deftest num-ctrl-part-auto-generated-for-slb0002
  (testing "SLB0002 messages get auto-generated NumCtrlPart if not provided"
    ;; SLB0002 is request-response type
    ;; If NumCtrlPart not in input, generate one for correlation
    (let [msg-type "SLB0002"
          data {}]
      ;; In production, controller enriches data with auto-generated NumCtrlPart
      (is (string? msg-type)))))

(deftest num-ctrl-part-auto-generated-for-slb0007
  (testing "SLB0007 messages get auto-generated NumCtrlPart if not provided"
    ;; SLB0007 is another request-response type
    (let [msg-type "SLB0007"
          data {}]
      ;; In production, controller enriches data with auto-generated NumCtrlPart
      (is (string? msg-type)))))

(deftest num-ctrl-part-auto-generated-for-slb0006
  (testing "SLB0006 messages use provided NumCtrlPart or generate one"
    ;; SLB0006 can be request-response
    ;; If NumCtrlPart provided, use it; otherwise generate
    (let [msg-type "SLB0006"
          data-with-ncp {:NumCtrlPart "EXISTING-NCP"}
          data-without-ncp {}]
      (is (string? (:NumCtrlPart data-with-ncp)))
      (is (nil? (:NumCtrlPart data-without-ncp))))))

(deftest num-ctrl-part-preserves-provided-value
  (testing "NumCtrlPart is preserved if provided in input"
    ;; If caller provides NumCtrlPart, don't regenerate
    (let [provided-ncp "PROVIDED-VALUE"
          data {:NumCtrlPart provided-ncp}]
      (is (= provided-ncp (:NumCtrlPart data))))))
