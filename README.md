# system-manager

*A Clojure library for setting up consistent Component-based systems*

[![Project logo][logo]][logo-link]


## About

The code in this library is one of those things that has evolved over time and
keeps getting copied and pasted into new projects, with slight updates and
improvments at each new iteration. The most recent big improvements were
done:
* In 2017, as part of the Dragon project
* In 2018, as part of:
   * the Hexagram30 projects
   * the NASA CMR-Graph and CMR-OPeNDAP projects
   * the Clojang component project

In partciular, the second one is where I shifted to a protocol-based approach.
So far, I've been happiest with this approach, and wan to start using this
across all my Clojure projects.

A project of it's own is the first step :-)


## Usage

There are two ways to use this:

1. as a development system (i.e., in the REPL)
1. as something called from `(-main)` for use in running production apps


### Development System

All you need to do from here on out is:

1. Include the [dependency][dep] in `project.clj`
1. Create a `repl` namespace in someplace like `dev-resources`.
1. Require the appropriate bits, and populate the `repl` ns.
1. Start up the REPL and type `(startup)`, `(shutdown)`, or
   `(reset)`.


### Production Use

TBD


## Example REPL Namespace

```clj
(ns myproj.dev.repl
  "A development namespace for the my project.

  Somethink like this can be created for any project that wishes to use the
  system-manager for managing REPL state in its own development environment."
  (:require
   [clojure.java.io :as io]
   [clojure.pprint :refer [pprint]]
   [clojure.tools.namespace.repl :as repl]
   [clojusc.system-manager.system.core :as system-api]
   [clojusc.twig :as logger]
   [com.stuartsierra.component :as component]
   [myproj.components.core]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Initial Setup & Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(logger/set-level! '[myproj] :debug)

(def ^:dynamic *mgr* nil)
(def system-init-fn 'clojusc.system-manager.components.core/init)
(def after-refresh-fn 'clojusc.system-manager.repl/startup)

(defn mgr-arg
  []
  (if *mgr*
    *mgr*
    (throw (new Exception
                (str "A state manager is not defined; "
                     "have you run (startup)?")))))

(defn system-arg
  []
  (if-let [state (:state *mgr*)]
    (system-api/get-system state)
    (throw (new Exception
                (str "System data structure is not defined; "
                     "have you run (startup)?")))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   State Management   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn startup
  []
  (alter-var-root #'*mgr* (constantly (system-api/create-state-manager)))
  (system-api/set-system-ns (:state *mgr*) system-init-fn)
  (system-api/startup *mgr*))

(defn shutdown
  []
  (when *mgr*
    (let [result (system-api/shutdown (mgr-arg))]
      (alter-var-root #'*mgr* (constantly nil))
      result)))

(defn system
  []
  (system-api/get-system (:state (mgr-arg))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Reloading Management   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn reset
  []
  (shutdown)
  (repl/refresh :after after-refresh-fn))

(def refresh #'repl/refresh)

```


## License

Copyright © 2015-2018 Duncan McGreggor

Copyright © 2018 NASA

Apache License, Version 2.0.


<!-- Named page links below: /-->

[logo]: https://avatars0.githubusercontent.com/u/18177940?s=250
[logo-large]: https://avatars0.githubusercontent.com/u/18177940
[logo-link]: https://github.com/clojusc/
[dep]: https://clojars.org/clojusc/system-manager
