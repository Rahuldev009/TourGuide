FROM openjdk:8-alpine
COPY build/libs/tourGuide-1.0.0.jar tourguide.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "tourguide.jar"]