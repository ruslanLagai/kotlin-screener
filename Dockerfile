# syntax = docker/dockerfile:experimental

# ------------------------------------------------------------------------------
# BUILD STAGE
# ------------------------------------------------------------------------------

FROM gradle:jdk20 as build

ARG ARTIFACT_VERSION=0.1
ARG MAVEN_OPTS

WORKDIR /workspace/

COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradlew gradlew
COPY src src

RUN --mount=type=cache,target=/root/.m2/ \
    --mount=type=cache,sharing=locked,target=/root/.gradle \
    gradle --no-daemon -s -i bootJar

# ------------------------------------------------------------------------------
# RUNTIME STAGE (deployment)
# ------------------------------------------------------------------------------

FROM openjdk:20-ea-9-slim

ARG ARTIFACT_VERSION=1.0
ENV app_name=screener-kotlin
ENV app_user=appuser

RUN addgroup ${app_user} && adduser --ingroup ${app_user} ${app_user}

RUN mkdir -p /opt/logs \
    && chown ${app_user}:${app_user} /opt/logs -R \
    && mkdir -p /opt/software/${app_name} \
    && chown ${app_user}:${app_user} /opt/software/${app_name} -R

COPY --from=build /workspace/build/libs/${app_name}-${ARTIFACT_VERSION}.jar /opt/software/${app_name}.jar

WORKDIR /opt/software/

EXPOSE 8080

ENV JAVA_OPTS="-Dserver.tomcat.accesslog.enabled=true -Xmx1024m -Xms256m -XX:+HeapDumpOnOutOfMemoryError"


ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=$PROFILE -jar ${app_name}.jar"]
