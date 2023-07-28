# IntelliJ-HCL plugin

Provides [HCL language](https://github.com/hashicorp/hcl) and [Terraform](https://terraform.io) configuration files (`.tf`) support for [IntelliJ Platform](https://www.jetbrains.org/pages/viewpage.action?pageId=983889)-based IDEs

The HCL format is used for [Nomad](https://www.nomadproject.io/)(`.nomad` files).

[Plugin page](https://plugins.jetbrains.com/plugin/7808) in [JetBrains Marketplace](https://plugins.jetbrains.com).

### Features:

##### For both .hcl and .tf file formats:

* Syntax highlighting
* Structure outline in the 'Structure' tool window
* Code formatter with the 'Reformat code' action available
* Code folding
* Comment/Uncomment action

#### Terraform configs (.tf) files

* Interpolations syntax highlighting
* (WIP) Properties validation (according to the properties required for resource/provider, type checking)
* (WIP) Go to definition from resource to provider

#### Terraform configs Interpolation Language

* Syntax highlighting
* Autocompletion for [predefined methods](https://www.terraform.io/docs/configuration/interpolation.html) 
* (WIP) Go to declaration on resources, providers, properties, etc.
