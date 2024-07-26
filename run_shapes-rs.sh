#!/bin/bash

# Set the path to the shapes-rs directory
BENCHMARKS_DIR="./shapes-rs"

# Check if the script and directory exist
if [ -d "$BENCHMARKS_DIR" ]; then
    # Enter the benchmarks directory
    cd "$BENCHMARKS_DIR" || exit

    # Execute the execute_benchmark.sh script with the parameter
    cargo run "$1" "$2" "$3"

    # Return to the original directory
    cd - || exit
else
    echo "Error: $BENCHMARKS_DIR does not exist."
fi