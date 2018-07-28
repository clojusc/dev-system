(ns clojusc.system-manager.system.impl.state
  "Generic state tracking for a Componenet system."
  (:require
    [com.stuartsierra.component :as component]
    [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   State Atom   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def default-state
  {:status :stopped
   :system nil
   :init-fn 'identity
   :refresh-fn 'identity
   :ns ""})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   System State Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord StateTracker [
  status
  system
  init-fn
  refresh-fn
  ns])

(defn get-state
  [this]
  this)

(defn set-state
  [this new-state]
  (merge this new-state))

(defn get-status
  [this]
  (:status (get-state this)))

(defn set-status
  [this value]
  (set-state this (assoc (get-state this) :status value)))

(defn get-system
  [this]
  (:system (get-state this)))

(defn set-system
  [this value]
  (set-state this (assoc (get-state this) :system value)))

(defn get-system-init-fn
  [this]
  (:init-fn (get-state this)))

(defn set-system-init-fn
  [this fq-fn]
  (set-state this (assoc (get-state this) :init-fn fq-fn)))

(defn get-system-ns
  [this]
  (:ns (get-state this)))

(defn set-system-ns
  [this an-ns]
  (set-state this (assoc (get-state this) :ns an-ns)))

(def behaviour
  {:get-state get-state
   :set-state set-state
   :get-status get-status
   :set-status set-status
   :get-system get-system
   :set-system set-system
   :get-system-init-fn get-system-init-fn
   :set-system-init-fn set-system-init-fn
   :get-system-ns get-system-ns
   :set-system-ns set-system-ns})

(defn create-tracker
  ([]
    (create-state-tracker {}))
  ([options]
    (map->StateTracker (merge default-state options))))
