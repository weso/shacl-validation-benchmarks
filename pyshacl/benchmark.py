# -*- coding: utf-8 -*-
from pyshacl import validate, monkey
from rdflib import Graph
import time
from os import path

def load_graph(data):
    monkey.apply_patches()
    target_ttl_file = path.abspath(data)
    target_graph = Graph()
    with open(target_ttl_file, 'rb') as file:
        target_graph.parse(file=file, format='turtle')
    return target_graph
    
def main(data, shapes, iters):
    s = load_graph(shapes)
    times = []

    for file in data:
        g  = load_graph(file)

        validate(
            g,
            shacl_graph=s,
            data_graph_format='turtle',
            shacl_graph_format='turtle',
            inference='none'
        ) # avoid cold starts
        for _ in range(iters):
            start = time.time()
            validate(
                g,
                shacl_graph=s,
                data_graph_format='turtle',
                shacl_graph_format='turtle',
                inference='none'
            )
            end = time.time()
            times.append(end - start)
            print(end - start)

if __name__ == '__main__':
    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument('--data', metavar='path', default='../data/10-lubm.ttl', help='the path to workspace', nargs='+')
    parser.add_argument('--shapes', metavar='path', default='../data/lubm.ttl', help='path to shape')
    parser.add_argument('--iters', default=1, type=int, help='number of iterations')
    args = parser.parse_args()

    main(args.data, args.shapes, args.iters)