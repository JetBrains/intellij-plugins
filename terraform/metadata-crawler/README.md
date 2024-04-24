# Metadata generation

Metadata for Terraform is collected and stored in the jar-file and then bundled into plugin to provide completion and validation

For collecting the metadata, the [TerraformProvidersMetadataBuilder](ls-schemas-extractor/src/TerraformProvidersMetadataBuilder.kt)
is used

## Using Docker to collect metadata and publish a jar file
1. Set up Docker on the local computer
2. Generate Docker image `./gradlew clean buildDockerImage`
3. Push the image to a Docker registry of your choice using the command: `docker push intellij.terraform/metadata-crawler:<IMAGE_VERSION>` (optional)
4. Run the image: `docker run -d intellij.terraform/metadata-crawler:<IMAGE_VERSION> publish`
5. The application will collect metadata, build artifact and push it to the repository according to default settings. If we need to alter defaults, we need to redefine environment variables (see the table below).

For the image, we can specify the following environment configuration parameters

| Variable Name              | Default Value                                          | Comment                                                           |
|----------------------------|--------------------------------------------------------|-------------------------------------------------------------------|
| TERRAFORM_REGISTRY_HOST    | https://registry.terraform.io                          |                                                                   |
| PROVIDERS_IN_REGISTRY      | 4108                                                   | Minimum providers that can be fetched from the registry           |
| DOWNLOADS_LIMIT_FOR_PROVIDER | 10000                                                 | Minimum download numbers for provider to include it into metadata |
| MANDATORY_PROVIDERS_COUNT  | 33                                                     | Nesessary providers number from the `hashicorp` namespace         |
| ARTIFACT_GROUP             | org.intellij.plugins.hcl.terraform                     | Maven coordinates for the metadata jar                            |
| ARTIFACT_VERSION           | 2023.3.0                                               | Metadata version                                                  |
| REPO_URL                   | https://packages.jetbrains.team/maven/p/ij/intellij-dependencies | Maven repository URL                                              |
| REPO_USERNAME              | spaceUsername                                          | Maven repository username                                         |
| REPO_PASSWORD              | spacePassword                                          | Maven repository password                                         |

For example, if we want to change an artifact version, we can specify it in the command line:
`docker run -d -e ARTIFACT_VERSION=2023.3.1  intellij.terraform/metadata-crawler:<IMAGE_VERSION> publish`

ℹ️ PROVIDERS_IN_REGISTRY and MANDATORY_PROVIDERS_COUNT settings will fail the build only if assertions are enabled.
Add `-e LS_SCHEMAS_EXTRACTOR_OPTS=-ea` to the docker start command to enable assertions.

### How to build artifact only
1. If we don't want to publish metadata, we can bind a local folder to a container folder and get the metadata 
jar file: `docker run -d -v <LOCAL_FOLDER>:/opt/terraform-metadata/build/libs intellij.terraform/metadata-crawler:<IMAGE_VERSION> jar`
2. After execution finished, we can publish the jar manually.