package com.intellij.flex.uiDesigner;

import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.psi.PsiElement;
import gnu.trove.THashMap;

import java.util.Map;

public class ResolveExternalInlineStyleSourceActionTest extends FlexUIDesignerBaseTestCase {
  private static final String BASE_PATH = "/resolveExternalInlineStyle";

  protected Runnable myAfterCommitRunnable = null;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myAfterCommitRunnable = null;

    configureByFile(BASE_PATH + "/innerComponentInDeclarations.mxml");
  }

  @JSTestOptions({JSTestOption.WithGumboSdk, JSTestOption.WithFlexFacet})
  public void testFindInnerComponentInDeclarations() throws Exception {
    Map<String, String> styles = new THashMap<String, String>();
    styles.put("skinClass", "spark.skins.spark.ButtonBarMiddleButtonSkin");
    ResolveExternalInlineStyleSourceAction action = new ResolveExternalInlineStyleSourceAction("innerComponentInDeclarations", "spark.components.ButtonBarButton", "skinClass", styles, myModule);
    PsiElement element = (PsiElement) action.find();
    assertNotNull(element);
    assertEquals(1002, element.getTextOffset());
  }

  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithJsSupportLoader, JSTestOption.WithGumboSdk, JSTestOption.WithFlexFacet})
  public void testFindInnerComponentInDeclarationsAsTag() throws Exception {
    Map<String, String> styles = new THashMap<String, String>();
    styles.put("skinClass", "spark.skins.spark.ButtonBarLastButtonSkin");
    ResolveExternalInlineStyleSourceAction action = new ResolveExternalInlineStyleSourceAction("innerComponentInDeclarations", "spark.components.ButtonBarButton", "skinClass", styles, myModule);
    PsiElement element = (PsiElement) action.find();
    assertNotNull(element);
    assertEquals(1323, element.getTextOffset());
  }
  
  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithJsSupportLoader, JSTestOption.WithGumboSdk, JSTestOption.WithFlexFacet})
  public void testFindComponent() throws Exception {
    Map<String, String> styles = new THashMap<String, String>();
    styles.put("left", "10");
    styles.put("right", "10");
    ResolveExternalInlineStyleSourceAction action = new ResolveExternalInlineStyleSourceAction("innerComponentInDeclarations", "spark.components.Button", "left", styles, myModule);
    PsiElement element = (PsiElement) action.find();
    assertNotNull(element);
    assertEquals(1717, element.getTextOffset());
  }
  
  @JSTestOptions({JSTestOption.WithCssSupportLoader, JSTestOption.WithJsSupportLoader, JSTestOption.WithGumboSdk, JSTestOption.WithFlexFacet})
  public void testFindComponent2() throws Exception {
    Map<String, String> styles = new THashMap<String, String>();
    styles.put("left", "10");
    styles.put("right", "10");
    styles.put("top", "4");
    ResolveExternalInlineStyleSourceAction action = new ResolveExternalInlineStyleSourceAction("innerComponentInDeclarations", "spark.components.Button", "left", styles, myModule);
    PsiElement element = (PsiElement) action.find();
    assertNotNull(element);
    assertEquals(1808, element.getTextOffset());
  }
}
