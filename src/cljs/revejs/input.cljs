(ns revejs.input
  (:require [quil.core :as q :include-macros true]
            [revejs.draw :as draw]
            [revejs.ship :as ship]
            [revejs.state :refer [game-state ship1-history ship2-history]]
            [revejs.util :as u :refer [WIDTH HEIGHT]]
            [revejs.setup :as setup]
            [revejs.bullet :as bullet]
            [brute.entity :as e]
            [revejs.component :as c :refer [Ship Ship1 Ship2 Transform Velocity TT Renderer Max_Thrust Max_Velocity]]
            [brute.system :as s]
            ))

(def keycodes
  "Keycodes that interest us. Taken from
  http://docs.closure-library.googlecode.com/git/closure_goog_events_keynames.js.source.html#line33"
  {37 :left
   38 :up
   39 :right
   40 :down
   16 :shift
   190 :.
   18 :alt
   157 :cmd
   32 :space
   13 :enter
   10 :return
   87 :w
   65 :a
   68 :d
   83 :s
   67 :c
   81 :q})

(defn key-pressed [event]
  ;; (println (q/key-code))
  (let [ship1 (first (e/get-all-entities-with-component @game-state Ship1))
        ship2 (first (e/get-all-entities-with-component @game-state Ship2))
        pos1 (e/get-component @game-state ship1 Transform)
        pos2 (e/get-component @game-state ship2 Transform)
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
           ;; (println (e/get-component @game-state ship1 Transform))
           )
         (= (keycodes (q/key-code)) :s)
         (reset! game-state (e/update-component @game-state ship1 Velocity u/add-thrust pos1 (- 0 max1)))
         (= (keycodes (q/key-code)) :a)
         (reset! game-state (e/update-component @game-state ship1 Velocity u/spin -3))
         (= (keycodes (q/key-code)) :d)
         (reset! game-state (e/update-component @game-state ship1 Velocity u/spin 3))
         (= (keycodes (q/key-code)) :c)
         (swap! game-state #(bullet/fire % ship1))
         ))
      (if (not tt2)
        (cond
         (= (keycodes (q/key-code)) :up)
         (reset! game-state (e/update-component @game-state ship2 Velocity u/add-thrust pos2 max2))
         (= (keycodes (q/key-code)) :down)
         (reset! game-state (e/update-component @game-state ship2 Velocity u/add-thrust pos2 (- 0 max2)))
         (= (keycodes (q/key-code)) :left)
         (reset! game-state (e/update-component @game-state ship2 Velocity u/spin -3))
         (= (keycodes (q/key-code)) :right)
         (reset! game-state (e/update-component @game-state ship2 Velocity u/spin 3))
         (= (keycodes (q/key-code)) :.)
         (swap! game-state #(bullet/fire % ship2))
         ))
      (cond
       (= (keycodes (q/key-code)) :q)  
       (do
         (println (str "tt1 is " (:tt (e/get-component @game-state ship1 TT))))
         (reset! game-state (e/update-component @game-state ship1 TT #(assoc % :tt (not (:tt %))))))
       (= (keycodes (q/key-code)) :shift)  
       (reset! game-state (e/update-component @game-state ship2 TT #(assoc % :tt (not (:tt %)))))
       (or (= (keycodes (q/key-code)) :return)(= (keycodes (q/key-code)) :enter)) 
       (do (println "Reset state")
           (setup/state-reset))))))

(defn key-released [event]
  ;; (println (q/key-code))
  (let [ship1 (first (e/get-all-entities-with-component @game-state Ship1))
        ship2 (first (e/get-all-entities-with-component @game-state Ship2))
        pos1 (e/get-component @game-state ship1 Transform)
        pos2 (e/get-component @game-state ship2 Transform)
        max1 (:max-thrust (e/get-component @game-state ship2 Max_Thrust))
        max2 (:max-thrust (e/get-component @game-state ship2 Max_Thrust))
        tt1 (:tt (e/get-component @game-state ship1 TT))
        tt2 (:tt (e/get-component @game-state ship2 TT))
        ]
    (do
      (if (not tt1)
        (cond
         (= (keycodes (q/key-code)) :a)
         (reset! game-state (e/update-component @game-state ship1 Velocity u/spin 0))
         (= (keycodes (q/key-code)) :d)
         (reset! game-state (e/update-component @game-state ship1 Velocity u/spin 0))
         ))
      (if (not tt2)
        (cond
         (= (keycodes (q/key-code)) :left)
         (reset! game-state (e/update-component @game-state ship2 Velocity u/spin 0))
         (= (keycodes (q/key-code)) :right)
         (reset! game-state (e/update-component @game-state ship2 Velocity u/spin 0))
         )))))
