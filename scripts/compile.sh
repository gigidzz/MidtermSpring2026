#!/usr/bin/env sh
set -eu

rm -rf out
mkdir -p out
javac -d out src/*.java
javac -cp out:lib/junit-platform-console-standalone.jar -d out test/*.java
