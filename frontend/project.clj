(defproject sim-bacen-spb "0.1.0-SNAPSHOT"
  :description "Simulador BACEN - Sistema de Pagamentos Brasileiro - Frontend"
  :url "https://github.com/ebaptistella/sim-bacen-spb"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.12.2"]
                 [org.clojure/clojurescript "1.11.60"]
                 [reagent "1.2.0"]
                 [cljsjs/react "18.2.0-0"]
                 [cljsjs/react-dom "18.2.0-0"]]
  :plugins [[com.github.clj-kondo/lein-clj-kondo "0.2.5"]
            [com.github.clojure-lsp/lein-clojure-lsp "2.0.13"]
            [lein-cljfmt "0.8.2"]
            [lein-nsorg "0.3.0"]
            [lein-cljsbuild "1.1.8"]]
  :clojure-lsp {:settings {:clean {:ns-inner-blocks-indentation :same-line}}}
  :clean-targets ^{:protect false} ["resources/public/js" "target"]
  :source-paths ["src"]
  :resource-paths ["resources"]
  :cljsbuild {:builds
              {:app
               {:source-paths ["src"]
                :compiler {:output-to "resources/public/js/app.js"
                           :output-dir "resources/public/js/out"
                           :main com.github.ebaptistella.ui.core
                           :optimizations :none
                           :source-map true}}
               :prod
               {:source-paths ["src"]
                :compiler {:output-to "resources/public/js/app.js"
                           :output-dir "resources/public/js/out-prod"
                           :main com.github.ebaptistella.ui.core
                           :optimizations :advanced}}}}
  :aliases {:build     ["do" ["clean"] ["cljsbuild" "once" "prod"]]
            :dev       ["cljsbuild" "auto" "app"]
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
