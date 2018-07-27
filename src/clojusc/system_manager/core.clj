(ns clojusc.system-manager.core
  "High-level system management API for a Component-based system."
  (:require
    [clojure.tools.namespace.repl :as repl]
    [clojusc.system-manager.system.core :as system-api]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Management Global Vars   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:dynamic *mgr* nil)
(def ^:dynamic *system-init-fn* 'identity)
(def ^:dynamic *after-refresh-fn* (ns-resolve *ns* 'startup))
(def ^:dynamic *throw-errors* false)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Management System Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Utility Functions

(defn- mgr-arg
  []
  (or *mgr*
      (let [msg (str "A state manager is not defined; "
                     "have you run (startup)?")]
        (if *throw-errors*
          (throw (new Exception msg))
          {:error msg}))))

(defn- system-arg
  []
  (if-let [state (:state *mgr*)]
    (system-api/get-system state)
    (let [msg (str "System data structure is not defined; "
                   "have you run (startup)?")]
      (if *throw-errors*
        (throw (new Exception msg))
        {:error msg}))))

(defn- call-if-no-error
  [func data & args]
  (if (:error data)
    data
    (apply func data args)))

(defn- state-arg
  []
  (call-if-no-error :state (mgr-arg)))

;; State Management

(defn startup
  []
  (alter-var-root #'*mgr* (constantly (system-api/create-state-manager)))
  (system-api/set-system-init-fn (:state *mgr*) *system-init-fn*)
  (system-api/startup *mgr*))

(defn shutdown
  []
  (when *mgr*
    (let [result (system-api/shutdown (mgr-arg))]
      (alter-var-root #'*mgr* (constantly nil))
      result)))

(defn restart
  ([]
    (shutdown)
    (startup))
  ([component-key]
    (system-api/restart (system-arg) component-key)))

;; Reloading Management

(defn reset
  []
  (shutdown)
  (repl/refresh :after *after-refresh-fn*))

(def refresh #'repl/refresh)

;; Initialization

(defn setup-manager
  [opts]
  (alter-var-root #'*system-init-fn* (constantly (:init opts)))
  (alter-var-root #'*after-refresh-fn* (constantly (:after-refresh opts)))
  (alter-var-root #'*throw-errors* (constantly (:throw-errors opts))))

;; Convenience wrappers

(def manager #'mgr-arg)
(def state #'state-arg)
(def system #'system-arg)

(defn get-state
  []
  (call-if-no-error system-api/get-state (state-arg)))

(defn get-status
  []
  (call-if-no-error system-api/get-status (state-arg)))

(defn get-system-init-fn
  []
  (call-if-no-error system-api/get-system-init-fn (state-arg)))

(defn get-system-ns
  []
  (call-if-no-error system-api/get-system-ns (state-arg)))
