# Plain Java News Application (No Spring Boot)

This project uses only standard Java APIs: `HttpServer`, `HttpClient`, and `java.net`. It retrieves top headlines from NewsAPI.org and exposes them through a small HTTP API.

## Requirements
- JDK 17 or later
- Docker Desktop for container steps
- NewsAPI.org API key: https://newsapi.org/

No Maven, Gradle, Spring Boot, or third-party Java libraries are required.

## Run in VS Code (Windows PowerShell)

1. Extract the ZIP and open the `java-news-app` folder in VS Code.
2. Open Terminal > New Terminal in VS Code.
3. Set your NewsAPI key in the terminal (replace the placeholder):
```powershell
$env:NEWSAPI_KEY="YOUR_REAL_NEWSAPI_KEY"
```
4. Compile:
```powershell
javac --add-modules jdk.httpserver -d out src/NewsApp.java
```
5. Run:
```powershell
java --add-modules jdk.httpserver -cp out NewsApp
```

Open:
- http://localhost:8080/
- http://localhost:8080/api/news/headlines?country=us
- http://localhost:8080/api/news/headlines?country=us&category=technology

The headlines endpoint forwards the NewsAPI JSON response. API key, endpoint access, plan limits, and supported country/category combinations are controlled by NewsAPI.org.

## Docker: build, run, and verify

Run commands from the project root:

```powershell
docker build -t YOUR_DOCKERHUB_USERNAME/java-news-app:1.0 .
docker run -d --name java-news-app -p 8080:8080 -e NEWSAPI_KEY=YOUR_REAL_NEWSAPI_KEY YOUR_DOCKERHUB_USERNAME/java-news-app:1.0
docker ps
docker logs java-news-app
curl.exe "http://localhost:8080/api/news/headlines?country=us"
```

Push to Docker Hub:
```powershell
docker login
docker push YOUR_DOCKERHUB_USERNAME/java-news-app:1.0
```

Stop/remove:
```powershell
docker stop java-news-app
docker rm java-news-app
```

## GitHub repository

Create an empty repository on GitHub named `java-news-app`, then run in the project root:
```powershell
git init
git add .
git commit -m "Initial plain Java NewsAPI application"
git branch -M main
git remote add origin https://github.com/YOUR_GITHUB_USERNAME/java-news-app.git
git push -u origin main
```

For later updates:
```powershell
git add .
git commit -m "Describe your changes"
git push
```

## Security and troubleshooting
- Never commit your real API key. The `.gitignore` excludes `.env`; the app reads the key from `NEWSAPI_KEY`.
- If the API returns an error, verify the key and NewsAPI account plan/endpoint permissions.
- If port 8080 is busy, stop the other app or set `$env:PORT="8081"` and map Docker to that port as needed.
- NewsAPI's free/developer plan may limit use to development/testing. Check current NewsAPI terms before public production deployment.
