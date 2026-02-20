FROM  amazoncorretto:21
WORKDIR /app
COPY target/ecommerce-0.0.1-SNAPSHOT.jar /app/ecommerce.jar
EXPOSE 8080
CMD ["java", "-jar", "ecommerce.jar"]