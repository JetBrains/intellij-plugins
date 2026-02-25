# CloudFormation Metadata Crawler

This project collects AWS CloudFormation metadata and packages it as a Maven artifact used by the CloudFormation plugin.

## Build Metadata Jar

```bash
./gradlew clean metadataJar
```

Generated artifact:

- `build/dist/cloudformation-meta-<ARTIFACT_VERSION>.jar`


## Publish Metadata Jar

```bash
./gradlew clean publishMetadataPublicationToMavenRepository
```

## TeamCity

Configuration to use

https://buildserver.labs.intellij.net/buildConfiguration/ijplatform_master_CloudFormationMetadata_Build#all-projects

## Compare Metadata

Use the `MetadataComparatorMain` entry point (for example via an IDE run configuration):

- `<jar-path>`
- `<gathered-metadata-dir> <jar-path>`
- `<left-jar-path> <right-jar-path>`
