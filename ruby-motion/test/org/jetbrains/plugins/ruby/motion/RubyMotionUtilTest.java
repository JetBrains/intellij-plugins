/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.ruby.motion;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;
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
    assertEquals(((RubyMotionUtilImpl)RubyMotionUtil.getInstance()).getDefaultSdkVersion(RubyMotionUtilImpl.ProjectType.IOS), result.first);
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
    final List<String> names = ContainerUtil.map(frameworks, framework -> framework.getName());
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
