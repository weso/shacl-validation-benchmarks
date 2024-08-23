#!/bin/bash

PYRUDOF_DIR="./pyrudof" # Set the path to the pyrudof directory
PYSHACL_DIR="./pyshacl" # Set the path to the pyshacl directory
RDF4J_DIR="./rdf4j" # Set the path to the rdf4j directory

# Check if the script and directory exist
if [ -d "$PYRUDOF_DIR" ]  && [ -d "$PYSHACL_DIR" ] && [ -d "$RDF4J_DIR" ]; then
    # Enter the shapes-rs directory
    cd "$PYRUDOF_DIR" || exit

    # Execute the run.sh script with the parameter
    sh run.sh 10

    # Return to the original directory
    cd .. || exit

    # Enter the pyshacl directory
    cd "$PYSHACL_DIR" || exit

    # Execute the run.sh script with the parameter
    sh run.sh 10

    # Enter the rdf4j directory
    cd "$RDF4J_DIR" || exit

    # Execute the run.sh script with the parameter
    sh run.sh

    # Return to the original directory
    cd .. || exit
else
    echo "Error: benchmark directories do not exist."
fi