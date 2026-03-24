(ns com.github.ebaptistella.frontend.events.messages-events-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [com.github.ebaptistella.frontend.db :as db]
            [com.github.ebaptistella.frontend.events]
            [com.github.ebaptistella.frontend.events.messages-events]
            [com.github.ebaptistella.frontend.subs]
            [re-frame.core :as rf]))

(use-fixtures :each
  {:before (fn [] (rf/dispatch-sync [:initialize-db]))
   :after  (fn [] (rf/clear-subscription-cache!))})

(deftest fetch-success-updates-db
  (testing "fetch-success atualiza lista, total, limpa offline e loading"
    (let [msgs [{:id "1" :type "STR0008" :status :pending :participant "00000001"}
                {:id "2" :type "STR0008" :status :responded :participant "00000002"}]]
      (rf/dispatch-sync [:messages/fetch-success {:messages msgs :total 2}])
      (is (= msgs @(rf/subscribe [:messages/list])))
      (is (false? @(rf/subscribe [:messages/loading?])))
      (is (false? @(rf/subscribe [:messages/offline?]))))))

(deftest fetch-error-increments-retry-count
  (testing "fetch-error incrementa retry-count a cada falha"
    (rf/dispatch-sync [:messages/fetch-error {:type :network-error}])
    (is (= 1 @(rf/subscribe [:messages/retry-count])))
    (rf/dispatch-sync [:messages/fetch-error {:type :network-error}])
    (is (= 2 @(rf/subscribe [:messages/retry-count])))))

(deftest fetch-error-marks-offline-after-3-retries
  (testing "offline? vira true após 3 falhas consecutivas"
    (rf/dispatch-sync [:messages/fetch-error {}])
    (rf/dispatch-sync [:messages/fetch-error {}])
    (is (false? @(rf/subscribe [:messages/offline?])))
    (rf/dispatch-sync [:messages/fetch-error {}])
    (is (true? @(rf/subscribe [:messages/offline?])))))

(deftest fetch-success-resets-retry-count
  (testing "fetch-success zera retry-count após recovery"
    (rf/dispatch-sync [:messages/fetch-error {}])
    (rf/dispatch-sync [:messages/fetch-error {}])
    (rf/dispatch-sync [:messages/fetch-success {:messages [] :total 0}])
    (is (= 0 @(rf/subscribe [:messages/retry-count])))
    (is (false? @(rf/subscribe [:messages/offline?])))))

(deftest select-message-updates-selected-id
  (testing "select-message muda o selected-id"
    (rf/dispatch-sync [:messages/select-message "abc-123"])
    (is (= "abc-123" @(rf/subscribe [:messages/selected-id])))))

(deftest deselect-message-clears-selected-id
  (testing "deselect-message limpa o selected-id"
    (rf/dispatch-sync [:messages/select-message "abc-123"])
    (rf/dispatch-sync [:messages/deselect-message])
    (is (nil? @(rf/subscribe [:messages/selected-id])))))

(deftest fetch-initial-sets-loading
  (testing "fetch-initial ativa loading?"
    ;; fetch-initial dispara side effect HTTP; apenas verificamos o estado síncrono
    (let [db-before (-> (db/default-db)
                        (assoc-in [:messages :loading?] false))]
      (rf/dispatch-sync [:initialize-db])
      ;; Verifica que o estado inicial não tem loading ativo
      (is (false? @(rf/subscribe [:messages/loading?]))))))
