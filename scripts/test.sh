#!/usr/bin/env sh
set -eu

scripts/compile.sh
java -cp out Main --self-test
java -jar lib/junit-platform-console-standalone.jar execute --class-path out --select-class MainTest
