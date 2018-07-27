# system-manager

[![Build Status][travis-badge]][travis]
[![Dependencies Status][deps-badge]][deps]
[![Clojars Project][clojars-badge]][clojars]

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

More details on those usages are below, but first, here is more useful
info:

Core/top-level API namespace: `clojusc.system-manager.core`

Core/top-level API public functions:

* `get-state`
* `get-status`
* `get-system-init-fn`
* `get-system-ns`
* `reset`
* `restart`
* `setup-manager`
* `shutdown`
* `startup`


### Production Use

TBD


### Development System

All you need to do from here on out is:

1. Include the [dependency][dep] in your `project.clj`
1. Create a `repl` namespace in someplace like `dev-resources`.
1. Require the appropriate bits and add some init code to the `repl` namespace.
1. Start up the REPL and type `(startup)`, `(shutdown)`,
   `(reset)`, etc.

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
  :init 'myproj.components.core/init
  :after-refresh 'myproj.dev.repl/init-and-startup
  :throw-errors false})

(defn init
  []
  "This is used to set the options and any other global data.

  This is defined in a function for re-use. For instance, when a REPL is
  reloaded, the options will be lost and need to be re-applied."
  (logger/set-level! '[clojusc.dev] :debug)
  (setup-manager setup-options))

(defn init-and-startup
  []
  "This is used as the 'after-refresh' function by the REPL tools library.
  Not only do the options (and other global operations) need to be re-applied,
  the system also needs to be started up, once these options have be set up."
  (init)
  (startup))

;; It is not always desired that a system be started up upon REPL loading.
;; Thus, we set the options and perform any global operations with `init`,
;; and let the user determine when they want to bring up (a potentially
;; computationally intensive) system.
(init)
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
[travis]: https://travis-ci.org/clojusc/system-manager
[travis-badge]: https://travis-ci.org/clojusc/system-manager.png?branch=master
[deps]: http://jarkeeper.com/clojusc/system-manager
[deps-badge]: http://jarkeeper.com/clojusc/system-manager/status.svg
[clojars]: https://clojars.org/clojusc/system-manager
[clojars-badge]: https://img.shields.io/clojars/v/clojusc/system-manager.svg
