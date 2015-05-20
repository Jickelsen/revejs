(ns revejs.setup
  (:require [quil.core :as q :include-macros true]
            [brute.entity :as e]
            [revejs.state :refer [game-state ship1-history ship2-history]]
            [revejs.draw :as draw]
            [revejs.ship :as ship]
            [revejs.scoring :as scoring]
            [revejs.bullet :as bullet]
            [revejs.util :as u :refer [WIDTH HEIGHT]]
            [revejs.component :as c :refer [Ship Ship1 Ship2 Transform Velocity]]
            ;; [revejs.shot :as s]
            [brute.system :as s]))

(enable-console-print!)

(def FRAMERATE 60)

(defn- start
    "Create all the initial entities with their components"
    [state]
    (-> state
        (ship/create-ship 1)
        (scoring/create-score 1)
        (ship/create-ship 2)
        (scoring/create-score 2))
    )

(defn setup-game-loop
    "register all the system functions"
    [system]
    (-> system
        (s/add-system-fn ship/game-tick)
        (s/add-system-fn bullet/game-tick)
        (s/add-system-fn draw/draw-state)
    ))

(defn state-reset []
  (do
    (reset! ship1-history [])
    (reset! ship2-history [])
     (-> (e/create-system)
         (start)
         (setup-game-loop)
         (as-> s (reset! game-state s)))))

(defn game-loop []
  (do
     (reset! game-state (s/process-one-game-tick @game-state (/ 1000 FRAMERATE)))
     ))

(defn setup []
  (do
    (println "The game has started")
    (state-reset)
    (q/no-smooth) ;; Doesn't seem to do anything
    (q/text-font (q/create-font "DejaVu Sans" 48 true))
    (q/hint :disable-depth-test)
    (q/rect-mode :center)
    (q/text-align :center)
    (q/frame-rate FRAMERATE)))

(add-watch game-state :history
  (fn [_ _ _ n]
   (let [ship1 (first (e/get-all-entities-with-component n Ship1))
        ship2 (first (e/get-all-entities-with-component n Ship2))
        pos1 (e/get-component n ship1 Transform)
        pos2 (e/get-component n ship2 Transform)
        vel1 (e/get-component n ship1 Velocity)
        vel2 (e/get-component n ship2 Velocity)
        ]
     (when-not (= (:pos  (last @ship1-history)) pos1)
       (swap! ship1-history conj {:pos pos1 :vel vel1}))
     (when-not (= (:pos  (last @ship2-history)) pos2)
       (swap! ship2-history conj {:pos pos2 :vel vel2}))))) 
