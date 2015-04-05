(ns revejs.update
  (:require [quil.core :as q :include-macros true]
            [revejs.setup :refer [tt]]
            [brute.entity :as e]
            [brute.system :as s]
            [revejs.component :refer [Ship Ship1 Ship2 Position Velocity Renderer]]
            )
  )

(def speed 1)
(def gravity 0.01)
(def char-width 50)
(def char-height 50)
(def WIDTH 500)
(def HEIGHT 500)

(defn move [state movable]
  (e/update-component state movable
                      Position #(merge-with + % (e/get-component state movable Velocity))))

(defn apply-gravity [state movable]
  (e/update-component state movable
                      Velocity (fn [x] (update-in x [:y] #(+ % gravity)))))

(defn bounds [state movable]
  (let [pos (e/get-component state movable Position)]
    (-> state
        (e/update-component movable
                            Velocity #(-> %
                                          (assoc :x (if (>= (:x pos) WIDTH)
                                                      0
                                                      (if (<= (:x pos) 0)
                                                        0
                                                        (:x %))))
                                          (assoc :y (if (>= (:y pos) HEIGHT)
                                                      0
                                                      (if (<= (:y pos) 0)
                                                        0
                                                        (:y %))))
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

(defn go-up [velocity]
  (let [{x :x y :y a :a} velocity]
    ;; (let [{:keys [x y a]} velocity]
    (-> velocity 
        (assoc :x x)
        (assoc :y (- y speed))
        (assoc :a a))))
(defn go-down [velocity]
  (let [{x :x y :y a :a} velocity]
    (-> velocity 
        (assoc :x x)
        (assoc :y (+ y speed))
        (assoc :a a))))
(defn go-left [velocity]
  (let [{x :x y :y a :a} velocity]
    (-> velocity 
        (assoc :x (- x speed))
        (assoc :y y)
        (assoc :a a)
    )))
(defn go-right [velocity]
  (let [{x :x y :y a :a} velocity]
    (-> velocity 
        (assoc :x (+ x speed))
        (assoc :y y)
        (assoc :a a)
    )))

(defn update-state [state delta]
  (reduce (fn [sys ship]
            (-> sys
                (apply-gravity ship)
                (move ship)
                (bounds ship)
                ))
          state (e/get-all-entities-with-component state Ship)))

(defn draw-state [state delta]
  (do 
    (if @tt
      (q/background 155 165 55)
      (q/background 225 125 75))
    (doseq [renderable (e/get-all-entities-with-component state Renderer)]
      (let [{x :x y :y a :a} (e/get-component state renderable Position)]
        (q/ellipse 20 20 20 20)
        (q/push-matrix)
        (q/translate x y)
        (q/rotate a)

        ((:renderer (e/get-component state renderable Renderer)))
        (q/pop-matrix)))
    state))


