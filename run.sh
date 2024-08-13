#!/bin/bash

SHAPES_RS_DIR="./shapes-rs" # Set the path to the shapes-rs directory
PYSHACL_DIR="./pyshacl" # Set the path to the pyshacl directory
SHACLEX_DIR="./shaclex" # Set the path to the shaclex directory

# Check if the script and directory exist
if [ -d "$SHAPES_RS_DIR" ]  && [ -d "$PYSHACL_DIR" ] && [ -d "$SHACLEX_DIR" ]; then
    # Enter the shapes-rs directory
    cd "$SHAPES_RS_DIR" || exit

    # Execute the run.sh script with the parameter
    sh run.sh $1

    # Return to the original directory
    cd .. || exit

    # Enter the shapes-rs directory
    cd "$PYSHACL_DIR" || exit

    # Execute the run.sh script with the parameter
    sh run.sh $1

    # Return to the original directory
    cd .. || exit

        # Enter the shapes-rs directory
    cd "$SHACLEX_DIR" || exit

    # Execute the run.sh script with the parameter
    sh run.sh $1 $2 $3

    # Return to the original directory
    cd .. || exit
else
    echo "Error: benchmark directories do not exist."
fi