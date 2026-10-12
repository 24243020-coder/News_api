FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY src ./src
RUN javac --add-modules jdk.httpserver -d out src/NewsApp.java && \
    jar --create --file news-app.jar --main-class NewsApp -C out .

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/news-app.jar app.jar
EXPOSE 8080
ENV PORT=8080
ENTRYPOINT ["java", "--add-modules", "jdk.httpserver", "-jar", "app.jar"]
