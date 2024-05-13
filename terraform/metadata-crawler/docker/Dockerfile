FROM eclipse-temurin:17-jdk-jammy

#Install terraform
RUN apt-get update && apt-get install -y gnupg software-properties-common && apt-get install -y unzip
RUN wget -O- https://apt.releases.hashicorp.com/gpg | \
    gpg --dearmor | \
    tee /usr/share/keyrings/hashicorp-archive-keyring.gpg > /dev/null
RUN gpg --no-default-keyring \
    --keyring /usr/share/keyrings/hashicorp-archive-keyring.gpg \
    --fingerprint
RUN echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] \
https://apt.releases.hashicorp.com $(lsb_release -cs) main" | \
tee /etc/apt/sources.list.d/hashicorp.list
RUN apt update
RUN apt-get -yq install terraform

#Copy existing resources
COPY terraform/ /opt/terraform-metadata

#Enable schemas extractor
RUN chmod +x /opt/terraform-metadata/ls-schemas-extractor/bin/ls-schemas-extractor

# Set working directory
WORKDIR /opt/terraform-metadata

ENV TERRAFORM_REGISTRY_HOST=https://registry.terraform.io
ENV DOWNLOADS_LIMIT_FOR_PROVIDER=10000
ENV MANDATORY_PROVIDERS_COUNT=33
ENV ARTIFACT_GROUP=org.intellij.plugins.hcl.terraform
ENV ARTIFACT_VERSION=2024.1.2
ENV CLEAN_DOWNLOADED_DATA=true

ENV REPO_URL=https://packages.jetbrains.team/maven/p/ij/intellij-dependencies
ENV REPO_USERNAME=spaceUsername
ENV REPO_PASSWORD=spacePassword

# Pre-download all project dependencies
RUN ./gradlew --no-daemon dependencies

ENTRYPOINT ["./gradlew", "--no-daemon"]