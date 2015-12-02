package org.angularjs.codeInsight;

import junit.framework.TestCase;
import org.angularjs.editor.AngularJSInjectorMatchingEndFinder;
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
    final AngularJSInjectorMatchingEndFinder finder = new AngularJSInjectorMatchingEndFinder("{{", "}}", text);
    final int end = finder.find();
    Assert.assertTrue(finder.getAfterStartIdx() > 0);
    Assert.assertTrue(end > 0);
    Assert.assertEquals(check, text.substring(finder.getAfterStartIdx(), end));
  }
}
