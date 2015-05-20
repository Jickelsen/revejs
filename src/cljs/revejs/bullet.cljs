(ns revejs.bullet
  (:require [quil.core :as q :include-macros true]
            [revejs.state :refer [game-state ship1-history ship2-history]]
            [revejs.util :as u :refer [WIDTH HEIGHT]]
            [revejs.scoring :as scoring]
            [brute.entity :as e]
            [brute.system :as s]
            [cljs-time.core :as t]
        [revejs.component :as c :refer [Ship Ship1 Ship2 Transform Velocity TT Renderer Max_Thrust Max_Velocity Cannon Bullet Bullet1 Bullet2]]
            ))

(def width 6)
(def height 6)
(def speed 3)
(def life-time 3000)

(defn render-bullet [w h variant]
  (q/fill 30 30 30)
  (cond 
    (= variant 1)
    (q/fill 221 72 147)
    (= variant 2)
    (q/fill 37 157 196)
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
        pos-x (+ (:x entity-pos) (* (* (:w entity-pos) 1.3) (Math/cos (q/radians (:a entity-pos)))))
        pos-y (+ (:y entity-pos) (* (* (:h entity-pos) 1.3) (Math/sin (q/radians (:a entity-pos)))))
        angle (:a entity-pos)
        cannon (e/get-component state entity Cannon)
        {firing? :firing fire-timestamp :fire-timestamp fire-delay :fire-delay} cannon  
        ]
    (if (and firing? (>= (t/in-millis (t/interval fire-timestamp (t/now))) fire-delay))
      (-> state
          (e/add-entity bullet)
          (e/add-component bullet (c/->Bullet (t/now) life-time))
          (e/add-component bullet (c/->Transform pos-x pos-y (:a entity-pos) size-x size-y))
          (e/add-component bullet (u/add-thrust (c/->Velocity vel-x vel-y 0) entity-pos speed))
          (#(cond
               (e/get-component % entity Ship1)
               (-> %
                   (e/add-component bullet (c/->Bullet1))
                   (e/add-component bullet (c/->Renderer (partial render-bullet size-x size-y 1))))
               (e/get-component % entity Ship2)
               (-> %
                   (e/add-component bullet (c/->Bullet2))
                   (e/add-component bullet (c/->Renderer (partial render-bullet size-x size-y 2)))))
               )
          (e/add-component entity (c/->Cannon firing? (t/now) fire-delay))
          )
      state)))

(defn life-cycle [state bullet]
  (let [{life-timestamp :life-timestamp life-time :life-time} (e/get-component state bullet Bullet)]
    (if (> (t/in-millis (t/interval life-timestamp (t/now))) life-time)
      (e/kill-entity state bullet)
      state)))

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
  (reduce (fn [st ship]
            (if
              (u/collides-with (e/get-component st bullet Transform) (e/get-component st ship Transform))
              (-> st 
                  (e/kill-entity bullet)
                  (scoring/score-hit-ship ship)
                  )
              st))
          state (e/get-all-entities-with-component state Ship)))

(defn start-firing [state entity]
  (let [cannon (e/get-component state entity Cannon)]
    (e/add-component state entity (assoc cannon :firing true) )))

(defn stop-firing [state entity]
  (let [cannon (e/get-component state entity Cannon)]
    (e/add-component state entity (assoc cannon :firing false) )))

(defn- handle-firing [state]
  (reduce (fn [st cannon]
            (-> st
                (fire cannon))
            )
          state (e/get-all-entities-with-component state Cannon)))

(defn- handle-bullets [state]
  (reduce (fn [st bullet]
            (-> st 
                (life-cycle bullet)
                (u/move bullet)
                (bounds bullet)
                (collide bullet)
                )
            )
          state (e/get-all-entities-with-component state Bullet)))

(defn game-tick [state _]
  (-> state
      (handle-bullets)
      (handle-firing)
      ))
  
