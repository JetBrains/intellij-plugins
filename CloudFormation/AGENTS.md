# CloudFormation Module Notes

## Scope

These notes are specific to `contrib/CloudFormation` and its `metadata-crawler` subproject.

## Architecture

- Runtime resource metadata is not generated from sources in this module at IDE startup. The plugin reads `com/intellij/aws/meta/cloudformation-metadata.xml` and `cloudformation-descriptions.xml` from the external `cloudformation-meta` jar via `CloudFormationMetadataProvider`.
- The consumed metadata artifact is wired through `intellij.cloudFormation.iml` and packaged through `plugin-content.yaml` as `intellij.aws.cloudformation.meta.jar`.
- Changing only crawler code does not change editor behavior until a new metadata jar is built/published and the plugin consumes that refreshed artifact.

## Metadata Crawler

- The crawler lives in `contrib/CloudFormation/metadata-crawler`.
- Default generated output goes to `build/generated/metadata/com/intellij/aws/meta/`.
- `metadata-crawler/build.gradle` syncs shared runtime sources from `../src/main/kotlin` into `build/generated/shared-cloudformation-sources`. If you change metadata model classes used by both runtime and crawler, keep this shared-source flow in mind.
- `MetadataComparatorMain` compares gathered metadata against jar contents. If the metadata model changes, update comparator reporting as needed, not just the serializer and crawler.

## SAM-Specific Knowledge

- AWS SAM resource type discovery is collected in `ResourceTypesSaver` from the official SAM resources documentation page, not from a hardcoded list.
- AWS SAM `Globals` support is also collected in `ResourceTypesSaver`, from the official SAM globals documentation page, and persisted in `CloudFormationMetadata.serverlessGlobals`.
- Runtime `Globals` validation in `CloudFormationInspections` should use collected metadata from `CloudFormationMetadata`, not a separate hardcoded property list.
- If new SAM resources are missing from completion or are reported as unknown, check the crawler output and the consumed metadata jar before changing completion logic.

## Update Workflow

1. Update crawler parsing or metadata model.
2. Verify generated metadata locally with `MetadataComparatorMain` or by inspecting the generated metadata output.
3. Ask the User to build and publish a refreshed `cloudformation-meta` jar.
4. Update the consumed artifact version/path if required so the runtime plugin actually sees the new metadata.

## Useful Commands

- Compare gathered metadata with a jar: run `MetadataComparatorMain` with either `<jar-path>`, `<gathered-dir> <jar-path>`, or `<left-jar> <right-jar>`

## Tests

- If completion or inspection behavior changes, add or update tests under `contrib/CloudFormation/src/test/kotlin/com/intellij/aws/cloudformation/tests/` and matching `testData` files.
- `YamlExamplesTest` treats "no problems" as the absence of a `*.expected` file. Do not add an empty expected file for clean examples.
- If you modify iml files, run the `IntelliJConfigurationFilesFormatTest` to verify the formatting.

## Final steps

- Update this file if you find something importante to know in the next sessions
