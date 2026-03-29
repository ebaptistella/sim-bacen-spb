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
     :route-name :test-inject-message]
    ["/api/v1/messages/outbound"
     :post
     http-server.messages/handle-outbound
     :route-name :outbound-message]

    ;; 33 ingest endpoints for STR messages
    ["/api/v1/str/str0001"
     :post
     http-server.messages/ingest-str0001
     :route-name :ingest-str0001]
    ["/api/v1/str/str0003"
     :post
     http-server.messages/ingest-str0003
     :route-name :ingest-str0003]
    ["/api/v1/str/str0004"
     :post
     http-server.messages/ingest-str0004
     :route-name :ingest-str0004]
    ["/api/v1/str/str0005"
     :post
     http-server.messages/ingest-str0005
     :route-name :ingest-str0005]
    ["/api/v1/str/str0006"
     :post
     http-server.messages/ingest-str0006
     :route-name :ingest-str0006]
    ["/api/v1/str/str0007"
     :post
     http-server.messages/ingest-str0007
     :route-name :ingest-str0007]
    ["/api/v1/str/str0008"
     :post
     http-server.messages/ingest-str0008
     :route-name :ingest-str0008]
    ["/api/v1/str/str0010"
     :post
     http-server.messages/ingest-str0010
     :route-name :ingest-str0010]
    ["/api/v1/str/str0011"
     :post
     http-server.messages/ingest-str0011
     :route-name :ingest-str0011]
    ["/api/v1/str/str0012"
     :post
     http-server.messages/ingest-str0012
     :route-name :ingest-str0012]
    ["/api/v1/str/str0013"
     :post
     http-server.messages/ingest-str0013
     :route-name :ingest-str0013]
    ["/api/v1/str/str0014"
     :post
     http-server.messages/ingest-str0014
     :route-name :ingest-str0014]
    ["/api/v1/str/str0020"
     :post
     http-server.messages/ingest-str0020
     :route-name :ingest-str0020]
    ["/api/v1/str/str0021"
     :post
     http-server.messages/ingest-str0021
     :route-name :ingest-str0021]
    ["/api/v1/str/str0022"
     :post
     http-server.messages/ingest-str0022
     :route-name :ingest-str0022]
    ["/api/v1/str/str0025"
     :post
     http-server.messages/ingest-str0025
     :route-name :ingest-str0025]
    ["/api/v1/str/str0026"
     :post
     http-server.messages/ingest-str0026
     :route-name :ingest-str0026]
    ["/api/v1/str/str0029"
     :post
     http-server.messages/ingest-str0029
     :route-name :ingest-str0029]
    ["/api/v1/str/str0033"
     :post
     http-server.messages/ingest-str0033
     :route-name :ingest-str0033]
    ["/api/v1/str/str0034"
     :post
     http-server.messages/ingest-str0034
     :route-name :ingest-str0034]
    ["/api/v1/str/str0035"
     :post
     http-server.messages/ingest-str0035
     :route-name :ingest-str0035]
    ["/api/v1/str/str0037"
     :post
     http-server.messages/ingest-str0037
     :route-name :ingest-str0037]
    ["/api/v1/str/str0039"
     :post
     http-server.messages/ingest-str0039
     :route-name :ingest-str0039]
    ["/api/v1/str/str0040"
     :post
     http-server.messages/ingest-str0040
     :route-name :ingest-str0040]
    ["/api/v1/str/str0041"
     :post
     http-server.messages/ingest-str0041
     :route-name :ingest-str0041]
    ["/api/v1/str/str0043"
     :post
     http-server.messages/ingest-str0043
     :route-name :ingest-str0043]
    ["/api/v1/str/str0044"
     :post
     http-server.messages/ingest-str0044
     :route-name :ingest-str0044]
    ["/api/v1/str/str0045"
     :post
     http-server.messages/ingest-str0045
     :route-name :ingest-str0045]
    ["/api/v1/str/str0046"
     :post
     http-server.messages/ingest-str0046
     :route-name :ingest-str0046]
    ["/api/v1/str/str0047"
     :post
     http-server.messages/ingest-str0047
     :route-name :ingest-str0047]
    ["/api/v1/str/str0048"
     :post
     http-server.messages/ingest-str0048
     :route-name :ingest-str0048]
    ["/api/v1/str/str0051"
     :post
     http-server.messages/ingest-str0051
     :route-name :ingest-str0051]
    ["/api/v1/str/str0052"
     :post
     http-server.messages/ingest-str0052
     :route-name :ingest-str0052]})
