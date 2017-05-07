FROM ubuntu:16.04

RUN apt-get update 
RUN apt-get install --yes git libtool automake g++ pkg-config bison flex default-jdk
RUN mkdir /src && git clone https://git-wip-us.apache.org/repos/asf/thrift.git /src/thrift
RUN mkdir /build && cd /src/thrift && ./bootstrap.sh && ./configure && make && make install
VOLUME /src/griddle
WORKDIR /src/griddle
ENTRYPOINT ["./gradlew"]
