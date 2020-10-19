package org.jetbrains.idea.perforce;

import junit.framework.TestCase;
import org.jetbrains.idea.perforce.perforce.FormParser;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FormParserTest extends TestCase{
  public void test(){
    String change = "# A Perforce Change Specification.\n" +
                    "#\n" +
                    "#  Change:      The change number. 'new' on a new changelist.  Read-only.\n" +
                    "#  Date:        The date this specification was last modified.  Read-only.\n" +
                    "#  Client:      The client on which the changelist was created.  Read-only.\n" +
                    "#  User:        The user who created the changelist. Read-only.\n" +
                    "#  Status:      Either 'pending' or 'submitted'. Read-only.\n" +
                    "#  Description: Comments about the changelist.  Required.\n" +
                    "#  Jobs:        What opened jobs are to be closed by this changelist.\n" +
                    "#               You may delete jobs from this list.  (New changelists only.)\n" +
                    "#  Files:       What opened files from the default changelist are to be added\n" +
                    "#               to this changelist.  You may delete files from this list.\n" +
                    "#               (New changelists only.)\n" +
                    "\n" +
                    "Change:\tnew\n" +
                    "\n" +
                    "Client:\tlesya-test\n" +
                    "\n" +
                    "User:\tlesya\n" +
                    "\n" +
                    "Status:\tnew\n" +
                    "\n" +
                    "Description:\n" +
                    "\t<enter description here>\n" +
                    "\n" +
                    "Files:\n" +
                    "\t//depot/javacvs/src/javacvs/org/netbeans/lib/cvsclient/Entry.java\t# delete\n" +
                    "\t//depot/javacvs/src/javacvs/org/netbeans/lib/cvsclient/NewClass.java\t# edit\n" +
                    "\t//depot/javacvs/src/javacvs/org/netbeans/lib/cvsclient/NewFile3.txt\t# add\n" +
                    "";

    Map<String, List<String>> changeForm = FormParser.execute(change, PerforceRunner.CHANGE_FORM_FIELDS);

    checkField(changeForm, PerforceRunner.CHANGE, "new");
    checkField(changeForm, PerforceRunner.CLIENT, "lesya-test");
    checkField(changeForm, PerforceRunner.USER, "lesya");
    checkField(changeForm, PerforceRunner.STATUS, "new");
    checkField(changeForm, PerforceRunner.DESCRIPTION, "<enter description here>");

    checkField(changeForm, PerforceRunner.FILES, Arrays.asList("//depot/javacvs/src/javacvs/org/netbeans/lib/cvsclient/Entry.java\t# delete",
                                                               "//depot/javacvs/src/javacvs/org/netbeans/lib/cvsclient/NewClass.java\t# edit",
                                                               "//depot/javacvs/src/javacvs/org/netbeans/lib/cvsclient/NewFile3.txt\t# add"));


    String message1 = "message1";

    PerforceRunner.setDescriptionToForm(changeForm, message1);
    checkField(changeForm, PerforceRunner.DESCRIPTION, message1);

    PerforceRunner.setDescriptionToForm(changeForm, "line1\nline2\r\n\tline3\n\n\n\n line4\n\n\n");
    checkField(changeForm, PerforceRunner.DESCRIPTION, Arrays.asList("line1", "line2", "line3", "", "", "", "line4"));

  }

  private void checkField(Map<String, List<String>> changeForm, String fieldName, List<String> expected) {
    List<String> list = changeForm.get(fieldName);
    assertEquals(expected, list);
  }

  private void checkField(Map<String, List<String>> changeForm, String field, String expected) {
    List<String> list = changeForm.get(field);
    assertEquals(1, list.size());
    assertEquals(expected, list.get(0));
  }
}
