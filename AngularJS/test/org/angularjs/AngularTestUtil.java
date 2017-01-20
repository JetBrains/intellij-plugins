package org.angularjs;

import com.intellij.openapi.application.PathManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import static org.junit.Assert.assertTrue;

/**
 * @author Dennis.Ushakov
 */
public class AngularTestUtil {

  public static String getBaseTestDataPath(Class clazz) {
    String homePath = PathManager.getHomePath();
    return homePath + "/contrib/AngularJS/test/" + clazz.getPackage().getName().replace('.', '/') + "/data/";
  }

  public static int findOffsetBySignature(String signature, final PsiFile psiFile) {
    final String caretSignature = "<caret>";
    int caretOffset = signature.indexOf(caretSignature);
    assert caretOffset >= 0;
    signature = signature.substring(0, caretOffset) + signature.substring(caretOffset + caretSignature.length());
    int pos = psiFile.getText().indexOf(signature);
    assertTrue(pos >= 0);
    return pos + caretOffset;
  }

  public static String getDirectiveDefinitionText(PsiElement resolve) {
    return resolve.getParent().getText();
  }
}
