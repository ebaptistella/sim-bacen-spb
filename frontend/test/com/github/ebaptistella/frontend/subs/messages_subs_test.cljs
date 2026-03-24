(ns com.github.ebaptistella.frontend.subs.messages-subs-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [com.github.ebaptistella.frontend.events]
            [com.github.ebaptistella.frontend.events.messages-events]
            [com.github.ebaptistella.frontend.subs]
            [re-frame.core :as rf]))

(use-fixtures :each
  {:before (fn [] (rf/dispatch-sync [:initialize-db]))
   :after  (fn [] (rf/clear-subscription-cache!))})

(deftest loading-sub-reflects-state
  (testing ":messages/loading? reflete estado do db"
    (is (false? @(rf/subscribe [:messages/loading?])))
    (rf/dispatch-sync [:messages/fetch-success {:messages [] :total 0}])
    (is (false? @(rf/subscribe [:messages/loading?])))))

(deftest selected-message-returns-correct-item
  (testing ":messages/selected-message retorna a mensagem com o id selecionado"
    (let [msgs [{:id "x1" :type "STR0008" :status :pending}
                {:id "x2" :type "STR0008" :status :responded}]]
      (rf/dispatch-sync [:messages/fetch-success {:messages msgs :total 2}])
      (rf/dispatch-sync [:messages/select-message "x2"])
      (is (= {:id "x2" :type "STR0008" :status :responded}
             @(rf/subscribe [:messages/selected-message]))))))

(deftest selected-message-returns-nil-when-no-selection
  (testing ":messages/selected-message retorna nil quando nada selecionado"
    (is (nil? @(rf/subscribe [:messages/selected-message])))))

(deftest selected-message-returns-nil-for-unknown-id
  (testing ":messages/selected-message retorna nil se selected-id não existir na lista"
    (rf/dispatch-sync [:messages/fetch-success {:messages [{:id "a"}] :total 1}])
    (rf/dispatch-sync [:messages/select-message "id-inexistente"])
    (is (nil? @(rf/subscribe [:messages/selected-message])))))

(deftest list-sub-returns-all-messages
  (testing ":messages/list retorna a lista completa"
    (let [msgs [{:id "1"} {:id "2"} {:id "3"}]]
      (rf/dispatch-sync [:messages/fetch-success {:messages msgs :total 3}])
      (is (= msgs @(rf/subscribe [:messages/list]))))))

(deftest no-duplicates-after-multiple-fetches
  (testing "múltiplos fetch-success não duplicam mensagens (lista é substituída)"
    (let [msgs [{:id "1"} {:id "2"}]]
      (rf/dispatch-sync [:messages/fetch-success {:messages msgs :total 2}])
      (rf/dispatch-sync [:messages/fetch-success {:messages msgs :total 2}])
      (is (= 2 (count @(rf/subscribe [:messages/list])))))))
