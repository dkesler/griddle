FROM ubuntu:16.04

RUN apt-get update && \
    apt-get install --yes --no-install-recommends git libtool automake g++ pkg-config bison flex default-jdk && \
    mkdir /src && git clone https://git-wip-us.apache.org/repos/asf/thrift.git /src/thrift && \
    mkdir /build && cd /src/thrift && ./bootstrap.sh && ./configure && make && make install && \
    apt-get remove --yes git libtool automake g++ pkg-config bison flex && apt-get autoremove --yes && apt-get clean

VOLUME /src/griddle
WORKDIR /src/griddle
ENTRYPOINT ["./gradlew"]
