FROM gradle:7-jdk17

RUN apt-get update && \
    apt-get install -y libopenjfx-java && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY . /app