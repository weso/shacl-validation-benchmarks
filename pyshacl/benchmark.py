# -*- coding: utf-8 -*-
import rdflib
import pyshacl
from os import path

target_ttl_file = \
    '../test/resources/dash_tests/core/complex/personexample.test.ttl'
target_ttl_file = path.abspath(target_ttl_file)
target_graph = rdflib.Graph("Memory")
with open(target_ttl_file, 'rb') as file:
    target_graph.parse(file=file, format='turtle')

pyshacl.validate(target_graph, inference='none')

if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument('--workspace', metavar='path', required=True, help='the path to workspace')
    parser.add_argument('--schema', metavar='path', required=True, help='path to schema')
    args = parser.parse_args()
    main(workspace=args.workspace, schema=args.schema, dem=args.dem)