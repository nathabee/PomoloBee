#!/bin/sh
# This script copies custom hooks to the .git/hooks directory

HOOKS_DIR=$(pwd)/hooks
GIT_HOOKS_DIR=$(git rev-parse --git-dir)/hooks

# Copy all hooks from hooks directory to .git/hooks
cp $HOOKS_DIR/* $GIT_HOOKS_DIR/
echo "Hooks installed successfully."
