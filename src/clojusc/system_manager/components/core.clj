(ns clojusc.system-manager.components.core
  "A placeholder, no-op component namespace."
  (:require
    [com.stuartsierra.component :as component]))

(defrecord PlaceholderComponent []
  component/Lifecycle

  (start [component]
    component)

  (stop [component]
    component))

(defn init
  "In a real component.core namespace, the init function would be responsible
  for selecting the appropriate function for generating a Component library
  system map."
  []
  (component/system-map
    :placeholder (map->PlaceholderComponent {})))
