#!/bin/bash
cd /home/kavia/workspace/code-generation/reminder-calendar-4413/reminder_app_frontend
./gradlew lint
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

