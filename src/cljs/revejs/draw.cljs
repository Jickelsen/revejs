(ns revejs.draw (:require [quil.core :as q :include-macros true]
            [revejs.state :refer [game-state ship1-history ship2-history tt]]
            [revejs.util :as u :refer [WIDTH HEIGHT]]
            [brute.entity :as e]
            [brute.system :as s]
            [revejs.component :refer [Ship Ship1 Ship2 Position Velocity TT Renderer Max_Thrust Max_Velocity Bullet Bullet1 Bullet2]]
            )
  )

(defn draw-state [state _]
  (do 
    (if (:tt (e/get-component state (first (e/get-all-entities-with-component state Ship1)) TT)) 
      (q/background 155 165 55)
      (q/background 225 125 75))
    (doseq [renderable (e/get-all-entities-with-component state Renderer)]
      (let [{x :x y :y a :a} (e/get-component state renderable Position)]
        (q/push-matrix)
        (q/translate x y)
        (q/rotate (q/radians a) )
        (cond 
          (e/get-component state renderable Ship1)
          ((:renderer (e/get-component state renderable Renderer)) 1)
          (e/get-component state renderable Ship2)
          ((:renderer (e/get-component state renderable Renderer)) 2)
          (e/get-component state renderable Bullet1)
          ((:renderer (e/get-component state renderable Renderer)) 1)
          (e/get-component state renderable Bullet2)
          ((:renderer (e/get-component state renderable Renderer)) 2)
          :else ((:renderer (e/get-component state renderable Renderer))))
        (q/pop-matrix)))
    state))


