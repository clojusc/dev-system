(ns clojusc.system-manager.core
  "High-level system management API for a Component-based system."
  (:require
    [clojure.tools.namespace.repl :as repl]
    [clojusc.system-manager.system.core :as system-api]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Management Global Options   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; Options are set before a startup function is called; due to this design
;;; requirement, options are stored as atoms

(def ^{:dynamic true :private true} *system-init-fn* (atom 'identity))
(def ^{:dynamic true :private true} *after-refresh-fn* (atom (ns-resolve *ns* 'startup)))
(def ^{:dynamic true :private true} *throw-errors* (atom false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Management DB   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; The manager is not only an implementation of `StateDataAPI` and an instance
;;; of `StateTracker`, it's also a simple, in-memory database: a Clojure atom.

(def ^{:dynamic true :private true} *mgr* (atom nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Management System Implementation   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Utility Functions

(defn- mgr-arg
  []
  (or @*mgr*
      (let [msg (str "A state manager is not defined; "
                     "have you run (startup)?")]
        (if @*throw-errors*
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

(defn- system-arg
  []
  (call-if-no-error #(system-api/get-system (:state %)) (mgr-arg)))

(defn reset-mgr!
  [new-mgr]
  (reset! *mgr* new-mgr))

;; State Management

(defn create-manager
  [state-options]
  (->> state-options
       system-api/create-state-tracker
       system-api/create-state-manager
       reset-mgr!))

(defn startup
  []
  (let [options {:init-fn @*system-init-fn*
                 :refresh-fn @*after-refresh-fn*}]
    (create-manager options)
    (reset-mgr! (system-api/startup @*mgr*))
    (system-api/get-status (state-arg))))

(defn shutdown
  []
  (when *mgr*
    (reset-mgr! (system-api/shutdown @*mgr*))
    (let [result (system-api/get-status (state-arg))]
      (reset-mgr! nil)
      result)))

(defn restart
  ([]
    (shutdown)
    (startup))
  ([component-key]
    (->> component-key
         (system-api/restart (system-arg))
         reset-mgr!)))

;; Reloading Management

(defn reset
  []
  (shutdown)
  (repl/refresh :after *after-refresh-fn*))

(def refresh #'repl/refresh)

;; Initialization

(defn setup-manager
  [opts]
  (reset! *system-init-fn* (:init opts))
  (reset! *after-refresh-fn* (:after-refresh opts))
  (reset! *throw-errors* (:throw-errors opts)))

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
