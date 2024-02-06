####
# This Dockerfile is used in order to build a container that runs the Quarkus application in native (no JVM) mode.
# It uses a micro base image, tuned for Quarkus native executables.
# It reduces the size of the resulting container image.
# Check https://quarkus.io/guides/quarkus-runtime-base-image for further information about this image.
#
# Before building the container image run:
#
# ./gradlew build -Dquarkus.package.type=native
#
# Then, build the image with:
#
# docker build -f src/main/docker/Dockerfile.native-micro -t quarkus/paddy-auth .
#
# Then run the container using:
#
# docker run -i --rm -p 8080:8080 quarkus/paddy-auth
#
###
FROM ghcr.io/graalvm/graalvm-ce:ol8-java17-22.3.3 AS BUILD

WORKDIR /appbuild

COPY . .

RUN gu install native-image

RUN ./gradlew build --no-daemon -Dquarkus.package.type=native


FROM quay.io/quarkus/quarkus-micro-image:2.0 AS NATIVE

WORKDIR /work

COPY --from=BUILD /appbuild/* .

RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work

EXPOSE 8080
USER 1001

CMD ["work/quarkus-build/gen/paddy-auth-1.0.0-SNAPSHOT-runner", "-Dquarkus.http.host=0.0.0.0"]
