FROM gcr.io/distroless/java17

COPY build/libs/app.jar ./
COPY build/libs/*.jar app.jar

CMD ["app.jar"]
