.PHONY: upload

upload:
	@mvn clean package;
	@source .envrc;
	@echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin;
	@docker build -t abonnin33/opensky-puller:latest .;
	@sleep 1;
	@docker push abonnin33/opensky-puller:latest;