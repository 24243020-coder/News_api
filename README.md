## Docker: build, run, and verify

Run commands from the project root:

```powershell
docker build -t YOUR_DOCKERHUB_USERNAME/java-news-app:1.0 .
docker run -d --name java-news-app -p 8080:8080 -e NEWSAPI_KEY=af975c15809544e8b9e2735d50d11a9a/java-news-app:1.0
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
