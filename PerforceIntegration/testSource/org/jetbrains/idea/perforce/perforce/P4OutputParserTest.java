package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.util.Comparing;
import com.intellij.util.text.SyncDateFormat;
import junit.framework.TestCase;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.idea.perforce.ChangeListData;
import org.jetbrains.idea.perforce.merge.BaseRevision;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@NonNls public class P4OutputParserTest extends TestCase {
  private static final SyncDateFormat DATE_FORMAT = new SyncDateFormat(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()));

  public void testLog() throws ParseException {
    String output = "//depot/javacvs/src/javacvs/org/netbeans/lib/cvsclient/NewClass.java\n" +
                    "... #5 change 85 edit on 2004/11/03 14:00:53 by lesya@lesya-test (text)\n\n\ttest ''test2 test3\n\n" +
                    "... #4 change 84 edit on 2004/11/03 14:00:53 by lesya@lesya-test (text)\n\n\t\n\n" +
                    "... #3 change 83 edit on 2004/11/03 14:00:53 by lesya@lesya-test (text)\n\n\ttest test\n\n" +
                    "... #2 change 82 edit on 2004/11/03 14:00:53 by lesya@lesya-test (text)\n\n\ttest test\n\n" +
                    "... #1 change 36 add on 2004/08/18 14:00:53 by lesya@unit-037 (text)\n\n\ttest \n\n";

    List<P4Revision> revisions = OutputMessageParser.processLogOutput(output, true);
    assertEquals(5, revisions.size());

    for (P4Revision p4Revision : revisions) {
      assertEquals("//depot/javacvs/src/javacvs/org/netbeans/lib/cvsclient/NewClass.java",
                   p4Revision.getDepotPath());
    }

    checkRevision(revisions.get(4), 1, 36, "add", 18, Calendar.AUGUST, 2004, "lesya", "unit-037", "text", "test");
    checkRevision(revisions.get(3), 2, 82, "edit", 3, Calendar.NOVEMBER, 2004, "lesya", "lesya-test", "text", "test test");
    checkRevision(revisions.get(2), 3, 83, "edit", 3, Calendar.NOVEMBER, 2004, "lesya", "lesya-test", "text", "test test");
    checkRevision(revisions.get(1), 4, 84, "edit", 3, Calendar.NOVEMBER, 2004, "lesya", "lesya-test", "text", "");
    checkRevision(revisions.get(0), 5, 85, "edit", 3, Calendar.NOVEMBER, 2004, "lesya", "lesya-test", "text", "test ''test2 test3");

  }

  public void testLogForFileWithBranch() throws Exception{
    assertTrue(OutputMessageParser.LOG_PATTERN.matcher("... #3 change 66879 edit on 2005/03/15 17:03:00 by Dallas@omega (text)").matches());
    assertTrue(OutputMessageParser.DEPOT_PATTERN.matcher("//depot/Fabrique/fabrique-activeLibraries/controls/source/jetbrains/fabrique/web/ui/LayoutImpl.java").matches());

    String output = "//depot/Fabrique/fabrique-activeLibraries/controls/source/jetbrains/fabrique/web/ui/LayoutImpl.java\n" +
                    "... #3 change 66879 edit on 2005/03/15 17:03:00 by Dallas@omega (text)\n" +
                    "\n" +
                    "\tMoved Styleable and CocktailComponent interfaces from WebControl to WebControlInternal\n" +
                    "\n" +
                    "... #2 change 66734 edit on 2005/03/14 18:03:57 by Andrey.Podgurskiy@halt (text)\n" +
                    "\n" +
                    "\tFBQP-2147: Default CSS class name properties are even applied to 'internal' controls of complex controls\n" +
                    "\tFBQP-2174: ScrollPaneDemoPanel2 runtime exception\n" +
                    "\n" +
                    "... #1 change 66586 add on 2005/03/12 18:12:15 by AKireyev@illusion (text)\n" +
                    "\n" +
                    "\tMoved layout-related code to controls AL\n" +
                    "\n" +
                    "... ... branch from //depot/Fabrique/fabrique-framework/sourceWeb/jetbrains/fabrique/web/ui/LayoutImpl.java#1,#5\n" +
                    "//depot/Fabrique/fabrique-framework/sourceWeb/jetbrains/fabrique/web/ui/LayoutImpl.java\n" +
                    "... #5 change 66254 edit on 2005/03/10 14:34:33 by Dallas@omega (text)\n" +
                    "\n" +
                    "\tFBQP-1923: Web framework user API review / Review WebControl API\n" +
                    "\n";
    List<P4Revision> revisions = OutputMessageParser.processLogOutput(output, true);
    assertEquals(4, revisions.size());

    assertFalse(revisions.get(0).isBranched());
    assertEquals("Moved Styleable and CocktailComponent interfaces from WebControl to WebControlInternal", revisions.get(0).getSubmitMessage());

    assertFalse(revisions.get(1).isBranched());
    assertEquals("FBQP-2147: Default CSS class name properties are even applied to 'internal' controls of complex controls\n" +
                 "FBQP-2174: ScrollPaneDemoPanel2 runtime exception", revisions.get(1).getSubmitMessage());

    assertFalse(revisions.get(2).isBranched());
    assertEquals("Moved layout-related code to controls AL", revisions.get(2).getSubmitMessage());

    assertTrue(revisions.get(3).isBranched());
    assertEquals("[... ... branch from //depot/Fabrique/fabrique-framework/sourceWeb/jetbrains/fabrique/web/ui/LayoutImpl.java#1,#5]\n" +
                 "FBQP-1923: Web framework user API review / Review WebControl API"
      , revisions.get(3).getSubmitMessage());
    assertEquals("//depot/Fabrique/fabrique-framework/sourceWeb/jetbrains/fabrique/web/ui/LayoutImpl.java", revisions.get(3).getDepotPath());

    output = "//mindreef/main/webapps/sos/web/error.mrv\n" +
             "... #7 change 10910 edit on 2005/04/29 by aaron@Wyrmwood (text)\n" +
             "\n" +
             "\tbugfix3396\n" +
             "\n" +
             "... ... branch into //mindreef/bugfix/Sputnik1.0/webapps/sos/web/error.mrv#1\n" +
             "... ... branch into //mindreef/sandbox/contractbuilder/webapps/sos/web/error.mrv#1\n" +
             "... #6 change 10896 edit on 2005/04/29 by jim@Moray (text)\n" +
             "\n" +
             "\tLittle more padding on top of error page\n" +
             "\n" +
             "... #5 change 10839 edit on 2005/04/28 by jim@Moray (text)\n" +
             "\n" +
             "\tCleaned up exception pages.\n" +
             "\n" +
             "... #4 change 9786 edit on 2005/02/16 by aaron@Wyrmwood (text)\n" +
             "\n" +
             "\tremoved $link, solves the null pointer in tomcat window error\n" +
             "\n" +
             "... #3 change 9657 edit on 2005/02/07 by jim@Moray (text)\n" +
             "\n" +
             "\tServer setup pages\n" +
             "\n" +
             "... #2 change 9325 edit on 2005/01/01 by jim@Moray (text)\n" +
             "\n" +
             "\tInitial global-exception handlers for struts\n" +
             "\n" +
             "... #1 change 9324 add on 2005/01/01 by jim@Moray (text)\n" +
             "\n" +
             "\tTidy up the first WebTest.";

    revisions = OutputMessageParser.processLogOutput(output, false);
    assertEquals(7, revisions.size());

  }

  public void testChanges() throws Exception{
    String output="Change 66853 on 2005/03/15 15:03:42 by lesya@lesya_new\n" +
                  "\n" +
                  "\tNon-closing tags formatting fixed\n" +
                  "\n" +
                  "Change 66843 on 2005/03/15 14:43:26 by lesya@lesya_new\n" +
                  "\n" +
                  "\tReformat code action moved into Code main menu group\n" +
                  "\n" +
                  "Change 66826 on 2005/03/15 14:01:15 by lesya@lesya_new\n" +
                  "\n" +
                  "\tStructuralReplaceTest rebombed\n";

    List<ChangeListData> changes = OutputMessageParser.processChangesOutput(output);
    assertEquals(3, changes.size());

    checkChange(changes.get(0), 66853, "2005/03/15 15:03:42", "lesya", "lesya_new", "Non-closing tags formatting fixed");
    checkChange(changes.get(1), 66843, "2005/03/15 14:43:26", "lesya", "lesya_new", "Reformat code action moved into Code main menu group");
    checkChange(changes.get(2), 66826, "2005/03/15 14:01:15", "lesya", "lesya_new", "StructuralReplaceTest rebombed");
  }

  public void testResolve() throws Exception{
    final Map<String, BaseRevision> output = PerforceRunner
      .processResolveOutput("C:\\work\\Irida\\source\\com\\intellij\\ide\\favoritesTreeView\\FavoritesTreeStructure.java - merging //IDEA/source/com/intellij/ide/favoritesTreeView/FavoritesTreeStructure.java #14 using base //IDEA/source/com/intellij/ide/favoritesTreeView/FavoritesTreeStructure.java#13\n" +
                            "C:\\work\\Irida\\source\\com\\intellij\\ide\\ui\\customization\\CustomizableActionsPanel.java - merging //IDEA/source/com/intellij/ide/ui/customization/CustomizableActionsPanel.java#9 using base //IDEA/source/com/intellij/ide/ui/customization/CustomizableActionsPanel.java#8");

    assertEquals("#13", output.get("C:\\work\\Irida\\source\\com\\intellij\\ide\\favoritesTreeView\\FavoritesTreeStructure.java").getRevisionNum());
    assertEquals("//IDEA/source/com/intellij/ide/favoritesTreeView/FavoritesTreeStructure.java", output.get("C:\\work\\Irida\\source\\com\\intellij\\ide\\favoritesTreeView\\FavoritesTreeStructure.java").getDepotPath());

    assertEquals("#8", output.get("C:\\work\\Irida\\source\\com\\intellij\\ide\\ui\\customization\\CustomizableActionsPanel.java").getRevisionNum());
    assertEquals("//IDEA/source/com/intellij/ide/ui/customization/CustomizableActionsPanel.java", output.get("C:\\work\\Irida\\source\\com\\intellij\\ide\\ui\\customization\\CustomizableActionsPanel.java").getDepotPath());
  }

  public void testBranches() {
    String branchesOutput = "Branch demetra 2005/09/28 'IDEA 6 '\n" +
                            "Branch Eshop_dev 2005/03/10 'Created by rob. '\n" +
                            "Branch ExAnalyser 2005/09/07 'Created by migger. '\n" +
                            "Branch irida-5.0.2 2005/09/28 'Created by yole. '\n" +
                            "Branch irida-i18n 2005/08/15 'Created by yole. '\n" +
                            "Branch irida_to_i18n 2005/09/29 'Created by yole. '\n" +
                            "Branch Omea2.0 2005/08/09 'Created by yole. '\n" +
                            "Branch OmeaPro1.0 2004/11/27 'Branch for the Omea Pro 1.0 release '\n" +
                            "Branch OmeaReader1.0 2004/09/28 'branch for Omea Reader 1.0 release '\n" +
                            "Branch ReSharper-Release1.0 2004/07/17 'Created by dsha. '";

    List<String> branches = OutputMessageParser.processBranchesOutput(branchesOutput);

    assertEquals(10, branches.size());

    assertTrue(Comparing.equal(branches, Arrays.asList("demetra", "Eshop_dev", "ExAnalyser",
                                                       "irida-5.0.2", "irida-i18n", "irida_to_i18n",
                                                       "Omea2.0", "OmeaPro1.0", "OmeaReader1.0", "ReSharper-Release1.0")));
  }

  public void testReadChangeDescription() {
    String changeDescription = "Change 104643 by ven@5.0 on 2005/10/06 16:14:20\n" +
                               "\n" +
                               "\tIDEADEV2626\n" +
                               "\n" +
                               "Affected files ...\n" +
                               "\n" +
                               "... //IDEA/source/com/intellij/psi/impl/source/resolve/PsiResolveHelperImpl.java#23 edit\n" +
                               "... //IDEA/testData/codeInsight/completion/smartType/IDEADEV2626-out.java#1 add\n" +
                               "... //IDEA/testData/codeInsight/completion/smartType/IDEADEV2626.java#1 add\n" +
                               "... //IDEA/testSource/com/intellij/codeInsight/completion/SmartTypeCompletion15Test.java#5 edit";
    final ChangeListData changeList = new OutputMessageParser(changeDescription).loadChangeListDescription();
    assertNotNull(changeList);
    assertEquals(104643, changeList.NUMBER);
    assertEquals("5.0", changeList.CLIENT);
    assertEquals("ven", changeList.USER);
  }

  public void testOpened() throws Exception {
    String opened = "//IDEA_Branches/irida/idea.ipr#3 - edit default change (text)\n" +
                    "//website/eshop_dev/static/content/idea/uninstall/index.jsp#5 - edit change 127985 (text)";
    final List<PerforceChange> list = PerforceOutputMessageParser.processOpenedOutput(opened);
    assertEquals(2, list.size());
    PerforceChange change1 = list.get(0);
    assertEquals("//IDEA_Branches/irida/idea.ipr", change1.getDepotPath());
    assertEquals(3, change1.getRevision());
    assertEquals(PerforceAbstractChange.EDIT, change1.getType());

    assertEquals(127985, list.get(1).getChangeList());
  }

  public void testResolved() throws Exception {
    String resolved = "C:\\yole\\ExceptionAnalyser\\PerforceTest\\src\\a\\b\\c\\d\\C.java - branch from //ExceptionAnalyser/PerforceTest/src/a/b/c/d/B.java#1\n" +
                      "C:\\yole\\IDEA\\idea.ipr - ignored //IDEA/idea.ipr#246,#247";
    final List<ResolvedFile> list = PerforceOutputMessageParser.processResolvedOutput(resolved, o -> o);
    assertEquals(2, list.size());
    ResolvedFile file1 = list.get(0);
    assertEquals("C:\\yole\\ExceptionAnalyser\\PerforceTest\\src\\a\\b\\c\\d\\C.java", file1.getLocalFile().toString());
    assertEquals(ResolvedFile.OPERATION_BRANCH, file1.getOperation());
    assertEquals("//ExceptionAnalyser/PerforceTest/src/a/b/c/d/B.java", file1.getDepotPath());
    assertEquals(1, file1.getRevision1());
    assertEquals(-1, file1.getRevision2());

    ResolvedFile file2 = list.get(1);
    assertEquals(ResolvedFile.OPERATION_IGNORE, file2.getOperation());
    assertEquals(246, file2.getRevision1());
    assertEquals(247, file2.getRevision2());
  }

  private void checkChange(final ChangeListData change,
                           final int changeNum,
                           final String date,
                           final String user,
                           final String client,
                           final String description) throws ParseException {
    assertEquals(changeNum, change.NUMBER);
    assertEquals(date, DATE_FORMAT.format(ChangeListData.DATE_FORMAT.parse(change.DATE)));
    assertEquals(user, change.USER);
    assertEquals(client, change.CLIENT);
    assertEquals(description, change.DESCRIPTION);
  }


  private void checkRevision(P4Revision rev1, int revNumber,
                             int chNumber,
                             String action,
                             int day,
                             int month,
                             int year,
                             String user,
                             String client, String type, String message) {
    assertEquals(revNumber, rev1.getRevisionNumber());

    assertEquals(chNumber, rev1.getChangeNumber());

    assertEquals(action, rev1.getAction());
    Date date = rev1.getDate();
    Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
    calendar.setTime(date);

    assertEquals(day, calendar.get(Calendar.DAY_OF_MONTH));
    assertEquals(month, calendar.get(Calendar.MONTH));
    assertEquals(year, calendar.get(Calendar.YEAR));
    assertEquals(user, rev1.getUser());
    assertEquals(client, rev1.getClient());
    assertEquals(type, rev1.getType());
    assertEquals(message, rev1.getSubmitMessage());
  }
}
