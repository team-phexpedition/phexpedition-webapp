[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=team-phexpedition_phexpedition-webapp&metric=alert_status)](https://sonarcloud.io/dashboard?id=team-phexpedition_phexpedition-webapp) [![Build Actions Status](https://github.com/team-phexpedition/phexpedition-webapp/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/team-phexpedition/phexpedition-webapp/actions) ![Docker Hub image](https://github.com/team-phexpedition/phexpedition-webapp/workflows/Docker%20Hub%20image/badge.svg)

# Phexpedition

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```

## Packaging and running the application

The application can be packaged using `./mvnw package`.
It produces the `webapp-1.0.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/webapp-1.0.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./target/webapp-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image.
