#! /bin/bash

#
# http://ryaneschinger.com/blog/using-google-container-registry-gcr-with-minikube/
#

minikube start

sleep 5

#
# TODO Replace MiniCLOUD-RX-f7ddd7a7326d.json with your json file
#

kubectl  create secret docker-registry gcr-json-key \
          --docker-server=https://gcr.io \
          --docker-username=_json_key \
          --docker-password="$(cat MiniCLOUD-RX-f7ddd7a7326d.json)" \
          --docker-email=ssoftwaresolutionsbv@gmail.com


kubectl patch serviceaccount default -p '{"imagePullSecrets": [{"name": "gcr-json-key"}]}'

#
# Execute these steps
#

#kubectl create -f worker-service.yaml
#kubectl create -f worker-pets.yaml
# Wait until all pets have been created !
#kubectl create -f master-pod.yaml
#kubectl create -f master-service.yaml