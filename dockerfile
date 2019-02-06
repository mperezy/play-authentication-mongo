FROM hseeberger/scala-sbt
# For a Alpine Linux version, comment above and uncomment below:
# FROM 1science/sbt

RUN mkdir -p /exampleapp
RUN mkdir -p /exampleapp/out

WORKDIR /exampleapp

COPY . /exampleapp

CMD sbt run