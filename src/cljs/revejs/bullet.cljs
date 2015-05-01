(ns revejs.bullet
  (:require [quil.core :as q :include-macros true]
            [revejs.state :refer [game-state ship1-history ship2-history tt ]]
            [revejs.util :as u :refer [WIDTH HEIGHT]]
            [brute.entity :as e]
            [brute.system :as s]
            [revejs.component :as c :refer [Ship Ship1 Ship2 Transform Velocity TT Renderer Max_Thrust Max_Velocity Bullet Bullet1 Bullet2]]
            ))

(def width 6)
(def height 6)
(def speed 2)

(defn render-bullet [w h variant]
  (q/fill 30 30 30)
  (cond 
    (= variant 1)
    (q/fill 100 220 100)
    (= variant 2)
    (q/fill 100 100 220)
    )
  (q/rect 0 0 w h)
  )

(defn fire
  "Add a bullet in front of the entity"
  [state entity]
  (let [bullet (e/create-entity)
        size-x width
        size-y height
        entity-pos (e/get-component state entity Transform)
        entity-vel (e/get-component state entity Velocity)
        vel-x (:x entity-vel)
        vel-y (:y entity-vel)
        pos-x (+ (:x entity-pos) (* (* (:w entity-pos) 2) (Math/cos (q/radians (:a entity-pos)))))
        pos-y (+ (:y entity-pos) (* (* (:h entity-pos) 2) (Math/sin (q/radians (:a entity-pos)))))
        angle (:a entity-pos)
        allegiance (cond
                     (e/get-component state entity Ship1)
                     (c/->Bullet1)
                     (e/get-component state entity Ship2)
                     (c/->Bullet2)
                     )
        ]
    (-> state
        (e/add-entity bullet)
        (e/add-component bullet (c/->Bullet))
        (e/add-component bullet allegiance)
        (e/add-component bullet (c/->Renderer render-bullet))
        (e/add-component bullet (c/->Transform pos-x pos-y (:a entity-pos) size-x size-y))
        (e/add-component bullet (u/add-thrust (c/->Velocity vel-x vel-y 0) entity-pos speed))
        )))

(defn bounds [state movable]
  (let [pos (e/get-component state movable Transform)
        vel (e/get-component state movable Velocity)
        abs-vel (Math/sqrt (Math/pow (:x vel) 2) (Math/pow (:y vel) 2))
        ;; max-vel (add-thrust vel pos (e/get-component state movable (:max-velocity Max_Velocity)))
        ;; null (println max-vel)
        ]
    (cond-> state
      (>= (:x pos) WIDTH)
      (e/kill-entity movable)
      (<= (:x pos) 0)
      (e/kill-entity movable)
      (>= (:y pos) HEIGHT)
      (e/kill-entity movable)
      (<= (:y pos) 0)
      (e/kill-entity movable))))

(defn collide [state bullet]
  (reduce (fn [sys ship]
            (if
              (u/collides-with (e/get-component sys bullet Transform) (e/get-component sys ship Transform))
              (e/kill-entity sys bullet)
              sys))
          state (e/get-all-entities-with-component state Ship)))

(defn game-tick [state _]
  (reduce (fn [sys bullet]
            (-> sys
                (u/move bullet)
                (bounds bullet)
                (collide bullet)
                )
            )
          state (e/get-all-entities-with-component state Bullet)))
