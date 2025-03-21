#!/bin/bash
for i = {1..1000}
    git commit --allow-empty -m "Leerer Commit Nr. $i"
done