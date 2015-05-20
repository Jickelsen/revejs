(ns revejs.draw
  (:require [quil.core :as q :include-macros true]
            ;; [clojure.string :as str]
            [revejs.state :refer [game-state ship1-history ship2-history]]
            [revejs.util :as u :refer [WIDTH HEIGHT]]
            [brute.entity :as e]
            [brute.system :as s]
            [revejs.component :refer [Ship Ship1 Ship2 Transform Velocity TT Renderer Max_Thrust Max_Velocity Bullet Bullet1 Bullet2 Score1 Score2]]
            )
  )

(defn draw-state [state _]
  (do 
    (q/no-stroke)
    (q/background 169 174 176)
    (q/text-size 48) ;; Apparently specifying font and size in setup isn't enough for proper rendering
    (q/fill 221 72 147)
    (q/text (:score (e/get-component state (first (e/get-all-entities-with-component state Score1)) Score1)) (* WIDTH 0.05) (* HEIGHT 0.05) )
    (q/fill 37 157 196)
    (q/text (:score (e/get-component state (first (e/get-all-entities-with-component state Score2)) Score2)) (* WIDTH 0.95) (* HEIGHT 0.05))
    ;; (q/fill (q/blend-color (q/color 221 72 147) (q/color 37 157 196) :blend))
    (q/fill (q/color 221 72 147 20))
    (doseq [hist (take 50 (take-nth 4 (reverse @ship1-history)))]
      (let [{x :x y :y a :a w :w h :h} (:pos hist)]
        (q/ellipse x y (* 0.6 w) (* 0.6 h))
        )
      )
    (q/fill 37 157 196)
    (doseq [hist (take 50 (take-nth 4 (reverse @ship2-history)))]
      (let [{x :x y :y a :a w :w h :h} (:pos hist)]
        (q/push-matrix)
        (q/translate x y)
        (q/rotate (q/radians a))
        (q/rect 0 0 (* 0.6 w) (* 0.6 h))
        (q/pop-matrix)))
    (doseq [renderable (e/get-all-entities-with-component state Renderer)]
      (let [{x :x y :y a :a w :w h :h} (e/get-component state renderable Transform)]
        (q/push-matrix)
        (q/translate x y)
        (q/rotate (q/radians a))
        ((:renderer (e/get-component state renderable Renderer)))
        (q/pop-matrix)))
    state))


