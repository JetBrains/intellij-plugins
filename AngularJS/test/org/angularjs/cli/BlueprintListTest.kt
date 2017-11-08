package org.angularjs.cli

import com.intellij.testFramework.LightPlatformTestCase
import com.intellij.util.containers.ContainerUtil
import junit.framework.TestCase
import java.util.*

/**
 * @author Dennis.Ushakov
 */
class BlueprintListTest : LightPlatformTestCase() {
  fun testList() {
    val output = """

  Available blueprints:
    class <name> <options...>
      --spec (Boolean) Specifies if a spec file is generated.
        aliases: -spec
      --app (String) Specifies app name to use.
        aliases: -a <value>, -app <value>
    component <name> <options...>
      --flat (Boolean) Flag to indicate if a dir is created.
        aliases: -flat
      --inline-template (Boolean) Specifies if the template will be in the ts file.
        aliases: -it, --inlineTemplate
      --inline-style (Boolean) Specifies if the style will be in the ts file.
        aliases: -is, --inlineStyle
      --prefix (String) (Default: null) Specifies whether to use the prefix.
        aliases: --prefix <value>
      --spec (Boolean) Specifies if a spec file is generated.
        aliases: -spec
      --view-encapsulation (String) Specifies the view encapsulation strategy.
        aliases: -ve <value>, --viewEncapsulation <value>
      --change-detection (String) Specifies the change detection strategy.
        aliases: -cd <value>, --changeDetection <value>
      --skip-import (Boolean) (Default: false) Allows for skipping the module import.
        aliases: --skipImport
      --module (String) Allows specification of the declaring module.
        aliases: -m <value>, --module <value>
      --export (Boolean) (Default: false) Specifies if declaring module exports the component.
        aliases: --export
      --app (String) Specifies app name to use.
        aliases: -a <value>, -app <value>
    directive <name> <options...>
      --flat (Boolean) Flag to indicate if a dir is created.
        aliases: -flat
      --prefix (String) (Default: null) Specifies whether to use the prefix.
        aliases: --prefix <value>
      --spec (Boolean) Specifies if a spec file is generated.
        aliases: -spec
      --skip-import (Boolean) (Default: false) Allows for skipping the module import.
        aliases: --skipImport
      --module (String) Allows specification of the declaring module.
        aliases: -m <value>, --module <value>
      --export (Boolean) (Default: false) Specifies if declaring module exports the component.
        aliases: --export
      --app (String) Specifies app name to use.
        aliases: -a <value>, -app <value>
    enum <name> <options...>
      --app (String) Specifies app name to use.
        aliases: -a <value>, -app <value>
    guard <name> <options...>
      --flat (Boolean) Indicate if a dir is created.
        aliases: -flat
      --spec (Boolean) Specifies if a spec file is generated.
        aliases: -spec
      --module (String) Allows specification of the declaring module.
        aliases: -m <value>, --module <value>
    interface <interface-type> <options...>
      --app (String) Specifies app name to use.
        aliases: -a <value>, -app <value>
    module <name> <options...>
      --spec (Boolean) Specifies if a spec file is generated.
        aliases: -spec
      --flat (Boolean) Flag to indicate if a dir is created.
        aliases: -flat
      --routing (Boolean) (Default: false) Specifies if a routing module file should be generated.
        aliases: --routing
      --app (String) Specifies app name to use.
        aliases: -a <value>, -app <value>
    pipe <name> <options...>
      --flat (Boolean) Flag to indicate if a dir is created.
        aliases: -flat
      --spec (Boolean) Specifies if a spec file is generated.
        aliases: -spec
      --skip-import (Boolean) (Default: false) Allows for skipping the module import.
        aliases: --skipImport
      --module (String) Allows specification of the declaring module.
        aliases: -m <value>, --module <value>
      --export (Boolean) (Default: false) Specifies if declaring module exports the pipe.
        aliases: --export
      --app (String) Specifies app name to use.
        aliases: -a <value>, -app <value>
    service <name> <options...>
      --flat (Boolean) Flag to indicate if a dir is created.
        aliases: -flat
      --spec (Boolean) Specifies if a spec file is generated.
        aliases: -spec
      --module (String) Allows specification of the declaring module.
        aliases: -m <value>, --module <value>
      --app (String) Specifies app name to use.
        aliases: -a <value>, -app <value>

ng generate <blueprint> <options...>
  Generates new code from blueprints.
  aliases: g
  --dry-run (Boolean) (Default: false) Run through without making any changes.
    aliases: -d, --dryRun
  --verbose (Boolean) (Default: false) Adds more details to output logging.
    aliases: -v, --verbose

    """

    val blueprints = BlueprintParser().parse(output)
    val requiredBlueprints = Arrays.asList("class", "component", "module", "service")
    val existingBlueprints = ContainerUtil.filter(blueprints) { requiredBlueprints.contains(it.name) }
    TestCase.assertEquals(requiredBlueprints.size, existingBlueprints.size)
    TestCase.assertEquals(listOf("--flat", "--inline-template", "--inline-style", "--prefix", "--spec", "--view-encapsulation",
                                 "--change-detection", "--skip-import", "--module", "--export", "--app"),
                          existingBlueprints[1].args)

    val blacklistedBlueprints = Arrays.asList("aliases:")
    val nonBlueprints = ContainerUtil.filter(blueprints) { blacklistedBlueprints.contains(it.name) }
    TestCase.assertEquals(0, nonBlueprints.size)
  }

  fun testNewList() {
    val output = "Available schematics:\n" +
                 "    application\n" +
                 "    class\n" +
                 "    component\n" +
                 "    directive\n" +
                 "    enum\n" +
                 "    guard\n" +
                 "    interface\n" +
                 "    module\n" +
                 "    pipe\n" +
                 "    service\n"
    val blueprints = BlueprintParser().parse(output)
    val requiredBlueprints = Arrays.asList("class", "component", "module", "service")
    val existingBlueprints = ContainerUtil.filter(blueprints) { requiredBlueprints.contains(it.name) }
    TestCase.assertEquals(requiredBlueprints.size, existingBlueprints.size)
  }
}
