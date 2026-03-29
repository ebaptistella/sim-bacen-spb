(ns com.github.ebaptistella.frontend.events.slb-form-events-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures async]]
            [com.github.ebaptistella.frontend.events]
            [com.github.ebaptistella.frontend.events.slb-form-events]
            [com.github.ebaptistella.frontend.subs]
            [re-frame.core :as rf]))

(use-fixtures :each
  {:before (fn [] (rf/dispatch-sync [:initialize-db]))
   :after  (fn [] (rf/clear-subscription-cache!))})

(deftest open-form-initializes-state
  (testing "open resets estado anterior e abre formulário"
    (rf/dispatch-sync [:slb-form/set-type "SLB0002"])
    (rf/dispatch-sync [:slb-form/set-field :NumCtrlPart "12345"])
    (rf/dispatch-sync [:slb-form/open])
    (is (true? @(rf/subscribe [:slb-form/visible?])))
    (is (nil? @(rf/subscribe [:slb-form/type])))
    (is (= {} @(rf/subscribe [:slb-form/fields])))
    (is (false? @(rf/subscribe [:slb-form/submitting?])))
    (is (nil? @(rf/subscribe [:slb-form/error])))))

(deftest close-form
  (testing "close fecha o formulário"
    (rf/dispatch-sync [:slb-form/open])
    (rf/dispatch-sync [:slb-form/close])
    (is (false? @(rf/subscribe [:slb-form/visible?])))))

(deftest set-type-resets-fields
  (testing "set-type armazena tipo e reseta campos"
    (rf/dispatch-sync [:slb-form/open])
    (rf/dispatch-sync [:slb-form/set-field :NumCtrlPart "12345"])
    (rf/dispatch-sync [:slb-form/set-type "SLB0002"])
    (is (= "SLB0002" @(rf/subscribe [:slb-form/type])))
    (is (= {} @(rf/subscribe [:slb-form/fields])))))

(deftest set-field-stores-value
  (testing "set-field armazena o valor do campo"
    (rf/dispatch-sync [:slb-form/open])
    (rf/dispatch-sync [:slb-form/set-type "SLB0002"])
    (rf/dispatch-sync [:slb-form/set-field :NumCtrlPart "12345"])
    (is (= "12345" (get @(rf/subscribe [:slb-form/fields]) :NumCtrlPart)))))

(deftest submit-success-closes-form
  (testing "submit-success fecha formulário e limpa flags"
    (rf/dispatch-sync [:slb-form/open])
    (rf/dispatch-sync [:slb-form/submit-success])
    (is (false? @(rf/subscribe [:slb-form/visible?])))
    (is (false? @(rf/subscribe [:slb-form/submitting?])))
    (is (nil? @(rf/subscribe [:slb-form/error])))))

(deftest submit-error-stores-error
  (testing "submit-error armazena erro sem fechar formulário"
    (rf/dispatch-sync [:slb-form/open])
    (rf/dispatch-sync [:slb-form/submit-error {:status 400 :message "Campos inválidos"}])
    (is (= {:status 400 :message "Campos inválidos"} @(rf/subscribe [:slb-form/error])))
    (is (false? @(rf/subscribe [:slb-form/submitting?])))
    (is (true? @(rf/subscribe [:slb-form/visible?])))))

(deftest submit-sets-submitting-flag
  (testing "submit coloca form em estado de submissão"
    (rf/dispatch-sync [:slb-form/open])
    (rf/dispatch-sync [:slb-form/set-type "SLB0002"])
    (rf/dispatch-sync [:slb-form/set-field :NumCtrlPart "12345"])
    (rf/dispatch-sync [:slb-form/set-field :ISPBPart "00000000"])
    (rf/dispatch-sync [:slb-form/set-field :VlrLanc "100.00"])
    (rf/dispatch-sync [:slb-form/submit])
    (is (true? @(rf/subscribe [:slb-form/submitting?])))))
