#!/bin/bash
source .venv/bin/activate

pip3 install -r requirements.txt > /dev/null
python3 benchmark.py --data ../data/*-lubm.ttl --shapes ../data/lubm.ttl --iters $1

deactivate