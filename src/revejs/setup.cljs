(ns revejs.setup
  (:require [brute.entity :as e]
            [revejs.component :as c :refer [Ship Ship1 Ship2 Position Velocity]]
            ;; [revejs.shot :as s]
            [brute.system :as s]))
(def game-state (atom 0))

(def game-history (atom [@game-state]))
(def ship1-history (atom []))
(def ship2-history (atom []))

(def tt (atom false))

(add-watch game-state :history
  (fn [_ _ _ n]
   (let [ship1 (first (e/get-all-entities-with-component n Ship1))
        ship2 (first (e/get-all-entities-with-component n Ship2))
        pos1 (e/get-component n ship1 Position)
        pos2 (e/get-component n ship2 Position)
        ]
     (when-not (= (last @ship1-history) pos1)
       (swap! ship1-history conj pos1))
     (when-not (= (last @ship2-history) pos2)
       (swap! ship2-history conj pos2))))) 
