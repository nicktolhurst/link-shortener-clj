#-- BUILDER CONTAINER --#
FROM clojure:openjdk-15-tools-deps AS BUILD

ADD deps.edn /deps.edn
RUN clj -Sdeps '{:mvn/local-repo "./src/linkshortener"}'
COPY ./uberdeps /uberdeps
COPY ./src /src

RUN /uberdeps/package.sh


#-- RUNTIME CONTAINER --#
FROM openjdk:15-slim-buster AS API

COPY --from=BUILD /target/project.jar /project.jar

EXPOSE 8080
ENV MALLOC_ARENA_MAX=2

ENTRYPOINT ["java", "-cp", "project.jar", "clojure.main", "-m", "linkshortener.core"]