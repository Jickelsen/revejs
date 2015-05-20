(ns revejs.component)

(defrecord Transform [x y a w h])
(defrecord Velocity [x y a])
(defrecord Max_Thrust [max-thrust])
(defrecord Max_Velocity [max-velocity])
(defrecord TT [tt])
(defrecord Ship [])
(defrecord Ship1 [])
(defrecord Ship2 [])
(defrecord Engine [throttle thrust max-velocity])
(defrecord Cannon [firing fire-timestamp fire-delay])
(defrecord Score1 [score])
(defrecord Score2 [score])
(defrecord Bullet [life-timestamp life-time])
(defrecord Bullet1 [])
(defrecord Bullet2 [])
(defrecord Renderer [renderer])
