# build angular app
FROM node:16.3.0-alpine3.13 AS ng-build
WORKDIR /app
COPY ./java-src/com.daimler.ctp.spa/angular/package*.json ./ 
RUN npm ci && npm install -g @angular/cli@12.0.4
COPY ./java-src/com.daimler.ctp.spa/angular /app
RUN ng build --configuration production --base-href ./

# build OSGi app with gradle and bndtools
FROM gradle:7.1.0-jdk8 AS java-build
COPY --chown=gradle:gradle ./java-src /home/gradle/src
COPY --chown=gradle:gradle --from=ng-build /app/dist /home/gradle/src/com.daimler.ctp.spa/angular/dist
WORKDIR /home/gradle/src
RUN gradle export.ctp-harness_DEBUG --no-daemon

# build easy-novnc server
FROM golang:1.14-buster AS easy-novnc-build
WORKDIR /src
RUN go mod init build && \
    go get github.com/geek1011/easy-novnc@v1.1.0 && \
    go build -o /bin/easy-novnc github.com/geek1011/easy-novnc

# build app container
FROM debian:buster
RUN apt-get update -y && \
    apt-get install -y --no-install-recommends \
        openbox \
        tigervnc-standalone-server \
        supervisor \
        gosu \
        \
        libxext6 \
        libxtst6 \
        \
        pcmanfm \
        xarchiver \
        nano \
        geany \
        procps && \
    rm -rf /var/lib/apt/lists && \
    mkdir -p /usr/share/desktop-directories

RUN apt-get update -y && \
    apt-get install -y --no-install-recommends lxterminal nano wget openssh-client rsync ca-certificates xdg-utils htop tar xzip gzip bzip2 zip unzip && \
    rm -rf /var/lib/apt/lists

COPY --from=easy-novnc-build /bin/easy-novnc /usr/local/bin/
COPY menu.xml /etc/xdg/openbox/
COPY supervisord.conf /etc/
EXPOSE 8080

RUN groupadd --gid 1000 app && \
    useradd --home-dir /data --shell /bin/bash --uid 1000 --gid 1000 app && \
    mkdir -p /data
VOLUME /data

ARG JavaURL=https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u222-b10/OpenJDK8U-jre_x64_linux_hotspot_8u222b10.tar.gz
RUN cd /data && \
    wget -q -O - ${JavaURL} | tar -xvz && \
    extractJavaDir=`expr "${JavaURL}" : '.*/\(.*\)/.*'`-jre && mv ${extractJavaDir} jre

COPY --from=java-build /home/gradle/src/com.daimler.ctp.rest/generated/distributions/executable/*.jar /data

CMD ["sh", "-c", "chown app:app /data /dev/stdout && exec gosu app supervisord"]