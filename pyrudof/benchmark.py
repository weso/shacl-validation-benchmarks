from rudof import shacl
import time
import statistics
import csv

def main(data, shapes, iters):
    ans = []
    for file in data:
        times = []
        shacl.validate( file, shapes ) # avoid cold starts
        for _ in range(iters):
            start = time.time_ns()
            shacl.validate( file, shapes )
            end = time.time_ns()
            times.append(end - start)
        
        ans.append([
            statistics.mean(times),
            statistics.stdev(times),
            file.replace('../data/', '').replace('.ttl', '').upper(),
            'pyrudof'
        ])

    with open('/home/angel/shacl-validation-benchmark/results/pyrudof.csv', 'w', newline='') as csvfile:
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