package org.jetbrains.plugins.ruby.motion;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.LightProjectDescriptor;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.PathUtil;
import org.jetbrains.plugins.ruby.RubyVMOptions;
import org.jetbrains.plugins.ruby.motion.facet.RubyMotionFacet;
import org.jetbrains.plugins.ruby.motion.facet.RubyMotionFacetType;
import org.jetbrains.plugins.ruby.motion.symbols.FunctionSymbol;
import org.jetbrains.plugins.ruby.motion.symbols.MotionClassSymbol;
import org.jetbrains.plugins.ruby.rails.RubyLightProjectDescriptorBase;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.sdk.LanguageLevel;
import org.jetbrains.plugins.ruby.ruby.testCases.RubyFixtureTestCase;
import org.jetbrains.plugins.ruby.ruby.testCases.RubyLightFixtureTestCase;
import org.jetbrains.plugins.ruby.ruby.testCases.RubyTestUtil;

/**
 * @author Dennis.Ushakov
 */
public abstract class RubyMotionLightFixtureTestCase extends RubyLightFixtureTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    RubyVMOptions.getInstance().forceLanguageLevel(LanguageLevel.RUBY19);
  }

  @Override
  protected void tearDown() throws Exception {
    RubyVMOptions.resetForcedLanguageLevel();
    super.tearDown();
  }

  @Override
  protected String getTestDataPath() {
    return PathUtil.getDataPath(getClass()) + "/" + getTestDataRelativePath();
  }

  @Nullable
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return new RubyMotionLightFixtureDescriptor();
  }

  protected PsiFile defaultConfigure() {
    return myFixture.configureByFiles("app/app_delegate.rb", "Rakefile")[0];
  }

  protected void checkResolveToObjC(final String signature, final String fqn) {
    final PsiReference ref = findReferenceBySignature(signature);
    final Symbol symbol = RubyTestUtil.resolveToSymbol(ref);
    TestCase.assertTrue(symbol instanceof MotionClassSymbol || symbol instanceof FunctionSymbol);
    TestCase.assertEquals(fqn, SymbolUtil.getSymbolFullQualifiedName(symbol));
  }

  public static class RubyMotionLightFixtureDescriptor extends RubyLightProjectDescriptorBase {
    @Override
    public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
      prepareSourceRoots(contentEntry);

      final RubyMotionFacetType facetType = RubyMotionFacetType.getInstance();
      RubyFixtureTestCase.addFacetToModule(module, facetType, facetType.createDefaultConfiguration(), "RubyMotion");
      RubyMotionFacet.updateMotionLibrary(model);
    }
  }
}
