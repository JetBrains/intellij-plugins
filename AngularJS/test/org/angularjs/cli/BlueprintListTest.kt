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
    @angular/cli:
      class <name> <options...>
        --spec (Boolean)
          aliases: -spec
      component <name> <options...>
        --flat (Boolean)
          aliases: -flat
        --inline-template (Boolean)
          aliases: -it, --inlineTemplate
        --inline-style (Boolean)
          aliases: -is, --inlineStyle
        --prefix (String) (Default: null)
          aliases: --prefix <value>
        --spec (Boolean)
          aliases: -spec
        --view-encapsulation (String)
          aliases: -ve <value>, --viewEncapsulation <value>
        --change-detection (String)
          aliases: -cd <value>, --changeDetection <value>
        --skip-import (Boolean) (Default: false)
          aliases: --skipImport
        --module (String)
          aliases: -m <value>, --module <value>
        --export (Boolean) (Default: false)
          aliases: --export
      directive <name> <options...>
        --flat (Boolean)
          aliases: -flat
        --prefix (String) (Default: null)
          aliases: --prefix <value>
        --spec (Boolean)
          aliases: -spec
        --skip-import (Boolean) (Default: false)
          aliases: --skipImport
        --module (String)
          aliases: -m <value>, --module <value>
        --export (Boolean) (Default: false)
          aliases: --export
      enum <name>
      interface <interface-type>
      module <name> <options...>
        --spec (Boolean)
          aliases: -spec
        --flat (Boolean)
          aliases: -flat
        --routing (Boolean) (Default: false)
          aliases: --routing
      ng2 <name> <options...>
        --source-dir (String) (Default: src)
          aliases: -sd <value>, --sourceDir <value>
        --prefix (String) (Default: app)
          aliases: -p <value>, --prefix <value>
        --style (String) (Default: css)
          aliases: --style <value>
        --routing (Boolean) (Default: false)
          aliases: --routing
        --inline-style (Boolean) (Default: false)
          aliases: -is, --inlineStyle
        --inline-template (Boolean) (Default: false)
          aliases: -it, --inlineTemplate
        --skip-git (Boolean) (Default: false)
          aliases: -sg, --skipGit
      pipe <name> <options...>
        --flat (Boolean)
          aliases: -flat
        --spec (Boolean)
          aliases: -spec
        --skip-import (Boolean) (Default: false)
          aliases: --skipImport
        --module (String)
          aliases: -m <value>, --module <value>
        --export (Boolean) (Default: false)
          aliases: --export
      service <name> <options...>
        --flat (Boolean)
          aliases: -flat
        --spec (Boolean)
          aliases: -spec
        --module (String)
          aliases: -m <value>, --module <value>

ng generate <blueprint> <options...>
  Generates new code from blueprints.
  aliases: g
  --dry-run (Boolean) (Default: false)
    aliases: -d, --dryRun
  --verbose (Boolean) (Default: false)
    aliases: -v, --verbose
  --pod (Boolean) (Default: false)
    aliases: -p, -pod
  --classic (Boolean) (Default: false)
    aliases: -c, --classic
  --dummy (Boolean) (Default: false)
    aliases: -dum, -id, --dummy
  --in-repo-addon (String) (Default: null)
    aliases: --in-repo <value>, -ir <value>, --inRepoAddon <value>

    """

    val blueprints = BlueprintParser().parse(output)
    val requiredBlueprints = Arrays.asList("class", "component", "module")
    val existingBlueprints = ContainerUtil.filter(blueprints) { requiredBlueprints.contains(it.name) }
    TestCase.assertEquals(3, existingBlueprints.size)
    TestCase.assertEquals(listOf("--flat", "--inline-template", "--inline-style", "--prefix", "--spec",
                                 "--view-encapsulation", "--change-detection", "--skip-import", "--module", "--export"),
                          existingBlueprints[1].args)
  }
}
