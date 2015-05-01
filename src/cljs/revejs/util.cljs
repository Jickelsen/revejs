(ns revejs.util
  (:require [quil.core :as q :include-macros true]
            [brute.entity :as e]
            [brute.system :as s]
            [revejs.component :refer [Ship Ship1 Ship2 Position Velocity TT Renderer Max_Thrust Max_Velocity]]
            ))

(def WIDTH 500)
(def HEIGHT 500)
(def speed 1)
(def gravity 0.001)

(def FRAMERATE 60)

(defn rotate [position ang-vel]
  (-> position
      (assoc :a (+ (:a position) ang-vel)))
  )

(defn go-up [velocity]
  (let [{x :x y :y a :a} velocity]
    ;; (let [{:keys [x y a]} velocity]
    (-> velocity 
        (assoc :x x)
        (assoc :y (- speed))
        (assoc :a a))))
(defn go-down [velocity]
  (let [{x :x y :y a :a} velocity]
    (-> velocity 
        (assoc :x x)
        (assoc :y (+ speed))
        (assoc :a a))))
(defn go-left [velocity]
  (let [{x :x y :y a :a} velocity]
    (-> velocity 
        (assoc :x (- speed))
        (assoc :y y)
        (assoc :a a)
    )))
(defn go-right [velocity]
  (let [{x :x y :y a :a} velocity]
    (-> velocity 
        (assoc :x (+ speed))
        (assoc :y y)
        (assoc :a a)
    )))

(defn add-thrust [velocity position thrust]
  (let [a-rad (/ (* (:a position) Math/PI) 180)]
  ;; (let [a-rad (q/radians (:a position))]
    (-> velocity
        (assoc :x (+ (:x velocity)  (* thrust (Math/cos a-rad))))
        (assoc :y (+ (:y velocity)  (* thrust (Math/sin a-rad))))
        (assoc :a (:a velocity))))
  )

(defn move [state movable]
  (e/update-component state movable
                      Position #(merge-with + % (e/get-component state movable Velocity))))

(defn apply-gravity [state movable]
  (e/update-component state movable
                      Velocity (fn [x] (update-in x [:y] #(+ % gravity)))))

