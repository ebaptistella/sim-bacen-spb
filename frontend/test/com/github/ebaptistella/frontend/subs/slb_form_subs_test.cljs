(ns com.github.ebaptistella.frontend.subs.slb-form-subs-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [com.github.ebaptistella.frontend.events]
            [com.github.ebaptistella.frontend.subs]
            [re-frame.core :as rf]))

(use-fixtures :each
  {:before (fn [] (rf/dispatch-sync [:initialize-db]))
   :after  (fn [] (rf/clear-subscription-cache!))})

(deftest visible-sub-default-false
  (testing "visible? retorna false por padrão"
    (is (false? @(rf/subscribe [:slb-form/visible?])))))

(deftest visible-sub-after-open
  (testing "visible? retorna true após open"
    (rf/dispatch-sync [:slb-form/open])
    (is (true? @(rf/subscribe [:slb-form/visible?])))))

(deftest type-sub-default-nil
  (testing "type retorna nil por padrão"
    (is (nil? @(rf/subscribe [:slb-form/type])))))

(deftest type-sub-after-set
  (testing "type retorna o tipo selecionado"
    (rf/dispatch-sync [:slb-form/open])
    (rf/dispatch-sync [:slb-form/set-type "SLB0002"])
    (is (= "SLB0002" @(rf/subscribe [:slb-form/type])))))

(deftest fields-sub-default-empty
  (testing "fields retorna mapa vazio por padrão"
    (is (= {} @(rf/subscribe [:slb-form/fields])))))

(deftest fields-sub-after-set
  (testing "fields retorna campos preenchidos"
    (rf/dispatch-sync [:slb-form/open])
    (rf/dispatch-sync [:slb-form/set-type "SLB0002"])
    (rf/dispatch-sync [:slb-form/set-field :NumCtrlPart "12345"])
    (rf/dispatch-sync [:slb-form/set-field :ISPBPart "00000000"])
    (is (= {:NumCtrlPart "12345" :ISPBPart "00000000"}
           @(rf/subscribe [:slb-form/fields])))))

(deftest submitting-sub-default-false
  (testing "submitting? retorna false por padrão"
    (is (false? @(rf/subscribe [:slb-form/submitting?])))))

(deftest submitting-sub-after-submit
  (testing "submitting? retorna true durante submissão"
    (rf/dispatch-sync [:slb-form/open])
    (rf/dispatch-sync [:slb-form/set-type "SLB0002"])
    (rf/dispatch-sync [:slb-form/set-field :NumCtrlPart "12345"])
    (rf/dispatch-sync [:slb-form/set-field :ISPBPart "00000000"])
    (rf/dispatch-sync [:slb-form/set-field :VlrLanc "100.00"])
    (rf/dispatch-sync [:slb-form/submit])
    (is (true? @(rf/subscribe [:slb-form/submitting?])))))

(deftest error-sub-default-nil
  (testing "error retorna nil por padrão"
    (is (nil? @(rf/subscribe [:slb-form/error])))))

(deftest error-sub-after-error
  (testing "error armazena mensagem de erro"
    (rf/dispatch-sync [:slb-form/open])
    (rf/dispatch-sync [:slb-form/submit-error {:status 400 :message "Erro"}])
    (is (= {:status 400 :message "Erro"}
           @(rf/subscribe [:slb-form/error])))))
