(ns revejs.ship
(:require [quil.core :as q :include-macros true]
          [revejs.state :refer [game-state ship1-history ship2-history]]
          [revejs.util :as u :refer [WIDTH HEIGHT]]
          [brute.entity :as e]
          [brute.system :as s]
          [cljs-time.core :as t]
          [revejs.component :as c :refer [Ship Ship1 Ship2 Transform Velocity TT Renderer Max_Thrust Max_Velocity Engine Cannon Bullet Bullet1 Bullet2]]
            ))

(def char-width 20)
(def char-height 20)
(def fire-delay 200)

(defn render-ship [w h variant]
  (let [tt
        (cond (= variant 1)
              (:tt (e/get-component @game-state (first (e/get-all-entities-with-component @game-state Ship1)) TT))
              (= variant 2)
              (:tt (e/get-component @game-state (first (e/get-all-entities-with-component @game-state Ship2)) TT))
              )]
    (q/fill 255 255 255)
    (q/rect -10 0 5 14)
    (cond 
      (= variant 1)
      (do
        (if tt
          (q/fill 10 10 10)
          (q/fill 221 72 147))
        (q/triangle -8 -10 17 0 -8 10)
        (q/fill 247 229 51)
        (q/ellipse 0 0 8 8)
        )
      (= variant 2)
      (do
        (if tt
          (q/fill 10 10 10)
          (q/fill 37 157 196))
        (q/rect 4 0 25 15)
        ;; (q/triangle 0 -10 25 0 0 10)
        (q/fill 247 229 51)
        (q/ellipse 0 0 8 8)))))

(defn random-angle []
  (rand-int 360))
(defn random-coord [min max]
  (+ min (rand (- max min))))

(defn create-ship
    "Creates a ship entity"
    [state player]
    (let [ship (e/create-entity)
          center-x (-> WIDTH (/ 2) (Math/floor))
          center-y (-> HEIGHT (/ 2) (Math/floor))
          ship-size 20
          ship-center-x (- center-x (/ 200 2))
          ship-center-y (- center-y (/ 200 2))
          angle (random-angle)
          min-x (* 0.15 WIDTH)
          max-x (* 0.85 WIDTH)
          min-y (* 0.15 HEIGHT)
          max-y (* 0.85 HEIGHT)
          x-coord (random-coord min-x max-x)
          y-coord (random-coord min-y max-y)]
        (-> state
            (e/add-entity ship)
            (e/add-component ship (c/->Ship))
            (e/add-component ship (c/->Transform x-coord y-coord angle char-width char-height))
            (e/add-component ship (c/->Velocity 0 0 0))
            (e/add-component ship (c/->Engine 0 0.1 3))
            (e/add-component ship (c/->Cannon false (t/now) fire-delay))
            (e/add-component ship (c/->TT false))
            (e/add-component ship (c/->Max_Thrust 2))
            (e/add-component ship (c/->Max_Velocity 3))
            (#(cond
                (= player 1)
                (-> %
                    (e/add-component ship (c/->Ship1))
                    (e/add-component ship (c/->Renderer (partial render-ship char-width char-height 1))))
                (= player 2)
                (-> %
                    (e/add-component ship (c/->Ship2))
                    (e/add-component ship (c/->Renderer (partial render-ship char-width char-height 2)))))))))

(defn bounds [state movable]
  (let [pos (e/get-component state movable Transform)
        vel (e/get-component state movable Velocity)
        abs-vel (Math/sqrt (Math/pow (:x vel) 2) (Math/pow (:y vel) 2))
        max-vel-component (u/set-speed vel pos (:max-velocity (e/get-component state movable Max_Velocity)))
        abs-max-vel (:max-velocity (e/get-component state movable Max_Velocity)) 
        max-thrust (:max-thrust (e/get-component state movable Max_Thrust)) 
        ]
    (-> state
        (e/update-component movable
                Velocity #(-> %
                                (assoc :y (if (>= (:y pos) HEIGHT)
                                            0
                                            (if (<= (:y pos) 0)
                                            0
                                            (:y %))))
                                (assoc :x (if (>= (:x pos) WIDTH)
                                            0
                                            (if (<= (:x pos) 0)
                                            0
                                            (:x %))))
                                (assoc :a (:a %))))
        (e/update-component movable
                Transform #(-> %
                                (assoc :x (if (>= (:x pos) WIDTH)
                                            WIDTH
                                            (if (<= (:x pos) 0)
                                            0
                                            (:x %))))
                                (assoc :y (if (>= (:y pos) HEIGHT)
                                            HEIGHT
                                            (if (<= (:y pos) 0)
                                            0
                                            (:y %))))
                                (assoc :a (:a %))))
        )))

(defn update-vals [map vals f]
  (reduce #(update-in %1 [%2] f) map vals))

(defn run-engine [state ship]
  (let [pos (e/get-component state ship Transform)
        vel (e/get-component state ship Velocity)
        {throttle :throttle thrust :thrust max-velocity :max-velocity} (e/get-component state ship Engine)
        new-vel-component (u/add-thrust vel pos (* throttle thrust))
        new-abs-vel (Math/sqrt (+ (Math/pow (:x new-vel-component) 2) (Math/pow (:y new-vel-component) 2)))
        max-vel-component (u/clamp-velocity new-vel-component max-velocity)
        ;; null (println new-abs-vel)
        ]
    (if (<= new-abs-vel max-velocity)
      (e/add-component state ship new-vel-component)
      (e/add-component state ship max-vel-component))))

(defn game-tick [state _]
  (reduce (fn [sys ship]
            (if (not (:tt (e/get-component sys ship TT)))
              (-> sys
                  (run-engine ship)
                  (u/apply-gravity ship)
                  (u/move ship)
                  (bounds ship)
                  )
              (if (e/get-component sys ship Ship1)
                (if (> (count @ship1-history) 1)
                  (let [{pos :pos vel :vel}(last (swap! ship1-history pop))]
                    (-> sys
                        (e/add-component ship pos)
                        (e/add-component ship vel)
                        ))
                  (e/update-component sys ship TT #(assoc % :tt (not (:tt %))))
                  )
                (if (> (count @ship2-history) 1)
                  (let [{pos :pos vel :vel}(last (swap! ship2-history pop))]
                    (-> sys
                        (e/add-component ship pos)
                        (e/add-component ship vel)
                        ))
                  (e/update-component sys ship TT #(assoc % :tt (not (:tt %))))
                  ))))
          state (e/get-all-entities-with-component state Ship)))
