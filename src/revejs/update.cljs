(ns revejs.update
  (:require [quil.core :as q :include-macros true]
            [revejs.util :as u]
            [revejs.setup :refer [game-state ship1-history ship2-history tt WIDTH HEIGHT]]
            [brute.entity :as e]
            [brute.system :as s]
            [revejs.component :refer [Ship Ship1 Ship2 Position Velocity TT Renderer Max_Thrust Max_Velocity Bullet Bullet1 Bullet2]]
            )
  )

(def char-width 50)
(def char-height 50)

(defn bounds [state movable]
  (let [pos (e/get-component state movable Position)
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
                            Position #(-> %
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

(defn update-state [state _]
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

(defn draw-state [state _]
  (do 
    (if (:tt (e/get-component state (first (e/get-all-entities-with-component state Ship1)) TT)) 
      (q/background 155 165 55)
      (q/background 225 125 75))
    (doseq [renderable (e/get-all-entities-with-component state Renderer)]
      (let [{x :x y :y a :a} (e/get-component state renderable Position)]
        (q/ellipse 20 20 20 20)
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


