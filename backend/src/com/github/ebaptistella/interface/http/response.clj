(ns com.github.ebaptistella.interface.http.response
  (:require [schema.core :as s]))

(s/defn ok            :- {:status s/Int :body s/Any} [body]         {:status 200 :body body})
(s/defn created       :- {:status s/Int :body s/Any} [body]         {:status 201 :body body})
(s/defn accepted      :- {:status s/Int :body s/Any} [body]         {:status 202 :body body})
(s/defn no-content    :- {:status s/Int}             []             {:status 204})
(s/defn bad-request   :- {:status s/Int :body {:error s/Str}} [msg] {:status 400 :body {:error msg}})
(s/defn not-found     :- {:status s/Int :body {:error s/Str}} [msg] {:status 404 :body {:error msg}})
(s/defn conflict             :- {:status s/Int :body {:error s/Str}} [msg] {:status 409 :body {:error msg}})
(s/defn unprocessable-entity :- {:status s/Int :body {:error s/Str}} [msg] {:status 422 :body {:error msg}})
(s/defn internal-server-error :- {:status s/Int :body {:error s/Str}} [msg] {:status 500 :body {:error msg}})
