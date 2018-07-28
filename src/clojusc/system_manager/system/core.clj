(ns clojusc.system-manager.system.core
  "A mid-level state tracking and management API for a Component-based system."
  (:require
    [clojusc.system-manager.system.impl.management :as management]
    [clojusc.system-manager.system.impl.state :as state])
  (:import
    (clojusc.system_manager.system.impl.management StateManager)
    (clojusc.system_manager.system.impl.state StateTracker)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   System State API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defprotocol StateDataAPI
  (get-state [this])
  (set-state [this new-state])
  (get-status [this])
  (set-status [this value])
  (get-system [this])
  (set-system [this value])
  (get-system-init-fn [this])
  (set-system-init-fn [this value])
  (get-system-ns [this])
  (set-system-ns [this value]))

(extend StateTracker
        StateDataAPI
        state/behaviour)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   State Management API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defprotocol StateTransitionAPI
  (init [this] [this mode])
  (deinit [this])
  (start [this] [this mode])
  (stop [this])
  (restart [this] [this mode])
  (startup [this] [this mode])
  (shutdown [this]))

(extend StateManager
        StateTransitionAPI
        management/behaviour)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Constructors   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def create-state-tracker #'state/create-tracker)
(def create-state-manager #'management/create-manager)
