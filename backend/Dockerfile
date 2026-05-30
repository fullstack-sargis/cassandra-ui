FROM gradle:8.14.3-jdk21 AS build
USER root
RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates ca-certificates-java openssl \
    && update-ca-certificates -f \
    && rm -rf /var/lib/apt/lists/*
RUN for host in repo.maven.apache.org plugins.gradle.org; do \
        export CERT_HOST="$host"; \
        openssl s_client -showcerts -connect "$host:443" -servername "$host" </dev/null 2>/dev/null \
          | awk 'BEGIN {n=0} /BEGIN CERTIFICATE/ {n++} n > 0 {print > "/tmp/" ENVIRON["CERT_HOST"] "-" n ".pem"}'; \
        for cert in /tmp/"$host"-*.pem; do \
          keytool -importcert -noprompt -trustcacerts \
            -alias "build-$host-$(basename "$cert" .pem)" \
            -file "$cert" \
            -keystore "$JAVA_HOME/lib/security/cacerts" \
            -storepass changeit || true; \
        done; \
      done
WORKDIR /app
COPY build.gradle settings.gradle gradle.properties ./
COPY src ./src
RUN gradle --no-daemon bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/cassandra-ui-backend-0.1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
