FROM  amazoncorretto:21
WORKDIR /app
COPY target/inventory-0.0.1-SNAPSHOT.jar /app/inventory.jar
EXPOSE 8080
CMD ["java", "-jar", "inventory.jar"]