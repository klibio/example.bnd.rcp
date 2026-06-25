# GRADLE BUILD CONTAINER for OSGi Java implementation
#   build OSGi app with gradle and bndtools
FROM gradle:8.9-jdk21 AS java-build
# Accept proxy build-args so Gradle can reach Maven Central behind corporate proxies
ARG http_proxy
ARG https_proxy
ARG HTTP_PROXY
ARG HTTPS_PROXY
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN ls -l /home/gradle/src/
# Ensure bnd local directories exist in clean CI checkouts.
RUN mkdir -p /home/gradle/src/cnf/local /home/gradle/src/cnf/cache
# Optional diagnostics: local builds may have a pre-populated p2 cache, CI usually does not.
RUN if [ -d /home/gradle/src/cnf/cache/p2-Platform_R-4.35-202502280140 ]; then \
            ls /home/gradle/src/cnf/cache/ && echo "---p2---" && ls /home/gradle/src/cnf/cache/p2-Platform_R-4.35-202502280140/ | head -5 && stat /home/gradle/src/cnf/cache/p2-Platform_R-4.35-202502280140/; \
        else \
            echo "No pre-populated p2 cache found; Gradle will resolve dependencies online."; \
        fi
# calling gradle explicitly for all platform-independent and linux.gtk projects
# Write proxy into ~/.gradle/gradle.properties using systemProp.* — the official Gradle mechanism
# that propagates to ALL JVMs including the single-use daemon forked by Gradle 8.x.
# JAVA_TOOL_OPTIONS carries preferIPv4Stack for all JVM forks.
RUN set -e; \
    export JAVA_TOOL_OPTIONS="-Djava.net.preferIPv4Stack=true"; \
    if [ -n "${http_proxy:-}" ]; then \
      PH=$(echo "${http_proxy}" | sed 's|.*://||;s|.*@||;s|:.*||'); \
      PP=$(echo "${http_proxy}" | sed 's|.*://||;s|.*@||;s|[^:]*:||;s|/.*||'); \
      mkdir -p /home/gradle/.gradle; \
      printf 'systemProp.http.proxyHost=%s\nsystemProp.http.proxyPort=%s\nsystemProp.https.proxyHost=%s\nsystemProp.https.proxyPort=%s\nsystemProp.http.nonProxyHosts=localhost\n' \
        "${PH}" "${PP}" "${PH}" "${PP}" > /home/gradle/.gradle/gradle.properties; \
    fi; \
    gradle --no-daemon clean \
    resolve.app.ui_linux.gtk.x86-64 \
    resolve.12_equinoxapp_linux.gtk.x86-64 \
    resolve.ui_linux.gtk.x86-64 \
    export.app.ui_linux.gtk.x86-64 \
    export.12_equinoxapp_linux.gtk.x86-64 \
    export.ui_linux.gtk.x86-64

# GOLANG BUILD CONTAINER for easy-novnc
#   build easy-novnc server
FROM golang:1.23-bookworm AS easy-novnc-build
ARG http_proxy
ARG https_proxy
ARG HTTP_PROXY
ARG HTTPS_PROXY
RUN GONOSUMCHECK=* GONOSUMDB=* GOBIN=/bin go install github.com/geek1011/easy-novnc@v1.1.0

# APPLICATION RUNTIME container
FROM debian:bookworm-slim

ARG BUILD_DATE
ARG VCS_REF

LABEL org.opencontainers.image.authors="dev@klib.io" \
      org.label-schema.build-date=$BUILD_DATE \
      org.label-schema.vcs-url="https://github.com/klibio/example.bnd.rcp" \
      org.label-schema.vcs-ref=$VCS_REF
# Accept proxy build-args so apt-get works behind corporate proxies;
# values are empty-string when not supplied (e.g. in GitHub Actions)
ARG http_proxy
ARG https_proxy
ARG HTTP_PROXY
ARG HTTPS_PROXY
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
COPY pop/pop.sh /data/pop.sh

EXPOSE 8080

#add unix user and group with specific home dir
RUN groupadd --gid 1000 app && \
    useradd --home-dir /data --shell /bin/bash --uid 1000 --gid 1000 app && \
    mkdir -p /data
VOLUME /data

# Eclipse Temurin 21 JRE — copied from official image, no download at build time
COPY --from=eclipse-temurin:21-jre-jammy /opt/java/openjdk /data/jre

COPY --from=java-build /home/gradle/src/example.rcp.app.ui/generated/distributions/executable /data
COPY --from=java-build /home/gradle/src/example.rcp.ui/generated/distributions/executable/*.jar /data
COPY --from=java-build /home/gradle/src/example.osgi.services/generated/distributions/executable/*.jar /data

CMD ["sh", "-c", "chown app:app /data /dev/stdout && exec gosu app supervisord"]
