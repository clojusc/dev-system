(ns clojusc.system-manager.repl
  "A development namespace for the system-manager project.

  Something like this can be created for any project that wishes to use the
  system-manager for managing a Component-based system either in the REPL (for
  development), or in `(-main)` as part of a production deployment."
  (:require
   [clojure.java.io :as io]
   [clojure.pprint :refer [pprint]]
   [clojusc.system-manager.components.core]
   [clojusc.system-manager.core :refer :all]
   [clojusc.system-manager.system.core :as system-api]
   [clojusc.twig :as logger]
   [com.stuartsierra.component :as component]
   [trifl.java :refer [show-methods]]))

(def setup-options {
  :init 'clojusc.system-manager.components.core/init
  :after-refresh 'clojusc.system-manager.repl/init-and-startup
  :throw-errors false})

(defn init
  "This is used to set the options and any other global data.

  This is defined in a function for re-use. For instance, when a REPL is
  reloaded, the options will be lost and need to be re-applied."
  []
  (logger/set-level! '[clojusc.dev] :debug)
  (setup-manager setup-options))

(defn init-and-startup
  "This is used as the 'after-refresh' function by the REPL tools library.
  Not only do the options (and other global operations) need to be re-applied,
  the system also needs to be started up, once these options have be set up."
  []
  (init)
  (startup))

;; It is not always desired that a system be started up upon REPL loading.
;; Thus, we set the options and perform any global operations with init,
;; and let the user determine when then want to bring up (a potentially
;; computationally intensive) system.
(init)

(defn banner
  []
  (println (slurp (io/resource "text/banner.txt")))
  :ok)
