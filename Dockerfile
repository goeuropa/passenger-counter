FROM eclipse-temurin:17
USER root
WORKDIR /app
COPY ./src/main/resources/vehicleCapacities.json ./resources/vehicleCapacities.json
COPY ./build/libs/passenger-counter-client.jar .
CMD ["java", "-jar", "passenger-counter-client.jar"]
