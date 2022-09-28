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
    final Map<File, String> map = FStat.splitOutputForEachFile("""
                                                                 ... depotFile //depot/created2.txt
                                                                 ... clientFile C:\\PerforceTest1\\created2.txt
                                                                 ... headAction add
                                                                 ... headType text
                                                                 ... headTime 1094486436
                                                                 ... headRev 1
                                                                 ... headChange 65
                                                                 ... haveRev 1

                                                                 ... depotFile //depot/modified.txt
                                                                 ... clientFile C:\\PerforceTest1\\modified.txt
                                                                 ... headAction edit
                                                                 ... headType text
                                                                 ... headTime 1094486436
                                                                 ... headRev 3
                                                                 ... headChange 65
                                                                 ... haveRev 3

                                                                 ... depotFile //depot/restored.txt
                                                                 ... clientFile C:\\PerforceTest1\\restored.txt
                                                                 ... headAction add
                                                                 ... headType text
                                                                 ... headTime 1094474794
                                                                 ... headRev 1
                                                                 ... headChange 60
                                                                 ... haveRev 1
                                                                 """);
    assertEquals(3, map.size());
    assertEquals(map.get(new File("C:\\PerforceTest1\\created2.txt")), """
      ... depotFile //depot/created2.txt
      ... clientFile C:\\PerforceTest1\\created2.txt
      ... headAction add
      ... headType text
      ... headTime 1094486436
      ... headRev 1
      ... headChange 65
      ... haveRev 1
      """);

    assertEquals(map.get(new File("C:\\PerforceTest1\\modified.txt")), """
      ... depotFile //depot/modified.txt
      ... clientFile C:\\PerforceTest1\\modified.txt
      ... headAction edit
      ... headType text
      ... headTime 1094486436
      ... headRev 3
      ... headChange 65
      ... haveRev 3
      """);

    assertEquals(map.get(new File("C:\\PerforceTest1\\restored.txt")), """
      ... depotFile //depot/restored.txt
      ... clientFile C:\\PerforceTest1\\restored.txt
      ... headAction add
      ... headType text
      ... headTime 1094474794
      ... headRev 1
      ... headChange 60
      ... haveRev 1
      """);
  }
}
