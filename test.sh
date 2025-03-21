#!/bin/bash

for i in {1..1000}
do
  git commit --allow-empty -m "Test Commit Nr. $i"
done