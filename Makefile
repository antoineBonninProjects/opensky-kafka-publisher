.PHONY: upload build run

upload:
	@mvn clean package;
	@source .envrc;
	@echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin;
	@docker build -t abonnin33/opensky-kafka-publisher:latest .;
	@sleep 1;
	@docker push abonnin33/opensky-kafka-publisher:latest;

build:
	@mvn clean package;

run:
	@java -jar target/opensky_kafka_publisher-1.0.jar;