#!/usr/bin/env bash
# Runs StreamFlix (needs JDK 17+ and Maven)
cd "$(dirname "$0")" && mvn -q javafx:run
