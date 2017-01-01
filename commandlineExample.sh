#! /bin/bash
java  -jar master/target/master-rx.jar \
      -cnfFile ./cnfs/hanoi4.cnf \
      -assumptionFile ./cnfs/empty.assumptions \
      -nsolvers 1
