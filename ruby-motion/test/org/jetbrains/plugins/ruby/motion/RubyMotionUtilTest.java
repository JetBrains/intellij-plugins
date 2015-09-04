package org.jetbrains.plugins.ruby.motion;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.plugins.ruby.motion.bridgesupport.Framework;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyElementFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionUtilTest extends RubyMotionLightFixtureTestCase {
  @Override
  protected String getTestDataRelativePath() {
    return "testApp";
  }

  public void testSdkAndFrameworksReading() {
    Pair<String,String[]> result;
    result = RubyMotionUtil.getInstance().calculateSdkAndFrameworks(createRakefile("# -*- coding: utf-8 -*-\n" +
                                                                                   "$:.unshift(\"/Library/RubyMotion/lib\")\n" +
                                                                                   "require 'motion/project'\n" +
                                                                                   "\n" +
                                                                                   "Motion::Project::App.setup do |app|\n" +
                                                                                   "  # Use `rake config' to see complete project settings.\n" +
                                                                                   "  app.name = 'TestApp'\n" +
                                                                                   "end"));
    assertEquals(RubyMotionUtil.getInstance().getDefaultSdkVersion(RubyMotionUtil.ProjectType.IOS), result.first);
    assertSameElements(result.second, "CoreGraphics", "Foundation", "UIKit");

    result = RubyMotionUtil.getInstance().calculateSdkAndFrameworks(createRakefile("# -*- coding: utf-8 -*-\n" +
                                                                                   "$:.unshift(\"/Library/RubyMotion/lib\")\n" +
                                                                                   "require 'motion/project'\n" +
                                                                                   "\n" +
                                                                                   "Motion::Project::App.setup do |app|\n" +
                                                                                   "  # Use `rake config' to see complete project settings.\n" +
                                                                                   "  app.name = 'TestApp'\n" +
                                                                                   "  app.sdk_version = '4.3'\n" +
                                                                                   "end"));
    assertEquals("4.3", result.first);
    assertSameElements(result.second, "CoreGraphics", "Foundation", "UIKit");

    result = RubyMotionUtil.getInstance().calculateSdkAndFrameworks(createRakefile("# -*- coding: utf-8 -*-\n" +
                                                                                   "$:.unshift(\"/Library/RubyMotion/lib\")\n" +
                                                                                   "require 'motion/project'\n" +
                                                                                   "\n" +
                                                                                   "Motion::Project::App.setup do |app|\n" +
                                                                                   "  # Use `rake config' to see complete project settings.\n" +
                                                                                   "  app.name = 'TestApp'\n" +
                                                                                   "  app.sdk_version = '4.3'\n" +
                                                                                   "  app.frameworks = ['UIKit', 'Foundation']\n" +
                                                                                   "end"));
    assertEquals("4.3", result.first);
    assertSameElements(result.second, "Foundation", "UIKit");

    result = RubyMotionUtil.getInstance().calculateSdkAndFrameworks(createRakefile("# -*- coding: utf-8 -*-\n" +
                                                                                   "$:.unshift(\"/Library/RubyMotion/lib\")\n" +
                                                                                   "require 'motion/project'\n" +
                                                                                   "\n" +
                                                                                   "Motion::Project::App.setup do |app|\n" +
                                                                                   "  # Use `rake config' to see complete project settings.\n" +
                                                                                   "  app.name = 'TestApp'\n" +
                                                                                   "  app.sdk_version = '4.3'\n" +
                                                                                   "  app.frameworks += ['GLKit']\n" +
                                                                                   "end"));
    assertEquals("4.3", result.first);
    assertSameElements(result.second, "CoreGraphics", "Foundation", "UIKit", "GLKit");

    result = RubyMotionUtil.getInstance().calculateSdkAndFrameworks(createRakefile("# -*- coding: utf-8 -*-\n" +
                                                                                   "$:.unshift(\"/Library/RubyMotion/lib\")\n" +
                                                                                   "require 'motion/project'\n" +
                                                                                   "\n" +
                                                                                   "Motion::Project::App.setup do |app|\n" +
                                                                                   "  # Use `rake config' to see complete project settings.\n" +
                                                                                   "  app.name = 'TestApp'\n" +
                                                                                   "  app.sdk_version = '4.3'\n" +
                                                                                   "  app.frameworks -= ['UIKit']\n" +
                                                                                   "end"));
    assertEquals("4.3", result.first);
    assertSameElements(result.second, "CoreGraphics", "Foundation");
  }

  public void testFrameworkResolution() {
    defaultConfigure();
    final Collection<Framework> frameworks = ((RubyMotionUtilImpl)RubyMotionUtil.getInstance()).getFrameworks(getModule());
    final List<String> names = ContainerUtil.map(frameworks, new Function<Framework, String>() {
      @Override
      public String fun(Framework framework) {
        return framework.getName();
      }
    });
    assertSameElements(names, "Accelerate",
                       "CFNetwork",
                       "CoreAudio",
                       "CoreFoundation",
                       "CoreGraphics",
                       "CoreImage",
                       "CoreMedia",
                       "CoreTelephony",
                       "CoreText",
                       "CoreVideo",
                       "Foundation",
                       "GLKit",
                       "ImageIO",
                       "MobileCoreServices",
                       "OpenGLES",
                       "QuartzCore",
                       "Security",
                       "SystemConfiguration",
                       "UIKit");
  }

  public void testFrameworkAdding() {
    final Pair<String, String[]> result = RubyMotionUtil.getInstance().calculateSdkAndFrameworks(createRakefile("# -*- coding: utf-8 -*-\n" +
                                                                     "$:.unshift(\"/Library/RubyMotion/lib\")\n" +
                                                                     "require 'motion/project'\n" +
                                                                     "\n" +
                                                                     "Motion::Project::App.setup do |app|\n" +
                                                                     "  # Use `rake config' to see complete project settings.\n" +
                                                                     "  app.name = 'TestApp'\n" +
                                                                     "  app.sdk_version = '4.3'\n" +
                                                                     "  app.frameworks << 'CoreData'\n" +
                                                                     "end"));
    assertContainsElements(Arrays.asList(result.second), "CoreData");
  }

  private PsiFile createRakefile(final String text) {
    return RubyElementFactory.createRubyFile(getProject(), text);
  }
}
