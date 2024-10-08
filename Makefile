.PHONY: test build run upload

test:
	@mvn test;

build:
	@mvn clean package;
	@docker build -t abonnin33/opensky-kafka-publisher:latest .;

run:
	@java -jar target/opensky-kafka-publisher-1.0.jar;

upload:
	@$(MAKE) build;
	@/bin/bash -c 'set -a; source .envrc; set +a; echo "$$DOCKER_PASSWORD" | docker login -u "$$DOCKER_USERNAME" --password-stdin';
	@docker push abonnin33/opensky-kafka-publisher:latest;