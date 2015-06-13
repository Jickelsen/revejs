(ns revejs.scoring
  (:require [revejs.ship :as ship]
            [revejs.util :as u]
            [revejs.state :refer [game-state ship1-history ship2-history]]
            [brute.entity :as e]
            [revejs.component :as c :refer [Score1 Score2 Ship Ship1 Ship2]]
           ))
(defn create-score [state player]
  (let [score e/create-entity]
    (cond
      (= player 1)
      (-> state
          (e/add-entity score)
          (e/add-component score (c/->Score1 0)))
      (= player 2)
      (-> state
          (e/add-entity score)
          (e/add-component score (c/->Score2 0)))
      )))

(defn score-hit-ship [st hit-ship]
  (if (e/get-component st hit-ship Ship1)
    (-> st
        (e/update-component (first (brute.entity/get-all-entities-with-component st Score2)) Score2 #(update-in % [:score]inc))
        (#(do (reset! ship1-history [])
             %))
        (e/kill-entity hit-ship)
      ((fn [st] (do 
                  (revejs.util/setTimeout (partial swap! game-state #(revejs.ship/create-ship % 1)) 500)
                  st))))
    (-> st
        (e/update-component (first (brute.entity/get-all-entities-with-component st Score1)) Score1 #(update-in % [:score]inc))
        (#(do (reset! ship2-history [])
             %))
        (e/kill-entity hit-ship)
      ((fn [st] (do 
                  (revejs.util/setTimeout (partial swap! game-state #(revejs.ship/create-ship % 2)) 500)
                  st))))))

        ;; (#((do
        ;;      (reset! ship2-history (last ship2-history)))
        ;;    (e/kill-entity % hit-ship)))
