(ns clojusc.system-manager.repl
  "A development namespace for the system-manager project.

  Something like this can be created for any project that wishes to use the
  system-manager for managing a Component-based system either in the REPL (for
  development), or in `(-main)` as part of a production deployment."
  (:require
   [clojure.java.io :as io]
   [clojure.pprint :refer [pprint]]
   [clojure.tools.namespace.repl :as repl]
   [clojusc.system-manager.components.core]
   [clojusc.system-manager.core :refer [
     refresh reset setup-manager shutdown startup system]]
   [clojusc.system-manager.system.core :as system-api]
   [clojusc.twig :as logger]
   [com.stuartsierra.component :as component]
   [trifl.java :refer [show-methods]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Initial Setup & Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(logger/set-level! '[clojusc.dev] :debug)

(def setup-options {
  :init 'clojusc.system-manager.components.core/init
  :after-refresh 'clojusc.system-manager.repl/startup
  :throw-errors false})

(setup-manager setup-options)

(defn banner
  []
  (println (slurp (io/resource "text/banner.txt")))
  :ok)
