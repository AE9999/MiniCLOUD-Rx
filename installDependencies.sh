#! /bin/bash
( \
    cd /tmp \
    && git clone https://github.com/AE9999/javacpp-presets.git \
    && cd javacpp-presets \
    && ./cppbuild.sh install minisat \
    && mvn clean install -Djavacpp.platform=linux-x86_64 -Djavacpp.platform.dependency=false --projects .,minisat \
    && ./cppbuild.sh install glucose \
    && mvn clean install -Djavacpp.platform=linux-x86_64 -Djavacpp.platform.dependency=false --projects .,glucose \
)
