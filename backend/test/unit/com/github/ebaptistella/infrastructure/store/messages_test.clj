(ns com.github.ebaptistella.infrastructure.store.messages-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]))

(defn- make-store []
  {:store (atom {:messages []})})

(deftest find-by-num-ctrl-if-encontrado
  (testing "returns the message when num-ctrl-if matches"
    (let [store (make-store)
          msg   {:id "uuid-1" :num-ctrl-if "NC-001" :type "STR0008" :status :pending}
          _     (store.messages/save! store msg)
          result (store.messages/find-by-num-ctrl-if store "NC-001")]
      (is (= msg result)))))

(deftest find-by-num-ctrl-if-nao-encontrado
  (testing "returns nil when num-ctrl-if is not in the store"
    (let [store  (make-store)
          msg    {:id "uuid-2" :num-ctrl-if "NC-002" :type "STR0008" :status :pending}
          _      (store.messages/save! store msg)
          result (store.messages/find-by-num-ctrl-if store "NC-001")]
      (is (nil? result)))))

(deftest find-by-num-ctrl-if-nil-input
  (testing "returns nil when num-ctrl-if argument is nil"
    (let [store  (make-store)
          msg    {:id "uuid-3" :num-ctrl-if "NC-003" :type "STR0008" :status :pending}
          _      (store.messages/save! store msg)
          result (store.messages/find-by-num-ctrl-if store nil)]
      (is (nil? result)))))

(deftest find-by-num-ctrl-if-multiplos
  (testing "returns the correct message among multiple stored messages"
    (let [store  (make-store)
          msg-a  {:id "uuid-4" :num-ctrl-if "NC-004" :type "STR0008" :status :pending}
          msg-b  {:id "uuid-5" :num-ctrl-if "NC-005" :type "STR0011" :status :responded}
          _      (store.messages/save! store msg-a)
          _      (store.messages/save! store msg-b)
          result (store.messages/find-by-num-ctrl-if store "NC-005")]
      (is (= msg-b result))
      (is (= "uuid-5" (:id result))))))
