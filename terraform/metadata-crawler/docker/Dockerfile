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

#Copy existing metadata resources
COPY terraform/ /opt/terraform-metadata

#Install schemas extractor
COPY ls-schemas-extractor.zip /opt/terraform-metadata
RUN cd /opt/terraform-metadata && unzip /opt/terraform-metadata/ls-schemas-extractor.zip
RUN rm /opt/terraform-metadata/ls-schemas-extractor.zip
RUN chmod +x /opt/terraform-metadata/ls-schemas-extractor/bin/ls-schemas-extractor
RUN mkdir -p /opt/terraform-metadata/ls-schemas-extractor/plugins-meta

# Set working directory
WORKDIR /opt/terraform-metadata
# Pre-download all project dependencies
RUN ./gradlew --no-daemon dependencies

ENTRYPOINT ["bash"]

VOLUME ["/opt/terraform-metadata/build/libs"]