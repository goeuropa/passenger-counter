FROM eclipse-temurin:21
USER root
WORKDIR /app
COPY ./src/main/resources/vehicleCapacities.json ./resources/vehicleCapacities.json
COPY ./src/main/resources/application.yml .
COPY ./build/libs/passenger-counter.jar .
CMD ["java", "-jar", "passenger-counter.jar"]
