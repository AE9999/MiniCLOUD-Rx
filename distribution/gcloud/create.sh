#! /bin/bash

# gcloud auth application-default login


gcloud --project=minicloud-rx \
       alpha container clusters create minicloud-rx \
       --num-nodes=8 \
       --enable-kubernetes-alpha \

kubectl create -f worker-service.yaml

kubectl create -f worker-pets.yaml

# kubectl create -f master-pod.yaml

# kubectl create -f master-service.yaml

# gcloud --project=minicloud-rx container clusters delete minicloud-rx