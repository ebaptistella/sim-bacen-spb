(ns com.github.ebaptistella.frontend.subs.outbound-subs-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [com.github.ebaptistella.frontend.events]
            [com.github.ebaptistella.frontend.events.outbound-events]
            [com.github.ebaptistella.frontend.subs]
            [re-frame.core :as rf]))

(use-fixtures :each
  {:before (fn [] (rf/dispatch-sync [:initialize-db]))
   :after  (fn [] (rf/clear-subscription-cache!))})

(deftest modal-visible-sub-default-false
  (testing ":outbound/modal-visible? é false após initialize-db"
    (is (false? @(rf/subscribe [:outbound/modal-visible?])))))

(deftest type-sub-default-nil
  (testing ":outbound/type é nil após initialize-db"
    (is (nil? @(rf/subscribe [:outbound/type])))))

(deftest participant-sub-default-empty
  (testing ":outbound/participant é string vazia após initialize-db"
    (is (= "" @(rf/subscribe [:outbound/participant])))))

(deftest params-sub-default-empty-map
  (testing ":outbound/params é mapa vazio após initialize-db"
    (is (= {} @(rf/subscribe [:outbound/params])))))

(deftest submitting-sub-default-false
  (testing ":outbound/submitting? é false após initialize-db"
    (is (false? @(rf/subscribe [:outbound/submitting?])))))

(deftest error-sub-default-nil
  (testing ":outbound/error é nil após initialize-db"
    (is (nil? @(rf/subscribe [:outbound/error])))))

(deftest subs-reflect-events
  (testing "subs refletem corretamente os eventos dispatched"
    (rf/dispatch-sync [:outbound/open-modal])
    (rf/dispatch-sync [:outbound/set-type "STR0016"])
    (rf/dispatch-sync [:outbound/set-participant "99999999"])
    (is (true? @(rf/subscribe [:outbound/modal-visible?])))
    (is (= "STR0016" @(rf/subscribe [:outbound/type])))
    (is (= "99999999" @(rf/subscribe [:outbound/participant])))))
