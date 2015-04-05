(defproject vis-world-map "0.1.0-SNAPSHOT"
  :description "World map in SVG"
  :url "http://github.com/pixell/cljs-world-map"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3126"]]

  :plugins [[lein-cljsbuild "1.0.5"]]

  :hooks [leiningen.cljsbuild]

  :repl-options {:nrepl-middleware [lighttable.nrepl.handler/lighttable-ops]}

  :cljsbuild {:builds [{:source-paths ["src/cljs"]
                        :compiler {:output-to     "resources/public/js/main.js"
                                   :output-dir    "resources/public/js"
                                   :source-map    "resources/public/js/main.js.map"
                                   :optimizations :whitespace
                                   :pretty-print  true}}]})
