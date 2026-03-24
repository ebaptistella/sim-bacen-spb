(defproject sim-bacen-spb "0.1.0-SNAPSHOT"
  :description "Simulador BACEN - Sistema de Pagamentos Brasileiro - Frontend"
  :url "https://github.com/ebaptistella/sim-bacen-spb"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.12.2"]
                 [org.clojure/core.async "1.7.701"]
                 [reagent "1.2.0"]
                 [re-frame "1.4.3"]
                 [thheller/shadow-cljs "2.28.20"]]
  :plugins [[com.github.clj-kondo/lein-clj-kondo "0.2.5"]
            [com.github.clojure-lsp/lein-clojure-lsp "2.0.13"]
            [lein-cljfmt "0.8.2"]
            [lein-nsorg "0.3.0"]
            [lein-shadow "0.4.1"]]
  :clojure-lsp {:settings {:clean {:ns-inner-blocks-indentation :same-line}}}
  :clean-targets ^{:protect false} ["resources/public/js" "target" "node_modules/.cache"]
  :source-paths ["src" "test"]
  :resource-paths ["resources"]
  :shadow-cljs {:builds
                {:app  {:target    :browser
                        :output-dir "resources/public/js"
                        :asset-path "/js"
                        :modules   {:main {:init-fn com.github.ebaptistella.frontend.core/init}}}
                 :test {:target    :node-test
                        :output-to "target/test/test.js"
                        :ns-regexp "-test$"}}}
  :aliases {:build     ["do" ["clean"] ["shadow" "release" "app"]]
            :dev       ["shadow" "watch" "app"]
            :compile   ["shadow" "compile" "app"]
            :test      ["shadow" "test" "test"]
            :clean-ns  ["clojure-lsp" "clean-ns" "--dry"]
            :format    ["clojure-lsp" "format" "--dry"]
            :clean-ns-fix ["clojure-lsp" "clean-ns"]
            :format-fix ["clojure-lsp" "format"]
            :cljfmt    ["cljfmt" "check"]
            :cljfmt-fix ["cljfmt" "fix"]
            :nsorg-check ["nsorg"]
            :nsorg-fix ["nsorg" "--replace"]
            :kondo     ["clj-kondo" "--lint" "src"]
            :lint      ["do" ["clean-ns"] ["format"] ["cljfmt"] ["nsorg-check"] ["clj-kondo" "--lint" "src"]]})
