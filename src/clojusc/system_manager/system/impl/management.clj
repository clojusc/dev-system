(ns clojusc.system-manager.system.impl.management
  "Generic state management for a Component system."
  (:require
    [clojusc.system-manager.system.impl.state :as state]
    [com.stuartsierra.component :as component]
    [taoensso.timbre :as log])
  (:import
    (clojure.lang Namespace)
    (clojure.lang Symbol)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Transition Vars   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def valid-stop-transitions #{:started :running})
(def invalid-init-transitions #{:initialized :started :running})
(def invalid-deinit-transitions #{:started :running})
(def invalid-start-transitions #{:started :running})
(def invalid-stop-transitions #{:stopped})
(def invalid-startup-transitions #{:running})
(def invalid-shutdown-transitions #{:uninitialized :shutdown})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- call-by-name
  ([^Symbol ns-slash-fn]
    (apply (resolve ns-slash-fn) []))
  ([^Symbol ns-slash-fn args]
    (apply (resolve ns-slash-fn) args))
  ([^Namespace an-ns ^Symbol a-fun args]
    (apply (ns-resolve an-ns a-fun) args)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   State Management Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord StateManager [state])

(defn init
  ([this]
    (init this :default))
  ([this mode]
    (if (contains? invalid-init-transitions (state/get-status (:state this)))
      (log/warn "System has aready been initialized.")
      (do
        (state/set-system (:state this)
                          (call-by-name (state/get-system-init-fn (:state this))))
        (state/set-status (:state this) :initialized)))
    (state/get-status (:state this))))

(defn deinit
  [this]
  (if (contains? invalid-deinit-transitions (state/get-status (:state this)))
    (log/error "System is not stopped; please stop before deinitializing.")
    (do
      (state/set-system (:state this) nil)
      (state/set-status (:state this) :uninitialized)))
  (state/get-status (:state this)))

(defn start
  ([this]
    (start this :default))
  ([this mode]
    (when (nil? (state/get-status (:state this)))
      (init mode))
    (if (contains? invalid-start-transitions (state/get-status (:state this)))
      (log/warn "System has already been started.")
      (do
        (state/set-system (:state this)
                          (component/start (state/get-system (:state this))))
        (state/set-status (:state this) :started)))
    (state/get-status (:state this))))

(defn stop
  [this]
  (if (contains? invalid-stop-transitions (state/get-status (:state this)))
    (log/warn "System already stopped.")
    (do
      (state/set-system (:state this)
                        (component/stop (state/get-system (:state this))))
      (state/set-status (:state this) :stopped)))
  (state/get-status (:state this)))

(defn restart
  ([this]
    (restart this :default))
  ([this mode]
    (stop this)
    (start this mode)))

(defn restart-component
  ([this component-key]
    ))

(defn startup
  "Initialize a system and start all of its components.

  This is essentially a convenience wrapper for `init` + `start`."
  ([this]
    (startup this :default))
  ([this mode]
    (if (contains? invalid-startup-transitions (state/get-status (:state this)))
      (log/warn "System is already running.")
      (do
        (when-not (contains? invalid-init-transitions
                             (state/get-status (:state this)))
          (init this mode))
        (when-not (contains? invalid-start-transitions
                            (state/get-status (:state this)))
          (start this mode))
        (state/set-status (:state this) :running)
        (state/get-status (:state this))))))

(defn shutdown
  [this]
  "Stop a running system and de-initialize it.

  This is essentially a convenience wrapper for `stop` + `deinit`."
  (if (contains? invalid-shutdown-transitions (state/get-status (:state this)))
    (log/warn "System is already shutdown.")
    (do
      (when-not (contains? invalid-stop-transitions
                           (state/get-status (:state this)))
        (stop this))
      (when-not (contains? invalid-deinit-transitions
                           (state/get-status (:state this)))
        (deinit this))
      (state/set-status (:state this) :shutdown)
      (state/get-status (:state this)))))

(def behaviour
  {:init init
   :deinit deinit
   :start start
   :stop stop
   :restart restart
   :restart-component restart-component
   :startup startup
   :shutdown shutdown})

(defn create-state-manager
  []
  (->StateManager (state/create-state-tracker)))
