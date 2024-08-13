#!/bin/bash

SHAPES_RS_DIR="./shapes-rs" # Set the path to the shapes-rs directory
PYSHACL_DIR="./pyshacl" # Set the path to the pyshacl directory
JENA_DIR="./jena" # Set the path to the jena directory
TOPQUADRANT_DIR="./topquadrant" # Set the path to the top-quadrant directory
RDF4J_DIR="./rdf4j" # Set the path to the rdf4j directory

# Check if the script and directory exist
if [ -d "$SHAPES_RS_DIR" ]  && [ -d "$PYSHACL_DIR" ] && [ -d "$JENA_DIR" ] && [ -d "$TOPQUADRANT_DIR" ] && [ -d "$RDF4J_DIR" ]; then
    # Enter the shapes-rs directory
    cd "$SHAPES_RS_DIR" || exit

    # Execute the run.sh script with the parameter
    sh run.sh $1

    # Return to the original directory
    cd .. || exit

    # Enter the pyshacl directory
    cd "$PYSHACL_DIR" || exit

    # Execute the run.sh script with the parameter
    sh run.sh $1

    # Return to the original directory
    cd .. || exit

    # Enter the jena directory
    cd "$JENA_DIR" || exit

    # Execute the run.sh script with the parameter
    sh run.sh

    # Return to the original directory
    cd .. || exit

    # Enter the rdf4j directory
    cd "$RDF4J_DIR" || exit

    # Execute the run.sh script with the parameter
    sh run.sh

    # Return to the original directory
    cd .. || exit

    # Enter the top-quadrant directory
    cd "$TOPQUADRANT_DIR" || exit

    # Execute the run.sh script with the parameter
    sh run.sh

    # Return to the original directory
    cd .. || exit
else
    echo "Error: benchmark directories do not exist."
fi