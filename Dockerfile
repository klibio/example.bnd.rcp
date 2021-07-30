# Anpassen an example.bnd.rcp
# build OSGi app with gradle and bndtools
FROM gradle:6.9.0-jdk11 AS java-build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN ls -l /home/gradle/src/
#run for all 3 linux projects
RUN gradle clean export.app.ui_linux.gtk.x86-64 --no-daemon
RUN gradle export.12_equinoxapp_linux.gtk.x86-64 --no-daemon
RUN gradle export.ui_linux.gtk.x86-64 --no-daemon

# build easy-novnc server
FROM golang:1.14-buster AS easy-novnc-build
WORKDIR /src
RUN go mod init build && \
    go get github.com/geek1011/easy-novnc@v1.1.0 && \
    go build -o /bin/easy-novnc github.com/geek1011/easy-novnc

# build app container
FROM debian:buster

ARG BUILD_DATE
ARG VCS_REF

LABEL org.opencontainers.image.authors="dev@klib.io" \
      org.label-schema.build-date=$BUILD_DATE \
      org.label-schema.vcs-url="https://github.com/klibio/example.bnd.rcp" \
      org.label-schema.vcs-ref=$VCS_REF
# Workaround https://unix.stackexchange.com/questions/2544/how-to-work-around-release-file-expired-problem-on-a-local-mirror
RUN echo "Acquire::Check-Valid-Until \"false\";\nAcquire::Check-Date \"false\";" | cat > /etc/apt/apt.conf.d/10no--check-valid-until
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
    apt-get install -y --no-install-recommends lxterminal wget openssh-client rsync ca-certificates xdg-utils htop tar xzip gzip bzip2 zip unzip && \
    rm -rf /var/lib/apt/lists

COPY --from=easy-novnc-build /bin/easy-novnc /usr/local/bin/
COPY menu.xml /etc/xdg/openbox/
COPY supervisord.conf /etc/
COPY pop.sh /data/pop.sh

EXPOSE 8080

#add unix user and group with specific home dir
RUN groupadd --gid 1000 app && \
    useradd --home-dir /data --shell /bin/bash --uid 1000 --gid 1000 app && \
    mkdir -p /data
VOLUME /data

ARG JavaURL=https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.11%2B9/OpenJDK11U-jre_x64_linux_hotspot_11.0.11_9.tar.gz
SHELL [ "/bin/bash", "-c"]
RUN cd /data && \
    wget -q -O - ${JavaURL} | tar -xvz && \
    JavaURLdecoded=$(echo "$JavaURL" | sed "s/%2B/+/") \
    extractJavaDir=`expr "${JavaURLdecoded}" : '.*/\(.*\)/.*'`-jre && mv ${extractJavaDir} jre

COPY --from=java-build /home/gradle/src/example.rcp.app.ui/generated/distributions/executable /data
COPY --from=java-build /home/gradle/src/example.rcp.ui/generated/distributions/executable/*.jar /data
COPY --from=java-build /home/gradle/src/example.osgi.services/generated/distributions/executable/*.jar /data


CMD ["sh", "-c", "chown app:app /data /dev/stdout && exec gosu app supervisord"]
