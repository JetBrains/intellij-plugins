## Full release change-notes history

### For newest releases see `<change-notes>` in [plugin.xml](https://github.com/VladRassokhin/intellij-hcl/blob/master/res/META-INF/plugin.xml)

#### 0.6.13

*   Fixed order of unary and select operations in interpolations (#179)  
*   Improved formatting (#141)  
*   Fixed exception in properties completion (#182)  
*   Reduced plugin memory consumption, speed up completion/inspections  
*   Updated model data to match Terraform v0.11.11  

#### 0.6.12

*   Added 'Conflicting properties' inspection  
*   Added 'Missing modules' inspection with 'run terraform get' quick fix  
*   Fixed heredoc formatting inside arrays (#153)  
*   Improved module references support  
*   Updated model data to match Terraform v0.11.8  

#### 0.6.11

*   Fixed 'Condition should be boolean' inspection on references (#147)  
*   Fixed 'Argument rangeInElement' exception (#151)  
*   Updated model data to match Terraform v0.11.7  

#### 0.6.10

*   Support `//noinspection` comments, quick fix to suppress inspections (#138)  
*   Fixed parsing escaped quotes in interpolations (#111)  
*   Improved completion items description rendering (#143)  
*   Completion/Navigation for module providers mapping (#130)  
*   Fixed missing properties inspection for newly added 'module' block (#139)  
*   Support 'timeouts' block in resources (#145)  
*   Updated model data to match Terraform v0.11.5  

#### 0.6.9.5

*   Updated model data to match Terraform v0.11.3  

#### 0.6.9.4

*   Updated model data to match Terraform v0.11.2  

#### 0.6.9.3

*   Show gutter 'run' icon for resource names, not full blocks (#124)  
*   Updated model data to match Terraform v0.11.1  

#### 0.6.9.2

*   Fix exception on IntelliJ platform older than 2017.3 (#123)  

#### 0.6.9.1

*   Fix inspection for duplicated properties inside one block (#122)  

#### 0.6.9

*   Added `terraform fmt` template for `File Watchers` plugin(#71)  
*   Fixed rename refactoring not accepting correct names (#112)  
*   Ability to select line comment prefix (`//` or `#`) (#81, #120)  
*   Inspection for duplicated properties inside one block (#119)  
*   Updated model data to match Terraform v0.11.0  

#### 0.6.8

*   Updated model data to match Terraform v0.10.8  

#### 0.6.7

*   Add action to run 'terraform fmt' (#71)  
*   Add support for 'locals' block and related completion, navigation, find usages (#106)  
*   Add 'Go to Definition' support for module parameters (#99)  

#### 0.6.6

*   Fixed quotes and right curly brace handling in interpolations in TF files (#102)  
*   Fixed environment variables handling in run configurations (#101)  
*   Context run configurations for files and single resources  
*   Gutter markers for per-resource run configurations  
*   Updated model data to match Terraform v0.10.3  

#### 0.6.5

*   Added simple Terraform run configuration  
*   Report missing braces in some cases (#94)  
*   Some minor fixes  
*   Updated model data to match Terraform v0.10.0  

#### 0.6.4

*   Inspection for duplicated output/variable definition  
*   Fixed compatibility with IntelliJ 172 branch (#92)  
*   Fixed autoinsertion of '}' after typing '${' in some cases (#91)  
*   Some hexadecimal numbers were highlighted erroneous (#90)  

#### 0.6.3

*   Support for `backend` blocks in `terraform` block  
*   Custom folding support: `region/endregion` and `<editor-fold>  
*   Required properties added automatically on resource/data type completion and in generators  
*   Smart completion in interpolations provides best suited results for some properties, e.g. vpc_id in aws resources  
*   Move statement up/down doesn't breaks syntax anymore  
*   Other improvements and bugfixes in completion  
*   Updated model data to match Terraform v0.9.5  

#### 0.6.2

*   Support `${terraform.env}` interpolation  
*   Improve resolving for 'computed' map variables  
*   Updated model data to match Terraform v0.9.4  

#### 0.6.1

*   Added 'Add missing variable' quick fix  
*   Do not highlight terraform_remote_state parameters as missing  
*   Updated Terraform model data to match TF v0.9.3  

#### 0.6.0

*   Added 'Introduce Variable' refactoring (#50)  
*   Resource and Data Source completion variants for unused providers no longer shown in basic completion (#77)  
*   Added completion, Go To Definition and find usages from module 'source' property for relative path cases (#73)  
*   Updated terraform model data to match Terraform v0.9.0

#### 0.5.12.2

*   Updated terraform model data to match Terraform v0.8.6  
*   Minor internal refactoring, plugin size reduced a bit  

#### 0.5.12.1

*   Support 'terraform' blocks in .tf files (#68)  
*   Updated terraform model data to match Terraform v0.8.4  

#### 0.5.12

*   Allow 'self' references in interpolation in resource connection blocks (#57)  
*   Support comparison and boolean operations in interpolation (#59)  
*   Report incorrect argument types for comparison, ternary and boolean operations in interpolations (simple cases only)  
*   Properly insert closing curve brace once interpolation start typed (#63)  
*   Fixed 'module' keyword highlighting and completion in interpolations (#62)  
*   Completion for module output in interpolations, module inputs no longer advised in interpolations completion (#52)  
*   Added completion for module parameters as well as 'missing parameter' inspection (#65, #66)  
*   Updated terraform model data to match Terraform v0.8.1  

#### 0.5.11.1

*   Fixed IAE (#55)  
*   Fixed incorrect backslash escape handling (#56)  
*   Removed Otto support since that tool was decommissioned  

#### 0.5.11

*   Support Terraform 0.8.0  
*   'Missing required property' inspection now ignores required properties with 'default' value. (#53)  
*   Multiline string literals supported only if there's interpolation inside.  
*   Backslash escaping changed in interpolations to match Terraform 0.8 style. [Details in HCL repo](https://github.com/hashicorp/hcl/pull/150).  
*   Do not highlight interpolation elements as unresolved reference for some holders, e.g. `VAR` in `data.consul.NAME.var.VAR` (#51)  
*   Updated terraform model data to match Terraform v0.8.0-rc1  

#### 0.5.10

*   'Copy Reference' action would copy FQN of resource, provider, etc. (#48)  
*   Updated terraform model data to match Terraform 0.7.5 (#49)  

#### 0.5.9

*   Updated terraform model data to match Terraform 0.7.4  

#### 0.5.8

*   Add references and completion variants for 'depends_on' in resources and data sources (#43)  
*   Updated terraform model data to match Terraform 0.7.2  

#### 0.5.7

*   Fixed incorrect inspection in .tfvars files for numeric values (#41)  
*   Fixed double quote inserter in Terraform files, now closing double quote would be added automatically if needed  
*   Improved missing properties generation in 'Missing required property' inspection based on expected property type  
*   Improved property name completion elements presentation  
*   Variable type could be autocompleted from three variants: string, list or map  
*   Variable type inspection (for 'type' and 'default' properties)  
*   Report duplicated providers with same name/alias as error  
*   Report usage of interpolations in variables, 'depends_on' and module source as error  

#### 0.5.6

*   Fixed incorrect inspection in .tfvars files (#39)  
*   Support references to elements of 'map' variables in .tfvars files (#41)  
*   Code completion in .tfvars files (#41)  
*   Support multiline interpolations in heredocs (#40)  

#### 0.5.5

*   Support Terraform 0.7.0 (issues: #31, #33, #36, #37)  
*   Updated terraform model data to match Terraform 0.7.0  

#### 0.5.4

*   Fixed error on rename refactoring (issue #29)  
*   Fixed parsing heredocs with indented end token (issue #30)  
*   Fixed interpolation escape (`$$`) (issue #34)  
*   Updated terraform model data to match Terraform 0.6.16  

#### 0.5.3

*   Updated terraform model data to match Terraform 0.6.15 (issue #28)  

#### 0.5.2

*   Updated terraform model data to match Terraform 0.6.14  
*   Support indented heredocs (issue #27)  
*   Support '.tfvars' files (issue #24)  
*   Fixed exception during formatting a .tf file with invalid syntax (issue #26)  

#### 0.5.1

*   Updated terraform model data to match Terraform 0.6.12  
*   Fix references to resource 'id' property reported as unresolved (issue #21)  
*   Fixes around unary operators in HIL  
*   Support indexing into variables in HIL ('${a[2]}') (issue #19)  
*   Various fixes  

#### 0.5.0

*   Customize highlighting text attributes (colour, etc)  
*   Improved Find Usages, Go to definition, Rename refactoring support.  
*   Support custom language injections in heredoc (issue #17)  
*   Added inspections in Terraform interpolations: incorrect use of 'self', 'unknown resource type', **'unresolved reference'**  
*   Added actions into 'Generate ...' popup. Use with `alt+insert`(`ctrl+n` on OSX)  

#### 0.4.4

*   Added 'deprecated parameter or block type' inspection.  
*   Updated terraform model data to match Terraform 0.6.11  
*   Minor internal refactoring  

#### 0.4.3

*   Updated terraform model data to match Terraform 0.6.8  

#### 0.4.2

*   Fix false 'unknown block' inspection result (issue #12)  
*   Fix compatibility with non IDEA IDEs (issue #13)  

#### 0.4.1

*   Fix ClassCastException (issue #11)  

#### 0.4.0

*   Autocompletion in interpolations  
*   Go to definition from interpolations to provider/resources properties, etc.  
*   Find usages of provider/resources properties, etc. in interpolations  
*   Inplace properties renaming (with usages tracking)  

#### 0.3.5

*   Updated terraform model data to match Terraform 0.6.5  
*   Added 'unknown block type' inspection.  

#### 0.3.4

*   Improvements in completion and 'required property is missing' inspection.  

#### 0.3.3

*   Improvements in completion. Now it available for almost all blocks properties (resource, provider, variable, etc)  
*   Missing required properties inspection improved.  

#### 0.3.2

*   Enable resource type and properties completion introduced in 0.3.0  

#### 0.3.1

*   Register '.nomad' (Nomad) and 'Appfile' (Otto) files as HCL files  

#### 0.3.0

*   Added Inspection for missing required properties in providers.  

#### 0.2.8, 0.2.9

*   Another minor improvement in code formatter around caret placement after pressing `Enter`  

#### 0.2.7

*   Code formatter improved  

#### 0.2.6

*   Support heredoc values (issue #8)  

#### 0.2.5

*   Compatibility with 139.X platform branch  

#### 0.2.4

*   Improvements around incorrect files  
*   Now numbers like '10Gb' properly handled  

#### 0.2.3

*   Fixes issue #6: Double quote handled incorrectly in interpolation  

#### 0.2.2

*   Fixes around Auto-create closing quotes, braces, brackets (issue #4)  
*   Improve interpolation language injector  

#### 0.2.1

*   Auto-create closing quotes, braces, brackets (issue #4)  
*   Highlight matching bracket signs (issue #5)  

#### 0.2.0

*   Terraform Interpolation language support (automatically injected in .tf files)  

#### 0.1.4

*   Fix grammar: identifiers may contain numbers  

#### 0.1.3

*   Register for '.tf' files extension  
*   Minor improvements  

#### 0.1.2

*   Comment/Uncomment line action  
*   'Structure' tool window support  
*   Objects and arrays folding in editor  

#### 0.1.1

*   Recompiled for Java 6  

#### 0.1

*   Syntax highlighting