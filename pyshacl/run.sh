#!/bin/bash
source .venv/bin/activate

pip3 install -r requirements.txt > /dev/null
python3 benchmark.py --data ../data/80-lubm.ttl ../data/90-lubm.ttl ../data/100-lubm.ttl --shapes ../data/conformant.ttl --iters $1

deactivate