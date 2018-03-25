(ns clojusc.dev.system.core
  "A generic development system management API."
  (:require
    [clojusc.dev.system.impl.management :as management]
    [clojusc.dev.system.impl.state :as state])
  (:import
    (clojusc.dev.system.impl.management StateManager)
    (clojusc.dev.system.impl.state StateTracker)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   System State API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defprotocol StateTrackerAPI
  (get-state [this])
  (set-state [this new-state])
  (get-status [this])
  (set-status [this value])
  (get-system [this])
  (set-system [this value])
  (get-system-ns [this])
  (set-system-ns [this value]))

(extend StateTracker
        StateTrackerAPI
        state/behaviour)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   State Management API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defprotocol StateManagementAPI
  (init [this] [this mode])
  (deinit [this])
  (start [this] [this mode])
  (stop [this])
  (restart [this] [this mode])
  (startup [this] [this mode])
  (shutdown [this]))

(extend StateManager
        StateManagementAPI
        management/behaviour)

(def create-state-manager #'management/create-state-manager)
