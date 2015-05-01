(ns revejs.ship
(:require [quil.core :as q :include-macros true]
          [revejs.state :refer [game-state ship1-history ship2-history tt]]
          [revejs.util :as u :refer [WIDTH HEIGHT]]
          [brute.entity :as e]
          [brute.system :as s]
          [cljs-time.core :as t]
          [revejs.component :as c :refer [Ship Ship1 Ship2 Transform Velocity TT Renderer Max_Thrust Max_Velocity Cannon Bullet Bullet1 Bullet2]]
            ))

(def char-width 20)
(def char-height 20)
(def fire-delay 250)

(defn render-ship [w h variant]
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
            (e/add-component ship1 (c/->Transform (/ center-x 2) (/ center-y 2) angle char-width char-height))
            (e/add-component ship2 (c/->Transform (* (/ center-x 2) 3) (* (/ center-y 2) 3) (+ 180 angle) char-width char-height))
            (e/add-component ship1 (c/->Velocity 0 0 0))
            (e/add-component ship2 (c/->Velocity 0 0 0))
            (e/add-component ship1 (c/->Cannon (t/now) fire-delay))
            (e/add-component ship2 (c/->Cannon (t/now) fire-delay))
            (e/add-component ship1 (c/->TT false))
            (e/add-component ship2 (c/->TT false))
            (e/add-component ship1 (c/->Max_Thrust 2))
            (e/add-component ship2 (c/->Max_Thrust 2))
            (e/add-component ship1 (c/->Max_Velocity 3))
            (e/add-component ship2 (c/->Max_Velocity 3))
            )))

(defn bounds [state movable]
  (let [pos (e/get-component state movable Transform)
        vel (e/get-component state movable Velocity)
        abs-vel (Math/sqrt (Math/pow (:x vel) 2) (Math/pow (:y vel) 2))
        ;; max-vel (add-thrust vel pos (e/get-component state movable (:max-velocity Max_Velocity)))
        abs-max-vel (:max-velocity (e/get-component state movable Max_Velocity)) 
        max-thrust (:max-thrust (e/get-component state movable Max_Thrust)) 
        ;; null (println max-vel)
        ]
    (-> state
        (e/update-component movable
                            Velocity #(-> %
                                          (assoc :x (if (>= (:x pos) WIDTH)
                                                      0
                                                      (if (<= (:x pos) 0)
                                                        0
                                                        (if (< abs-max-vel abs-vel)
                                                          (* (:x %) (/ max-thrust abs-vel))
                                                          (:x %)))))
                                          (assoc :y (if (>= (:y pos) HEIGHT)
                                                      0
                                                      (if (<= (:y pos) 0)
                                                        0
                                                        (if (< abs-max-vel abs-vel)
                                                          (* (:y %) (/ max-thrust abs-vel))
                                                          (:y %)))))
                                          (assoc :a (:a %))))
        (e/update-component movable
                            Transform #(-> %
                                          (assoc :x (if (>= (:x pos) WIDTH)
                                                      WIDTH
                                                      (if (<= (:x pos) 0)
                                                        0
                                                        (:x %))))
                                          (assoc :y (if (>= (:y pos) HEIGHT)
                                                      WIDTH
                                                      (if (<= (:y pos) 0)
                                                        0
                                                        (:y %))))
                                          (assoc :a (:a %))))
        )
        ))

(defn game-tick [state _]
  (reduce (fn [sys ship]

            (if (not (:tt (e/get-component sys ship TT)))
              (-> sys
                  (u/apply-gravity ship)
                  (u/move ship)
                  (bounds ship)
                  )
              (if (e/get-component sys ship Ship1)  
                (if (> (count @ship1-history) 1)
                  (e/add-component sys ship (last (swap! ship1-history pop)))
                  (e/update-component sys ship TT #(assoc % :tt (not (:tt %))))
                  )
                (if (> (count @ship2-history) 1)
                  (e/add-component sys ship (last (swap! ship2-history pop)))
                  (e/update-component sys ship TT #(assoc % :tt (not (:tt %))))
                  ))))
          state (e/get-all-entities-with-component state Ship)))
