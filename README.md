# system-manager

[![Build Status][travis-badge]][travis]
[![Dependencies Status][deps-badge]][deps]
[![Clojars Project][clojars-badge]][clojars]

*A Clojure library for setting up consistent Component-based systems*

[![Project logo][logo]][logo-link]


## About

### Purpose

The [Component][component] Clojure library has been become an invaluable tool
for Clojure developers who create applications with suites of potentially
interdependent services or software that provides supporting functionality
for other parts of the system. In real-world deployments, such system
components often need to be started up in a particular (dependency) order,
and conversely, shut down in a particular order.

The Component library has a great design: it does one thing well, completely
focusing on that. However, in nearly all of my production deployments, I need
more functionality, things that assist with debugging, monitoring, and
fine-tuned operations. These include such things as:

1. More defined states (and related state transitions). Component has
   `start` and `stop`, at the component level and at the system level.
   I need to know if a system has been initialized or not; if so, has it been
   started? Did it finish, and is it running? Has it been stopped, with
   state data still available? Or has it been shutdown: stopped with no state
   data?
2. An API for accessing these states.
3. An API for moving the system between those states


### Evolution

The code in this library is one of those things that has evolved over time and
keeps getting copied and pasted into new projects, with slight updates and
improvments at each new iteration. The most recent big improvements were
done:
* In 2017, as part of the Dragon project
* In 2018, as part of:
   * the Hexagram30 projects
   * the NASA CMR-Graph and CMR-OPeNDAP projects
   * the Clojang component project

In partciular, the second one is where an API emerged (using a protocol-based
approach), and this was backported to the Hexagram projects. Afterward, the
code was split out into it's own project, "dev-system". Once parts of it
started being used for not just development systems in the REPL, but actual
production applications, more refactoring ensued and the project was renamed
"system-manager".


### Design

The system-manager has three conceptual parts:

1. Data: current state, the Component system data structure, and configuration
   used to initialize or re=initialize (e.g., for restarts) the system. There
   is an API for accessing and updating this data.
1. Transitions: an API for moving the Component system manager between valid
   state transitions (e.g., init, deinit, start, stop, etc.).
1. High-level wrapper: the data and transitions API functions all take as their
   first argument their respective protocol records, and this involves a
   certain amount of boilerplate and repitition. The high-level wrapper takes
   care of this by creating the record instances and automatically passing the
   right ones to the right API functions, keeping the developer experience
   clean and simple.

The data can be changed by its own API functions, by those of the transition
API, and of course by the high-level API as well. The data that need to be
updated are system `:status` and the `:system` data structure itself. Both of
these are stored in the state tracker, which is stored in the `*mgr*` data
structure with the `:state` key.


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
[component]: https://github.com/stuartsierra/component
