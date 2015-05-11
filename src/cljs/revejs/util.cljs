(ns revejs.util
  (:require [quil.core :as q :include-macros true]
            [brute.entity :as e]
            [brute.system :as s]
            [revejs.component :refer [Ship Ship1 Ship2 Transform Velocity TT Renderer Max_Thrust Max_Velocity]]
            ))

(def WIDTH 500)
(def HEIGHT 500)
(def speed 1)
(def gravity 0.00)

(def FRAMERATE 60)

(defn rotate [position ang-delta]
  (-> position
      (assoc :a (+ (:a position) ang-delta)))
  )

(defn spin [velocity ang-vel]
  (-> velocity
      (assoc :a ang-vel))
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
        ))
  )

(defn set-speed [velocity position speed]
  (let [a-rad (/ (* (:a position) Math/PI) 180)]
    (-> velocity
        (assoc :x (* speed (Math/cos a-rad)))
        (assoc :y (* speed (Math/sin a-rad)))
        )
    ))

(defn clamp-velocity [velocity max-abs-vel]
  (let [a-rad (q/atan2 (:y velocity) (:x velocity))]
    (-> velocity
        (assoc :x (* max-abs-vel (Math/cos a-rad)))
        (assoc :y (* max-abs-vel (Math/sin a-rad)))
        )
    ))

(defn move [state movable]
  ;; (let [null (println (e/get-component state movable Velocity))])
  (e/update-component state movable
                      Transform #(merge-with + % (e/get-component state movable Velocity))))

(defn apply-gravity [state movable]
  (e/update-component state movable
                      Velocity (fn [x] (update-in x [:y] #(+ % gravity)))))

;; http://codeofrob.com/entries/learn-functional-programming-with-me---adding-collision-detection-to-the-game.html
(defn rect-right [rect] (+ (:x rect) (:w rect)))
(defn rect-bottom [rect] (+ (:y rect) (:h rect)))

(defn collides-with [one two]
    (cond (< (rect-right one) (:x two)) false
          (> (:x one) (rect-right two)) false
          (< (rect-bottom one) (:y two)) false
          (> (:y one) (rect-bottom two)) false
          :else true))
