# Common
All common code for build system in clojure

# Contributing

## Tests
To run unit tests inside `./test/unit`
```bash
clj -M:test :unit
```
To run integration tests inside `./test/integration`
```bash
clj -M:test :integration
```
To run all tests inside `./test`
```bash
clj -M:test
```
To generate a coverage report 
```bash
clj -M:test --plugin kaocha.plugin/cloverage
```

## Lint and format

```bash
clj -M:clojure-lsp format
clj -M:clojure-lsp clean-ns
clj -M:clojure-lsp diagnostics
```

## Build / Deploy

```bash
  # Build
  clojure -T:build jar :version '"0.1.0"'
  # Deploy
  env CLOJARS_USERNAME=username CLOJARS_PASSWORD=clojars-token clojure -T:build deploy :version '"0.1.0"'
```

# Features

## System

Copyright © 2023 easybootstrap
