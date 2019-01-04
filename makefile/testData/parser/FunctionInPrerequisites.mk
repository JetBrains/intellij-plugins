var/docker_lock: $(shell find .docker/ -type f) docker-compose.yaml
	$(MAKE) docker-build
	touch var/docker_lock