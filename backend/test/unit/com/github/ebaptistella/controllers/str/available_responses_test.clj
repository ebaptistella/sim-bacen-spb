(ns com.github.ebaptistella.controllers.str.available-responses-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.controllers.str.str0005
            com.github.ebaptistella.controllers.str.str0006
            com.github.ebaptistella.controllers.str.str0007
            com.github.ebaptistella.controllers.str.str0008
            com.github.ebaptistella.controllers.str.str0011
            [com.github.ebaptistella.controllers.str.str :refer [available-responses]]))

(deftest available-responses-test
  (testing "STR0005 returns R1, R2, E in order"
    (is (= [:STR0005R1 :STR0005R2 :STR0005E]
           (available-responses {:type :STR0005}))))

  (testing "STR0006 returns R1, R2, E in order"
    (is (= [:STR0006R1 :STR0006R2 :STR0006E]
           (available-responses {:type :STR0006}))))

  (testing "STR0007 returns R1, R2, E in order"
    (is (= [:STR0007R1 :STR0007R2 :STR0007E]
           (available-responses {:type :STR0007}))))

  (testing "STR0008 returns R1, R2, E in order"
    (is (= [:STR0008R1 :STR0008R2 :STR0008E]
           (available-responses {:type :STR0008}))))

  (testing "STR0011 returns R1 and E only (no R2)"
    (is (= [:STR0011R1 :STR0011E]
           (available-responses {:type :STR0011})))
    (is (not (contains? (set (available-responses {:type :STR0011})) :STR0011R2))))

  (testing "unknown type returns nil"
    (is (nil? (available-responses {:type :STR9999}))))

  (testing "query types (auto-responded) return nil"
    (is (nil? (available-responses {:type :STR0012})))
    (is (nil? (available-responses {:type :STR0001})))))
