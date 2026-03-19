(defproject com.github.ebaptistella/sim-bacen-spb "0.1.0-SNAPSHOT"
  :description "Simulador BACEN - Sistema de Pagamentos Brasileiro"
  :url "https://github.com/ebaptistella/sim-bacen-spb"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.12.2"]
                 ;; Override to avoid "update-vals/update-keys already refers to clojure.core" warnings (Clojure 1.11+)
                 [org.clojure/tools.analyzer "1.2.0"]
                 [com.stuartsierra/component "1.1.0"]
                 [ch.qos.logback/logback-classic "1.2.3" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.26"]
                 [org.slf4j/jcl-over-slf4j "1.7.26"]
                 [org.slf4j/log4j-over-slf4j "1.7.26"]
                 [io.pedestal/pedestal.service "0.5.8"]
                 [io.pedestal/pedestal.jetty "0.5.8"]
                 [prismatic/schema "1.4.1"]
                 [cheshire "5.11.0"]
                 ;; IBM MQ client
                 [com.ibm.mq/com.ibm.mq.allclient "9.4.0.0"]]
  :plugins [[com.github.clj-kondo/lein-clj-kondo "0.2.5"]
            [com.github.clojure-lsp/lein-clojure-lsp "2.0.13"]
            [lein-cljfmt "0.8.2"]
            [lein-nsorg "0.3.0"]]
  :clojure-lsp {:settings {:clean {:ns-inner-blocks-indentation :same-line}}}
  :clean-targets ^{:protect false} ["target"]
  :source-paths ["src"]
  :test-paths ["test/unit" "test/integration" "test/shared"]
  :resource-paths ["resources"]
  :main com.github.ebaptistella.main
  :aot [com.github.ebaptistella.main]
  :profiles {:dev {:dependencies [[io.pedestal/pedestal.service-tools "0.5.8"]
                                  [nubank/matcher-combinators "3.8.3"]
                                  [nubank/mockfn "0.7.0"]
                                  [nubank/state-flow "5.20.0"]]}
             :test {:resource-paths ["test/resources" "resources"]}
             :repl-auto {:repl-options {:init-ns com.github.ebaptistella.repl}}}
  :aliases {:repl ["with-profile" "+dev" "repl"]
            :repl-auto ["with-profile" "+dev,+repl-auto" "repl"]
            :run-dev ["trampoline" "run" "-m" "com.github.ebaptistella.main/-main"]
            :build ["do" ["clean"] ["uberjar"]]
            :clean-ns ["clojure-lsp" "clean-ns" "--dry"]
            :format ["clojure-lsp" "format" "--dry"]
            :diagnostics ["clojure-lsp" "diagnostics"]
            :clean-ns-fix ["clojure-lsp" "clean-ns"]
            :format-fix ["clojure-lsp" "format"]
            :cljfmt ["cljfmt" "check"]
            :cljfmt-fix ["cljfmt" "fix"]
            :nsorg-check ["nsorg"]
            :nsorg-fix ["nsorg" "--replace"]
            :kondo ["clj-kondo" "--lint" "src" "test"]
            :lint ["do" ["clean-ns"] ["format"] ["diagnostics"] ["cljfmt"] ["nsorg-check"] ["clj-kondo" "--lint" "src" "test"]]}
  :repl-options {:init-ns com.github.ebaptistella.main})
