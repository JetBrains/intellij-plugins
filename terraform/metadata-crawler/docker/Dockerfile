# Base layer with OpenJDK and Terraform
FROM gradle:7.6.4-jdk17-jammy AS base
#Install wget
RUN apt-get update && apt-get install -y gnupg software-properties-common unzip wget
#Install Terraform
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

# Builder layer, where we build the crawler's folder structure
FROM base AS builder
#Copy existing resources
COPY terraform-metadata/ /opt/terraform-metadata
#Enable schemas extractor
RUN chmod +x /opt/terraform-metadata/ls-schemas-extractor/bin/ls-schemas-extractor
RUN mkdir /opt/terraform-metadata/plugins-meta
ENV TERRAFORM_REGISTRY_HOST=https://registry.terraform.io
ENV DOWNLOADS_LIMIT_FOR_PROVIDER=20000
ENV CLEAN_DOWNLOADED_DATA=true
WORKDIR /opt/terraform-metadata

# Final layer, here we will actually run our application
FROM builder
RUN gradle --no-daemon dependencies
ENTRYPOINT ["gradle", "--no-daemon"]