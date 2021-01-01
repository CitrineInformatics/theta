# Contributing

## Branching

Theta follows trunk-flow:
features and bugfixes should be developed in branches off of `master` and submitted as PRs back into `master`.

## Releasing

Theta uses the [`sbt-ci-release`](https://github.com/olafurpg/sbt-ci-release) plugin to manage releases.
The `vesrion` is set by the [`sbt-dynver`](https://github.com/dwijnand/sbt-dynver) plugin:
stable versions are defined using git tags and
snapshot versions are defined implicitly based on the last stable release. 