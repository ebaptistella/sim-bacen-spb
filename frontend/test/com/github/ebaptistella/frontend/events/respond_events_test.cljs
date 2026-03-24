(ns com.github.ebaptistella.frontend.events.respond-events-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [com.github.ebaptistella.frontend.events]
            [com.github.ebaptistella.frontend.events.messages-events]
            [com.github.ebaptistella.frontend.events.respond-events]
            [com.github.ebaptistella.frontend.subs]
            [re-frame.core :as rf]))

(use-fixtures :each
  {:before (fn [] (rf/dispatch-sync [:initialize-db]))
   :after  (fn [] (rf/clear-subscription-cache!))})

(deftest open-modal-resets-state
  (testing "open-modal limpa qualquer estado anterior e abre modal"
    (rf/dispatch-sync [:respond/set-response-type :reject])
    (rf/dispatch-sync [:respond/set-motivo "algum motivo"])
    (rf/dispatch-sync [:respond/open-modal])
    (is (true? @(rf/subscribe [:respond/modal-visible?])))
    (is (nil? @(rf/subscribe [:respond/response-type])))
    (is (nil? @(rf/subscribe [:respond/motivo])))))

(deftest set-response-type-stores-value
  (testing "set-response-type armazena o tipo no db"
    (rf/dispatch-sync [:respond/open-modal])
    (rf/dispatch-sync [:respond/set-response-type :accept])
    (is (= :accept @(rf/subscribe [:respond/response-type])))))

(deftest set-response-type-clears-motivo
  (testing "set-response-type limpa motivo ao trocar de opção"
    (rf/dispatch-sync [:respond/open-modal])
    (rf/dispatch-sync [:respond/set-response-type :reject])
    (rf/dispatch-sync [:respond/set-motivo "motivo anterior"])
    (rf/dispatch-sync [:respond/set-response-type :accept])
    (is (nil? @(rf/subscribe [:respond/motivo])))))

(deftest set-motivo-stores-value
  (testing "set-motivo armazena o texto de rejeição"
    (rf/dispatch-sync [:respond/open-modal])
    (rf/dispatch-sync [:respond/set-motivo "Saldo insuficiente"])
    (is (= "Saldo insuficiente" @(rf/subscribe [:respond/motivo])))))

(deftest show-confirmation-transitions-screens
  (testing "show-confirmation fecha modal e abre confirmação"
    (rf/dispatch-sync [:respond/open-modal])
    (rf/dispatch-sync [:respond/show-confirmation])
    (is (false? @(rf/subscribe [:respond/modal-visible?])))
    (is (true? @(rf/subscribe [:respond/confirmation-visible?])))))

(deftest back-to-modal-returns-to-modal
  (testing "back-to-modal fecha confirmação e reabre modal"
    (rf/dispatch-sync [:respond/open-modal])
    (rf/dispatch-sync [:respond/show-confirmation])
    (rf/dispatch-sync [:respond/back-to-modal])
    (is (true? @(rf/subscribe [:respond/modal-visible?])))
    (is (false? @(rf/subscribe [:respond/confirmation-visible?])))))

(deftest close-modal-clears-all-state
  (testing "close-modal fecha tudo e limpa estado"
    (rf/dispatch-sync [:respond/open-modal])
    (rf/dispatch-sync [:respond/set-response-type :reject])
    (rf/dispatch-sync [:respond/set-motivo "motivo"])
    (rf/dispatch-sync [:respond/close-modal])
    (is (false? @(rf/subscribe [:respond/modal-visible?])))
    (is (nil? @(rf/subscribe [:respond/response-type])))
    (is (nil? @(rf/subscribe [:respond/motivo])))))

(deftest submit-success-closes-all-modals
  (testing "submit-success fecha modais e limpa estado"
    (rf/dispatch-sync [:respond/open-modal])
    (rf/dispatch-sync [:respond/set-response-type :accept])
    (rf/dispatch-sync [:respond/show-confirmation])
    (rf/dispatch-sync [:respond/submit-success])
    (is (false? @(rf/subscribe [:respond/modal-visible?])))
    (is (false? @(rf/subscribe [:respond/confirmation-visible?])))
    (is (nil? @(rf/subscribe [:respond/response-type])))))

(deftest submit-error-stores-error
  (testing "submit-error armazena o erro sem fechar modal"
    (rf/dispatch-sync [:respond/open-modal])
    (rf/dispatch-sync [:respond/show-confirmation])
    (rf/dispatch-sync [:respond/submit-error {:status 409}])
    (is (= {:status 409} @(rf/subscribe [:respond/error])))
    (is (true? @(rf/subscribe [:respond/confirmation-visible?])))))
