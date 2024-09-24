FROM ubuntu:latest AS builder

RUN apt-get update && \
    apt-get install -y --no-install-recommends tzdata less sed default-jdk maven

WORKDIR /source
COPY . .
RUN mvn clean compile package -DskipTests=true

WORKDIR /source/src/main/resources
RUN sed -i "/spring\.data\.mongodb\.host.*$/ s/=.*/= mongodb/" application.properties &&  \
    sed -i "/spring\.data\.redis\.host.*$/ s/=.*/= redis/" application.properties &&  \
    sed -i "s/localhost:3100/loki:3100/g" logback.xml

FROM ubuntu:latest

RUN apt-get update &&  \
    apt-get upgrade -y &&  \
    apt-get install -y --no-install-recommends tzdata default-jdk &&  \
    ln -snf /usr/share/zoneinfo/Etc/UTC /etc/localtime && echo "Etc/UTC" > /etc/timezone &&  \
    apt-get autoclean -y &&  \
    apt-get autoremove -y &&  \
    apt-get clean -y &&  \
    rm -rf /var/lib/apt/lists/* &&  \
    mkdir -p /app -m 755

WORKDIR /app
COPY --from=builder /source/target/original-*.jar /app/app.jar
COPY --from=builder /source/src/main/resources/* /app/

CMD [ "java", "-jar", "/app/app.jar" ]
