(ns clojusc.system-manager.system.impl.management
  "Generic state management for a Component system."
  (:require
    [clojusc.system-manager.system.impl.state :as state]
    [com.stuartsierra.component :as component]
    [taoensso.timbre :as log])
  (:import
    (clojure.lang Namespace)
    (clojure.lang Symbol)))

(declare update-manager)

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
    (call-by-name ns-slash-fn []))
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
      (do
        (log/warn "System has aready been initialized.")
        this)
      (do
        (-> (:state this)
            (state/set-system
             (call-by-name
              (state/get-system-init-fn (:state this))))
            (state/set-status :initialized)
            update-manager)))))

(defn deinit
  [this]
  (if (contains? invalid-deinit-transitions (state/get-status (:state this)))
    (do
      (log/error "System is not stopped; please stop before deinitializing.")
      this)
    (do
      (-> (:state this)
          (state/set-system nil)
          (state/set-status :uninitialized)
          update-manager))))

(defn start
  ([this]
    (start this :default))
  ([this mode]
    (when (nil? (state/get-status (:state this)))
      (init mode))
    (if (contains? invalid-start-transitions (state/get-status (:state this)))
      (do
        (log/warn "System has already been started.")
        this)
      (do
        (-> (:state this)
            (state/set-system
             (component/start
              (state/get-system (:state this))))
            (state/set-status :started)
            update-manager)))))

(defn stop
  [this]
  (if (contains? invalid-stop-transitions (state/get-status (:state this)))
    (log/warn "System already stopped.")
    (do
      (-> (:state this)
            (state/set-system
             (component/stop
              (state/get-system (:state this))))
            (state/set-status :stopped)
            update-manager))))

(defn restart
  ([this]
    (restart this :default))
  ([this mode]
    (-> this
        (stop)
        (start mode)))
  ([this mode component-key]
    ;; Bring down the component and its dependencies
    (let [stopped-manager (component/update-system-reverse
                           (get-in this [:state :system])
                           [component-key]
                           #(stop this))
      ;; Now bring it/them back up
          started-manager (component/update-system
                           (get-in stopped-manager [:state :system])
                           [component-key]
                           #(start stopped-manager))]
      started-manager)))

(defn startup
  "Initialize a system and start all of its components.

  This is essentially a convenience wrapper for `init` + `start`."
  ([this]
    (startup this :default))
  ([this mode]
    (cond (contains? invalid-startup-transitions
                     (state/get-status (:state this)))
          (do
            (log/warn "System is already running.")
            this)

          (not (contains? invalid-init-transitions
                          (state/get-status (:state this))))
          (-> this
              (init mode)
              (start mode)
              :state
              (state/set-status :running)
              update-manager)

          (not (contains? invalid-start-transitions
                          (state/get-status (:state this))))
          (-> this
              (start mode)
              :state
              (state/set-status :running)
              update-manager)

          :else
          this)))

(defn shutdown
  [this]
  "Stop a running system and de-initialize it.

  This is essentially a convenience wrapper for `stop` + `deinit`."
  (cond (contains? invalid-shutdown-transitions
                   (state/get-status (:state this)))
        (do
          (log/warn "System is already shutdown.")
          this)

        (not (contains? invalid-stop-transitions
                        (state/get-status (:state this))))
        (-> this
            stop
            deinit
            :state
            (state/set-status :shutdown)
            update-manager)

        (not (contains? invalid-deinit-transitions
                        (state/get-status (:state this))))
        (-> this
            deinit
            :state
            (state/set-status :shutdown)
            update-manager)

        :else
        this))

(def behaviour
  {:init init
   :deinit deinit
   :start start
   :stop stop
   :restart restart
   :startup startup
   :shutdown shutdown})

(defn create-manager
  [state-tracker]
  (->StateManager state-tracker))

(def update-manager #'create-manager)
