FROM hseeberger/scala-sbt:8u222_1.3.5_2.13.1
# For a Alpine Linux version, comment above and uncomment below:
# FROM 1science/sbt

RUN mkdir -p /exampleapp
RUN mkdir -p /exampleapp/out

WORKDIR /exampleapp

COPY . /exampleapp

CMD sbt run