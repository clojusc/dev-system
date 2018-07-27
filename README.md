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


### Production Use

TBD


### Development System

All you need to do from here on out is:

1. Include the [dependency][dep] in `project.clj`
1. Create a `repl` namespace in someplace like `dev-resources`.
1. Require the appropriate bits, and populate the `repl` ns.
1. Start up the REPL and type `(startup)`, `(shutdown)`, or
   `(reset)`.

The REPL namespace for this project is an example of this approach; for more
insight, see `dev-resources/src/clojusc/system_manager/repl.clj`.

Here's another example:

```clj
(ns myproj.dev.repl
  "A development namespace for 'my project'.

  Something like this can be created for any project that wishes to use the
  system-manager for managing REPL state in its own development environment."
  (:require
   [clojusc.system-manager.core :refer :all]
   [clojusc.twig :as logger]
   [myproj.components.core]))

(def setup-options {
  :init 'clojusc.system-manager.components.core/init
  :after-refresh 'clojusc.system-manager.repl/init-and-startup
  :throw-errors false})

(defn init
  []
  (logger/set-level! '[clojusc.dev] :debug)
  (setup-manager setup-options))

(defn init-and-startup
  []
  (init)
  (startup))

(init)

(defn banner
  []
  (println (slurp (io/resource "text/banner.txt")))
  :ok)
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
