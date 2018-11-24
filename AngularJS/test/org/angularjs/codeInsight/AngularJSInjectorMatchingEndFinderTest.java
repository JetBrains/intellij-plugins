package org.angularjs.codeInsight;

import com.intellij.openapi.util.text.InjectorMatchingEndFinder;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * @author Irina.Chernushina on 12/2/2015.
 */
public class AngularJSInjectorMatchingEndFinderTest extends TestCase {
  public void test() throws Exception {
    defaultTest("{{recipients.length, plural, =0 {something}}}", "recipients.length, plural, =0 {something}");
    defaultTest("{{recipients.length, plural, offset:1, =0 {You ({{sender.name}}) gave no gifts} =1 { {{recipients[0].gender, select, male {You ({{sender.name}}) gave him ({{recipients[0].name}}) a gift.} female {You ({{sender.name}}) gave her ({{recipients[0].name}}) a gift.} other {You ({{sender.name}}) gave them ({{recipients[0].name}}) a gift.!}}}} other {OUTER MESSAGE}}}",
                "recipients.length, plural, offset:1, =0 {You ({{sender.name}}) gave no gifts} =1 { {{recipients[0].gender, select, male {You ({{sender.name}}) gave him ({{recipients[0].name}}) a gift.} female {You ({{sender.name}}) gave her ({{recipients[0].name}}) a gift.} other {You ({{sender.name}}) gave them ({{recipients[0].name}}) a gift.!}}}} other {OUTER MESSAGE}");
    defaultTest("{{recipients.length, plural, offset:1, =0 {You ({{sender.name}}) gave no gifts} =1 { {{recipients[0].gender, select, male {You ({{sender.name}}) gave him ({{recipients[0].name}}) a gift.} female {You ({{sender.name}}) gave her ({{recipients[0].name}}) a gift.} other {You ({{sender.name}}) gave them ({{recipients[0].name}}) a gift.!}}} } other {OUTER MESSAGE}}}",
                "recipients.length, plural, offset:1, =0 {You ({{sender.name}}) gave no gifts} =1 { {{recipients[0].gender, select, male {You ({{sender.name}}) gave him ({{recipients[0].name}}) a gift.} female {You ({{sender.name}}) gave her ({{recipients[0].name}}) a gift.} other {You ({{sender.name}}) gave them ({{recipients[0].name}}) a gift.!}}} } other {OUTER MESSAGE}");
    defaultTest("{{recipients.length, plural, offset:1, =0 {You ({{sender.name}}) gave no gifts} =1 { {{recipients[0].gender, select, male {You ({{sender.name}}) gave him ({{recipients[0].name}}) a gift.} female {You ({{sender.name}}) gave her ({{recipients[0].name}}) a gift.} other {You ({{sender.name}}) gave them ({{recipients[0].name}}) a gift.!} }}} other {OUTER MESSAGE}}}",
                "recipients.length, plural, offset:1, =0 {You ({{sender.name}}) gave no gifts} =1 { {{recipients[0].gender, select, male {You ({{sender.name}}) gave him ({{recipients[0].name}}) a gift.} female {You ({{sender.name}}) gave her ({{recipients[0].name}}) a gift.} other {You ({{sender.name}}) gave them ({{recipients[0].name}}) a gift.!} }}} other {OUTER MESSAGE}");
    defaultTest("{{recipients.length, plural, offset:1, =0 {You ({{sender.name}}) gave no gifts} =1 { {{recipients[0].gender, select, male {You ({{sender.name}}) gave him ({{recipients[0].name}}) a gift.} female {You ({{sender.name}}) gave her ({{recipients[0].name}}) a gift.} other {You ({{sender.name}}) gave them ({{recipients[0].name}}) a gift.!} }} } other {OUTER MESSAGE}}}",
                "recipients.length, plural, offset:1, =0 {You ({{sender.name}}) gave no gifts} =1 { {{recipients[0].gender, select, male {You ({{sender.name}}) gave him ({{recipients[0].name}}) a gift.} female {You ({{sender.name}}) gave her ({{recipients[0].name}}) a gift.} other {You ({{sender.name}}) gave them ({{recipients[0].name}}) a gift.!} }} } other {OUTER MESSAGE}");
  }

  private static void defaultTest(String text, String check) {
    final int startIdx = text.indexOf("{{", 0);
    int afterStart = startIdx < 0 ? -1 : (startIdx + "{{".length());
    final int end = InjectorMatchingEndFinder.findMatchingEnd("{{", "}}", text, afterStart);
    Assert.assertTrue(afterStart > 0);
    Assert.assertTrue(end > 0);
    Assert.assertEquals(check, text.substring(afterStart, end));
    System.out.println("*");
  }
}
