#! /bin/bash

#
# Thanks https://ec.haxx.se/http-multipart.html
#
curl -v -H 'Content-Type: multipart/magic'  \
        --form nsolvers=1 \
        --form useDocker=true \
        --form cnfFile=@cnfs/hole9.cnf \
        --form assumptionFile=@cnfs/hole9-2.assumptions \
        http://localhost:8080/api/submit
