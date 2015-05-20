(defproject revejs "0.1.0-SNAPSHOT"
  :description "A game-like simulation built with Quil, with game-state undo."
  :url "http://www.rakethopp.se"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3211"]
                 [quil "2.2.5"]
                 [brute "0.3.0"]
                 [figwheel "0.2.6"]
                 [figwheel-sidecar "0.2.6"]
                 [com.andrewmcveigh/cljs-time "0.3.4"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]

  :plugins [[lein-cljsbuild "1.0.5" ]
            [lein-figwheel "0.2.6" :exclusions [org.clojure/clojure]]
            [cider/cider-nrepl "0.9.0-SNAPSHOT"]
            [refactor-nrepl "1.0.4"]]

  :source-paths ["src/clj"]
  :clean-targets ^{:protect false} ["resources/public/js/compiled"]
  :global-vars {*print-length* 100}
  
  :prep-tasks [["cljx" "once"] "javac" "compile"]
  :profiles {:dev {:plugins [[com.keminglabs/cljx "0.6.0"]]}}
  :cljx {:builds [{:source-paths ["cljx/"]
                   :output-path "target/classes"
                   :rules :clj}
                  {:source-paths ["cljx/"]
                   :output-path "target/generated-cljs"
                   :rules :cljs}]}
  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src/cljs" "dev_src" "target/generated-cljs"]
              :compiler {:output-to "resources/public/js/compiled/revejs.js"
                         :output-dir "resources/public/js/compiled/out"
                         :optimizations :none
                         :main revejs.core
                         :asset-path "js/compiled/out"
                         :source-map false
                         :source-map-timestamp true
                         :cache-analysis true }}
             ]}

  :figwheel {
             :http-server-root "public" ;; default and assumes "resources" 
             :server-port 3449 ;; default
             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             :nrepl-port 7888

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is simple ring servers, if this
             ;; doesn't work for you just run your own server :)
             ;; :ring-handler hello_world.server/handler

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log" 
             })
