# Metadata generation

Metadata for Terraform is collected and stored in the jar-file and then bundled into plugin to provide completion and validation

For collecting the metadata the [TerraformProvidersMetadataBuilder](ls-schemas-extractor/src/TerraformProvidersMetadataBuilder.kt)
is used

## Using Ansible to collect metadata on remote server

1. Set up [Ansible](https://docs.ansible.com/ansible/latest/installation_guide/intro_installation.html)
2. Set up a virtual machine in a cloud or locally, please note that following task will require about 4Gb of storage volume
3. Set up created VM as host to run in the `inventory.yaml`
    ``` 
    all:
     hosts:
      my-agent:
        ansible_host: <ip-of-your-vm>
    ```
4. run `ansible-playbook ansible/playbook.yaml -i ansible/inventory.yaml` in the `metadata-crawler` directory
5. in the `plugins-meta` folder you will get the updated metadata for providers, check the `failed` folder for errors

## Building and publishing the terraform-metadata jar

1. Copy metadata from `plugins-meta` folder to the `terraform/model` directory
2. Update the metadata version in `build.gradle` file: `version = '2023.1.0'`
3. Run `./gradlew clean jar publish` in order to create and publish Terraform jar.
4. Add the dependency on the produced jar to the Terraform IntelliJ plugin replacing the old one.