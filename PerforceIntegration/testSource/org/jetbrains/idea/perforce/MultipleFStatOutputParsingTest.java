/*
 * Copyright (c) 2004 JetBrains s.r.o. All  Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * -Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduct the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of JetBrains or IntelliJ IDEA
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. JETBRAINS AND ITS LICENSORS SHALL NOT
 * BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT
 * OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL JETBRAINS OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN
 * IF JETBRAINS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 */
package org.jetbrains.idea.perforce;

import junit.framework.TestCase;
import org.jetbrains.idea.perforce.perforce.FStat;

import java.io.File;
import java.util.Map;

public class MultipleFStatOutputParsingTest extends TestCase{
  public void test() throws Exception {
    final Map<File, String> map = FStat.splitOutputForEachFile("... depotFile //depot/created2.txt\n" +
                                                                      "... clientFile C:\\PerforceTest1\\created2.txt\n" +
                                                                      "... headAction add\n" +
                                                                      "... headType text\n" +
                                                                      "... headTime 1094486436\n" +
                                                                      "... headRev 1\n" +
                                                                      "... headChange 65\n" +
                                                                      "... haveRev 1\n" +
                                                                      "\n" +
                                                                      "... depotFile //depot/modified.txt\n" +
                                                                      "... clientFile C:\\PerforceTest1\\modified.txt\n" +
                                                                      "... headAction edit\n" +
                                                                      "... headType text\n" +
                                                                      "... headTime 1094486436\n" +
                                                                      "... headRev 3\n" +
                                                                      "... headChange 65\n" +
                                                                      "... haveRev 3\n" +
                                                                      "\n" +
                                                                      "... depotFile //depot/restored.txt\n" +
                                                                      "... clientFile C:\\PerforceTest1\\restored.txt\n" +
                                                                      "... headAction add\n" +
                                                                      "... headType text\n" +
                                                                      "... headTime 1094474794\n" +
                                                                      "... headRev 1\n" +
                                                                      "... headChange 60\n" +
                                                                      "... haveRev 1\n" +
                                                                      "");
    assertEquals(3, map.size());
    assertEquals(map.get(new File("C:\\PerforceTest1\\created2.txt")), "... depotFile //depot/created2.txt\n" +
                                                                   "... clientFile C:\\PerforceTest1\\created2.txt\n" +
                                                                   "... headAction add\n" +
                                                                   "... headType text\n" +
                                                                   "... headTime 1094486436\n" +
                                                                   "... headRev 1\n" +
                                                                   "... headChange 65\n" +
                                                                   "... haveRev 1\n");

    assertEquals(map.get(new File("C:\\PerforceTest1\\modified.txt")), "... depotFile //depot/modified.txt\n" +
                                                                   "... clientFile C:\\PerforceTest1\\modified.txt\n" +
                                                                   "... headAction edit\n" +
                                                                   "... headType text\n" +
                                                                   "... headTime 1094486436\n" +
                                                                   "... headRev 3\n" +
                                                                   "... headChange 65\n" +
                                                                   "... haveRev 3\n");

    assertEquals(map.get(new File("C:\\PerforceTest1\\restored.txt")), "... depotFile //depot/restored.txt\n" +
                                                                   "... clientFile C:\\PerforceTest1\\restored.txt\n" +
                                                                   "... headAction add\n" +
                                                                   "... headType text\n" +
                                                                   "... headTime 1094474794\n" +
                                                                   "... headRev 1\n" +
                                                                   "... headChange 60\n" +
                                                                   "... haveRev 1\n");
  }
}
