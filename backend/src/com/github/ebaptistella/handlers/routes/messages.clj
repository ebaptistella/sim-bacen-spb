(ns com.github.ebaptistella.handlers.routes.messages
  (:require [com.github.ebaptistella.infrastructure.http-server.messages :as http-server.messages]))

(def routes
  #{["/api/v1/messages"
     :get
     http-server.messages/list-messages
     :route-name :list-messages]
    ["/api/v1/messages/:id/respond"
     :post
     http-server.messages/handle-respond
     :route-name :respond-message]
    ["/api/v1/messages/:id"
     :get
     http-server.messages/get-message
     :route-name :get-message]
    ["/api/v1/test/inject-message"
     :post
     http-server.messages/test-inject-message
     :route-name :test-inject-message]})
