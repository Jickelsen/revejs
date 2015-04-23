(ns revejs.core
  (:require [quil.core :as q :include-macros true]
            [revejs.update :as dynamic-update]
            [revejs.util :as u]
            [revejs.setup :refer [game-state ship1-history ship2-history tt WIDTH HEIGHT]]
            [revejs.bullet :as b]
            ;; [revejs.setup :as dynamic-setup]
            ;; [revejs.setup :as dynamic-setup]
            [brute.entity :as e]
            [revejs.component :as c :refer [Ship Ship1 Ship2 Position Velocity TT Renderer Max_Thrust Max_Velocity]]
            ;; [revejs.shot :as s]
            [brute.system :as s]
            [figwheel.client :as fw]
            )
  )

(enable-console-print!)

(def FRAMERATE 60)

(defn render-ship [variant]
  (q/fill 50 80 50)
  (q/rect -2 0 5 14)
  (cond 
    (= variant 1)
    (q/fill 100 220 100)
    (= variant 2)
    (q/fill 100 100 220)
    )
  (q/triangle 0 -10 25 0 0  10)
  (q/fill 30 100 30)
  (q/ellipse 8 0 8 8))

(defn create-ship
    "Creates a ship entity"
    [state]
    (let [ship1 (e/create-entity)
          ship2 (e/create-entity)
          center-x (-> WIDTH (/ 2) (Math/floor))
          center-y (-> HEIGHT (/ 2) (Math/floor))
          ship-size 20
          ship-center-x (- center-x (/ 200 2))
          ship-center-y (- center-y (/ 200 2))
          angle 0]
        (-> state
            (e/add-entity ship1)
            (e/add-entity ship2)
            (e/add-component ship1 (c/->Ship))
            (e/add-component ship2 (c/->Ship))
            (e/add-component ship1 (c/->Ship1))
            (e/add-component ship2 (c/->Ship2))
            (e/add-component ship1 (c/->Renderer render-ship))
            (e/add-component ship2 (c/->Renderer render-ship))
            (e/add-component ship1 (c/->Position (/ center-x 2) (/ center-y 2) angle))
            (e/add-component ship2 (c/->Position (* (/ center-x 2) 3) (* (/ center-y 2) 3) (+ 3.14 angle)))
            (e/add-component ship1 (c/->Velocity 0 0 0))
            (e/add-component ship2 (c/->Velocity 0 0 0))
            (e/add-component ship1 (c/->TT false))
            (e/add-component ship2 (c/->TT false))
            (e/add-component ship1 (c/->Max_Thrust 2))
            (e/add-component ship2 (c/->Max_Thrust 2))
            (e/add-component ship1 (c/->Max_Velocity 3))
            (e/add-component ship2 (c/->Max_Velocity 3))
            )))

(defn- start
    "Create all the initial entities with their components"
    [system]
  ;; (s/create-shot game-state)
    (create-ship system)
    )

(defn create-systems
    "register all the system functions"
    [system]
    (-> system
        (s/add-system-fn dynamic-update/update-state)
        (s/add-system-fn dynamic-update/draw-state)
        (s/add-system-fn b/process-one-game-tick)
    ))

(defn state-reset []
  (do
    (reset! ship1-history [])
    (reset! ship2-history [])
     (-> (e/create-system)
         (start)
         (create-systems)
         (as-> s (reset! game-state s)))))

(defn setup []
  (do
    (println "Started")
    (state-reset)
    (q/rect-mode :center)
    (q/frame-rate FRAMERATE)))

(defn iteration []
  (do
     (q/background 155 165 55)
     (reset! game-state (s/process-one-game-tick @game-state (/ 1000 FRAMERATE)))
     ))

(def keycodes
  "Keycodes that interest us. Taken from
  http://docs.closure-library.googlecode.com/git/closure_goog_events_keynames.js.source.html#line33"
  {37 :left
   38 :up
   39 :right
   40 :down
   16 :shift
   32 :space
   13 :enter
   10 :return
   87 :w
   65 :a
   68 :d
   83 :s
   81 :q})

(defn key-pressed []
  (println (q/key-code))
  (let [ship1 (first (e/get-all-entities-with-component @game-state Ship1))
        ship2 (first (e/get-all-entities-with-component @game-state Ship2))
        pos1 (e/get-component @game-state ship1 Position)
        pos2 (e/get-component @game-state ship2 Position)
        max1 (:max-thrust (e/get-component @game-state ship2 Max_Thrust))
        max2 (:max-thrust (e/get-component @game-state ship2 Max_Thrust))
        tt1 (:tt (e/get-component @game-state ship1 TT))
        tt2 (:tt (e/get-component @game-state ship2 TT))
        ]
    (do
      (if (not tt1)
        (cond
         (= (keycodes (q/key-code)) :w)
         (do 
           (reset! game-state (e/update-component @game-state ship1 Velocity u/add-thrust pos1 max1))
           (println (e/get-component @game-state ship1 Velocity))
           )
         (= (keycodes (q/key-code)) :s)
         (reset! game-state (e/update-component @game-state ship1 Velocity u/add-thrust pos1 (- 0 max1)))
         (= (keycodes (q/key-code)) :a)
         (reset! game-state (e/update-component @game-state ship1 Position u/rotate -10))
         (= (keycodes (q/key-code)) :d)
         (reset! game-state (e/update-component @game-state ship1 Position u/rotate 10))
         (= (keycodes (q/key-code)) :space)
         (swap! game-state #(b/fire % ship1))))
      (if (not tt2)
        (cond
         (= (keycodes (q/key-code)) :up)
         (reset! game-state (e/update-component @game-state ship2 Velocity u/add-thrust pos2 max2))
         (= (keycodes (q/key-code)) :down)
         (reset! game-state (e/update-component @game-state ship2 Velocity u/add-thrust pos2 (- 0 max2)))
         (= (keycodes (q/key-code)) :left)
         (reset! game-state (e/update-component @game-state ship2 Position u/rotate -10))
         (= (keycodes (q/key-code)) :right)
         (reset! game-state (e/update-component @game-state ship2 Position u/rotate 10))))
      (cond
       (= (keycodes (q/key-code)) :q)  
       (do
         (println (str "tt1 is " (:tt (e/get-component @game-state ship1 TT))))
         (reset! game-state (e/update-component @game-state ship1 TT #(assoc % :tt (not (:tt %))))))
       (= (keycodes (q/key-code)) :shift)  
       (reset! game-state (e/update-component @game-state ship2 TT #(assoc % :tt (not (:tt %)))))
       (or (= (keycodes (q/key-code)) :return)(= (keycodes (q/key-code)) :enter)) 
       (do (println "Reset state")
           (state-reset))))))

(q/defsketch revejs
  :title "A game-like simulation built with Quil, with game-state undo"
  :size [WIDTH HEIGHT]
  :host "canvas-id"
  :setup setup 
  :draw iteration
  :key-pressed key-pressed
  )

(fw/start
 {
  
  ;; configure a websocket url if you are using your own server
  ;; :websocket-url "ws://localhost:3449/figwheel-ws"

  ;; optional callback
  :on-jsload (fn [] (print "reloaded"))

  ;; The heads up display is enabled by default
  ;; to disable it: 
  ;; :heads-up-display false

  ;; when the compiler emits warnings figwheel
  ;; blocks the loading of files.
  ;; To disable this behavior:
  ;; :load-warninged-code true

  ;; if figwheel is watching more than one build
  ;; it can be helpful to specify a build id for
  ;; the client to focus on
  ;; :build-id "example"
})
