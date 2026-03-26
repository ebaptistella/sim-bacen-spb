(ns com.github.ebaptistella.frontend.events.outbound-events-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [com.github.ebaptistella.frontend.events]
            [com.github.ebaptistella.frontend.events.outbound-events]
            [com.github.ebaptistella.frontend.subs]
            [re-frame.core :as rf]))

(use-fixtures :each
  {:before (fn [] (rf/dispatch-sync [:initialize-db]))
   :after  (fn [] (rf/clear-subscription-cache!))})

(deftest open-modal-resets-state
  (testing "open-modal limpa estado anterior e abre modal"
    (rf/dispatch-sync [:outbound/set-type "STR0015"])
    (rf/dispatch-sync [:outbound/set-participant "12345678"])
    (rf/dispatch-sync [:outbound/set-param :hr-fechamento "17:30"])
    (rf/dispatch-sync [:outbound/open-modal])
    (is (true? @(rf/subscribe [:outbound/modal-visible?])))
    (is (nil? @(rf/subscribe [:outbound/type])))
    (is (= "" @(rf/subscribe [:outbound/participant])))
    (is (= {} @(rf/subscribe [:outbound/params])))
    (is (nil? @(rf/subscribe [:outbound/error])))))

(deftest close-modal
  (testing "close-modal fecha o modal"
    (rf/dispatch-sync [:outbound/open-modal])
    (rf/dispatch-sync [:outbound/close-modal])
    (is (false? @(rf/subscribe [:outbound/modal-visible?])))))

(deftest set-type-resets-params
  (testing "set-type armazena tipo e reseta params"
    (rf/dispatch-sync [:outbound/open-modal])
    (rf/dispatch-sync [:outbound/set-param :hr-fechamento "17:00"])
    (rf/dispatch-sync [:outbound/set-type "STR0017"])
    (is (= "STR0017" @(rf/subscribe [:outbound/type])))
    (is (= {} @(rf/subscribe [:outbound/params])))))

(deftest set-participant-stores-value
  (testing "set-participant armazena o ISPB do participante"
    (rf/dispatch-sync [:outbound/open-modal])
    (rf/dispatch-sync [:outbound/set-participant "00000000"])
    (is (= "00000000" @(rf/subscribe [:outbound/participant])))))

(deftest set-param-stores-value
  (testing "set-param armazena o valor do parâmetro pelo key"
    (rf/dispatch-sync [:outbound/open-modal])
    (rf/dispatch-sync [:outbound/set-type "STR0015"])
    (rf/dispatch-sync [:outbound/set-param :hr-fechamento "16:00"])
    (is (= "16:00" (get @(rf/subscribe [:outbound/params]) :hr-fechamento)))))

(deftest submit-success-closes-modal
  (testing "submit-success fecha modal e limpa flags de submissão"
    (rf/dispatch-sync [:outbound/open-modal])
    (rf/dispatch-sync [:outbound/submit-success])
    (is (false? @(rf/subscribe [:outbound/modal-visible?])))
    (is (false? @(rf/subscribe [:outbound/submitting?])))
    (is (nil? @(rf/subscribe [:outbound/error])))))

(deftest submit-error-stores-error
  (testing "submit-error armazena o erro sem fechar modal"
    (rf/dispatch-sync [:outbound/open-modal])
    (rf/dispatch-sync [:outbound/submit-error {:status 500 :message "Falha MQ"}])
    (is (= {:status 500 :message "Falha MQ"} @(rf/subscribe [:outbound/error])))
    (is (false? @(rf/subscribe [:outbound/submitting?])))
    (is (true? @(rf/subscribe [:outbound/modal-visible?])))))
