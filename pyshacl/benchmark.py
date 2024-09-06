# -*- coding: utf-8 -*-
from pyshacl import validate, monkey
from rdflib import Graph
import time
from os import path
import statistics
import csv

def load_graph(data):
    monkey.apply_patches()
    target_ttl_file = path.abspath(data)
    target_graph = Graph()
    with open(target_ttl_file, 'rb') as file:
        target_graph.parse(file=file, format='turtle')
    return target_graph
    
def main(data, shapes, iters):
    ans = []
    for file in data:
        times = []
        for _ in range(iters):
            start = time.time_ns()
            g  = load_graph(file)
            s = load_graph(shapes)
            validate(
                g,
                shacl_graph=s,
                data_graph_format='turtle',
                shacl_graph_format='turtle',
                inference='none'
            )
            end = time.time_ns()
            times.append(end - start)
        
        ans.append([
            statistics.mean(times),
            statistics.stdev(times),
            file.replace('../data/', '').replace('.ttl', '').upper(),
            'pySHACL'
        ])

    with open('/home/angel/shacl-validation-benchmark/results/pyshacl.csv', 'w', newline='') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerows(ans)

if __name__ == '__main__':
    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument('--data', metavar='path', default='../data/10-lubm.ttl', help='the path to workspace', nargs='+')
    parser.add_argument('--shapes', metavar='path', default='../data/lubm.ttl', help='path to shape')
    parser.add_argument('--iters', default=1, type=int, help='number of iterations')
    args = parser.parse_args()

    main(args.data, args.shapes, args.iters)