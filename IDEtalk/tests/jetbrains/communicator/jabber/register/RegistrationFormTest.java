/*
 * Copyright 2000-2006 JetBrains s.r.o.
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
package jetbrains.communicator.jabber.register;

import jetbrains.communicator.BaseTestCase;
import jetbrains.communicator.mock.MockIDEFacade;
import jetbrains.communicator.util.StringUtil;
import jetbrains.communicator.util.TextAcceptor;

import javax.swing.*;
import java.lang.reflect.Field;

/**
 * @author Kir
 */
public class RegistrationFormTest extends BaseTestCase {
  private RegistrationForm myForm;
  private MockJabberFacade myFacade;
  private TextAcceptor myTextAcceptor;
  private String myErrorText;
  private MockIDEFacade myIdeFacade;

  protected void setUp() throws Exception {
    super.setUp();

    myFacade = new MockJabberFacade();
    myTextAcceptor = new TextAcceptor() {
      public void setText(String text) {
        myErrorText = text;
      }
    };
    myIdeFacade = new MockIDEFacade();
    myForm = new RegistrationForm(myFacade, myIdeFacade, myTextAcceptor);
  }

  public void testInit() throws Exception {


    assertEquals("Bad Default username", StringUtil.getMyUsername(), myFacade.getMyAccount().getUsername());
    assertEquals("Bad Default username", StringUtil.getMyUsername(), myForm.getUsername());
    assertEquals("Bad Default server", "intellijoin.org", myFacade.getMyAccount().getServer());
    assertEquals("Bad Default server", "intellijoin.org", myForm.getServer());
    assertEquals("Bad Default port", 5222, myFacade.getMyAccount().getPort());
    assertEquals("Bad Default port", 5222, myForm.getPort());
    assertFalse("Bad Default SSL", myForm.isForceSSL());
    assertFalse("Bad Remember password", myForm.shouldRememberPassword());

    myFacade.getMyAccount().setUsername("fooo");
    myFacade.getMyAccount().setServer("fooo.org");
    myFacade.getMyAccount().setPort(5223);
    myFacade.getMyAccount().setPassword("myPwd");
    myFacade.getMyAccount().setForceSSL(true);
    myFacade.getMyAccount().setRememberPassword(true);

    RegistrationForm.INITIAL_MESSAGE = "some initial message";
    myForm = new RegistrationForm(myFacade, myIdeFacade, myTextAcceptor);

    assertEquals("By default should be initialized with current username",
        "fooo", myForm.getUsername());

    assertEquals("Default server expected", "fooo.org", myForm.getServer());
    assertEquals("Wrong port", 5223, myForm.getPort());
    assertEquals("Default initial message expected", "some initial message", myErrorText);
    assertEquals("No error expected by default", "myPwd", myForm.getPassword());
    assertNull(RegistrationForm.INITIAL_MESSAGE);
    assertTrue("Bad forceSSL status", myForm.isForceSSL());
    assertTrue("Bad Remember password", myForm.shouldRememberPassword());
  }

  public void testSaveMode() throws Throwable {
    myForm.setUseExisingAccount(false);
    myForm = new RegistrationForm(myFacade, myIdeFacade, myTextAcceptor);
    assertFalse("should remember state", myForm.useExistingAccount());

    myForm.setUseExisingAccount(true);
    myForm = new RegistrationForm(myFacade, myIdeFacade, myTextAcceptor);
    assertTrue("should remember state", myForm.useExistingAccount());
  }

  public void testCommit_RememberPassword() throws Exception {
    myForm.setRememberPassword(true);
    myForm.commit();
    assertTrue(myFacade.getMyAccount().shouldRememberPassword());
  }

  public void testCommit_SkipPassword() throws Exception {
    myForm.setRememberPassword(false);
    myForm.commit();
    assertFalse(myFacade.getMyAccount().shouldRememberPassword());
  }

  public void testCommit_ExistingAccount_OK() throws Exception {
    _test(true, true);

    assertEquals("Should call connect and saveSettings", "foo:password@some.server:1234:falsesaveSettings", myFacade.getLog());
    assertNull("No error expected", myErrorText);
  }

  public void testCommit_SSL_ExistingAccount_OK() throws Exception {
    myForm.setForceSSL(true);
    _test(true, true);

    assertEquals("Should call connect and saveSettings", "foo:password@some.server:1234:truesaveSettings", myFacade.getLog());
    assertNull("No error expected", myErrorText);
  }

  public void testCommit() throws Exception {
    myFacade.getMyAccount().setLoginAllowed(false);
    myForm.commit();
    assertTrue(myFacade.getMyAccount().isLoginAllowed());
  }

  public void testCancel() throws Exception {
    myFacade.getMyAccount().setLoginAllowed(true);
    myForm.cancel();
    assertFalse(myFacade.getMyAccount().isLoginAllowed());
  }

  private void _test(boolean useExistingAccount, boolean isSuccessful) {
    myFacade.clearLog();
    myErrorText = null;
    myForm.setUseExisingAccount(useExistingAccount);

    myForm.setUsername("foo");
    myForm.setPassword("password");
    myForm.setPort(1234);
    myForm.setServer("some.server");

    myFacade.setConnected(isSuccessful);
    myForm.commit();
  }

  public void testCommit_ExistingAccount_Error() throws Exception {
    _test(true, false);

    assertEquals("Should call connect only", "foo:password@some.server:1234:false", myFacade.getLog());
    assertEquals("Error expected", "Error: " + MockJabberFacade.ERROR_LINE, myErrorText);
  }

  public void testResetErrorLabel() throws Exception {
    myForm.commit();
    assertNotNull("Sanity check", myErrorText);

    myForm.setUsername("anotherName");
    assertNull("Should be cleared on text field change", myErrorText);

    myForm.commit();
    assertNotNull("Sanity check", myErrorText);
    myForm.setServerIdx(1);
    assertNull("Should be cleared on combobox change", myErrorText);
  }

  public void testCommit_NewAccount_OK() throws Exception {
    myForm.setPasswordAgain("password");
    myForm.setNickame("fff");
    myForm.setFirstName("Kirill");
    myForm.setLastName("Maximov");
    _test(false, true);

    assertEquals("Should call createAccount, setVCard and saveSettings ",
        "createAccount_foo:password@some.server:1234:false_setVCardfffKirillMaximovsaveSettings", myFacade.getLog());
    assertNull("No error expected", myErrorText);
  }

  public void testCommit_SSL_NewAccount_OK() throws Exception {
    myForm.setForceSSL(true);
    myForm.setPasswordAgain("password");
    myForm.setNickame("fff");
    myForm.setFirstName("Kirill");
    myForm.setLastName("Maximov");
    _test(false, true);

    assertEquals("Should call createAccount, setVCard and saveSettings ",
        "createAccount_foo:password@some.server:1234:true_setVCardfffKirillMaximovsaveSettings", myFacade.getLog());
    assertNull("No error expected", myErrorText);
  }

  public void testCommit_NewAccount_Error() throws Exception {
    myForm.setPasswordAgain("password");
    myForm.setNickame("fff");
    myForm.setFirstName("Kirill");
    myForm.setLastName("Maximov");
    _test(false, false);

    assertEquals("Should call createAccount only",
        "createAccount_foo:password@some.server:1234:false", myFacade.getLog());
    assertEquals("Error expected", "Error: " + MockJabberFacade.ERROR_LINE, myErrorText);
  }

  public void testCommit_NewAccount_PasswordProblems() throws Exception {
    myForm.setPasswordAgain("nonMatchPwd");
    myForm.setPassword("password");
    myForm.setUsername("user");
    _testPassword();

    assertEquals("No Jabber calls expected", "", myFacade.getLog());
    assertEquals("Error expected", StringUtil.getMsg("jabber.password.mismatch"), myErrorText);

    myForm.setPasswordAgain("Pwd");
    myForm.setPassword("Pwd");
    _testPassword();

    assertEquals("No Jabber calls expected", "", myFacade.getLog());
    assertEquals("Error expected", StringUtil.getMsg("jabber.password.short"), myErrorText);

    myForm.setPasswordAgain("username");
    myForm.setPassword("username");
    myForm.setUsername("username");
    _testPassword();

    assertEquals("No Jabber calls expected", "", myFacade.getLog());
    assertEquals("Error expected", StringUtil.getMsg("jabber.password.username"), myErrorText);
  }

  private void _testPassword() {
    myFacade.clearLog();
    myErrorText = null;
    myForm.setUseExisingAccount(false);

    myForm.setPort(1234);
    myForm.setServer("some.server");

    myFacade.setConnected(false);
    myForm.commit();
  }

  public void testFieldsVisibility() throws Exception {
    assertVisible("Bad default visibility", true);

    myForm.setUseExisingAccount(false);
    assertVisible("Create new account by default", true);

    myForm.setUseExisingAccount(true);
    assertVisible("Should hide fields", false);
  }

  private void assertVisible(String message, boolean visible) throws NoSuchFieldException, IllegalAccessException {
    String []fields = new String[]{
      "myFirstNameLabel", "myFirstName",
      "myLastNameLabel", "myLastName",
      "myPasswordAgainLabel", "myPasswordAgain",
    };

    for (int i = 0; i < fields.length; i++) {
      String field = fields[i];
      Field fld = myForm.getClass().getDeclaredField(field);
      fld.setAccessible(true);
      assertEquals(message + ". Wrong visibility for " + field, visible,
          ((JComponent) fld.get(myForm)).isVisible());
    }
  }
}
