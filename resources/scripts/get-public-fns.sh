#!/bin/bash

grep 'defn' src/clojusc/system_manager/core.clj | \
	grep -v defn- | \
	sed 's/(defn /* `/' | \
	sed 's/$/`/' | \
	sort
