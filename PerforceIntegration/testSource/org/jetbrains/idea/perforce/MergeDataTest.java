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
import org.jetbrains.idea.perforce.perforce.MergedFileParser;

public class MergeDataTest extends TestCase{
  public void test() throws Exception {
    final MergedFileParser parser =
    new MergedFileParser("This package contains the modified source code of Netbeans' JavaCVS client library (http://javacvs.netbeans.org),\n" +
                            "copyrighted by SUN. The initial developer is Robert Greig; parts of the code were contributed by Milos Kleint\n" +
      "and Thomas Singer. Significant refactorings were done by Thomas Singer.\n" +
      "\n" +
      "All this source code is published under the SUN PUBLIC LICENSE, that is included. Using these source code\n" +
      "implies that you must ensure to comply to the SUN PUBLIC LICENSE.\n" +
      "\n" +
      ">>>> ORIGINAL readme2.txt#6\n" +
      "change11\n" +
      "change31\n" +
      "==== THEIRS readme2.txt#7\n" +
      "change13\n" +
      "change33\n" +
      "==== YOURS readme2.txt\n" +
      "change12\n" +
      "change32\n" +
      "<<<<\n" +
      "\n" +
      "\n" +
      "This package contains the modified source code of Netbeans' JavaCVS client library (http://javacvs.netbeans.org),\n" +
      "copyrighted by SUN. The initial developer is Robert Greig; parts of the code were contributed by Milos Kleint\n" +
      "and Thomas Singer. Significant refactorings were done by Thomas Singer.\n" +
      "\n" +
      "All this source code is published under the SUN PUBLIC LICENSE, that is included. Using these source code\n" +
      "implies that you must ensure to comply to the SUN PUBLIC LICENSE.\n" +
      "change1 ]\n" +
      "\n" +
      "\n" +
      "\n" +
      ">>>> ORIGINAL readme2.txt#6\n" +
      "change11\n" +
      "change31\n" +
      "==== THEIRS readme2.txt#7\n" +
      "change13\n" +
      "change33\n" +
      "==== YOURS readme2.txt\n" +
      "change12\n" +
      "change32\n" +
      "<<<<\n" +
      "\n" +
      "\n" +
      "");


    final String expectedOriginal = "This package contains the modified source code of Netbeans' JavaCVS client library (http://javacvs.netbeans.org),\n" +
                            "copyrighted by SUN. The initial developer is Robert Greig; parts of the code were contributed by Milos Kleint\n" +
      "and Thomas Singer. Significant refactorings were done by Thomas Singer.\n" +
      "\n" +
      "All this source code is published under the SUN PUBLIC LICENSE, that is included. Using these source code\n" +
      "implies that you must ensure to comply to the SUN PUBLIC LICENSE.\n" +
      "\n" +
      "change11\n" +
      "change31\n" +
      "\n" +
      "\n" +
      "This package contains the modified source code of Netbeans' JavaCVS client library (http://javacvs.netbeans.org),\n" +
      "copyrighted by SUN. The initial developer is Robert Greig; parts of the code were contributed by Milos Kleint\n" +
      "and Thomas Singer. Significant refactorings were done by Thomas Singer.\n" +
      "\n" +
      "All this source code is published under the SUN PUBLIC LICENSE, that is included. Using these source code\n" +
      "implies that you must ensure to comply to the SUN PUBLIC LICENSE.\n" +
      "change1 ]\n" +
      "\n" +
      "\n" +
      "\n" +
      "change11\n" +
      "change31\n" +
      "\n" +
      "\n" +
      "";
    final String expectedLocal = "This package contains the modified source code of Netbeans' JavaCVS client library (http://javacvs.netbeans.org),\n" +
                            "copyrighted by SUN. The initial developer is Robert Greig; parts of the code were contributed by Milos Kleint\n" +
      "and Thomas Singer. Significant refactorings were done by Thomas Singer.\n" +
      "\n" +
      "All this source code is published under the SUN PUBLIC LICENSE, that is included. Using these source code\n" +
      "implies that you must ensure to comply to the SUN PUBLIC LICENSE.\n" +
      "\n" +
      "change12\n" +
      "change32\n" +
      "\n" +
      "\n" +
      "This package contains the modified source code of Netbeans' JavaCVS client library (http://javacvs.netbeans.org),\n" +
      "copyrighted by SUN. The initial developer is Robert Greig; parts of the code were contributed by Milos Kleint\n" +
      "and Thomas Singer. Significant refactorings were done by Thomas Singer.\n" +
      "\n" +
      "All this source code is published under the SUN PUBLIC LICENSE, that is included. Using these source code\n" +
      "implies that you must ensure to comply to the SUN PUBLIC LICENSE.\n" +
      "change1 ]\n" +
      "\n" +
      "\n" +
      "\n" +
      "change12\n" +
      "change32\n" +
      "\n" +
      "\n" +
      "";
    final String expectedLast = "This package contains the modified source code of Netbeans' JavaCVS client library (http://javacvs.netbeans.org),\n" +
                            "copyrighted by SUN. The initial developer is Robert Greig; parts of the code were contributed by Milos Kleint\n" +
      "and Thomas Singer. Significant refactorings were done by Thomas Singer.\n" +
      "\n" +
      "All this source code is published under the SUN PUBLIC LICENSE, that is included. Using these source code\n" +
      "implies that you must ensure to comply to the SUN PUBLIC LICENSE.\n" +
      "\n" +
      "change13\n" +
      "change33\n" +
      "\n" +
      "\n" +
      "This package contains the modified source code of Netbeans' JavaCVS client library (http://javacvs.netbeans.org),\n" +
      "copyrighted by SUN. The initial developer is Robert Greig; parts of the code were contributed by Milos Kleint\n" +
      "and Thomas Singer. Significant refactorings were done by Thomas Singer.\n" +
      "\n" +
      "All this source code is published under the SUN PUBLIC LICENSE, that is included. Using these source code\n" +
      "implies that you must ensure to comply to the SUN PUBLIC LICENSE.\n" +
      "change1 ]\n" +
      "\n" +
      "\n" +
      "\n" +
      "change13\n" +
      "change33\n" +
      "\n" +
      "\n" +
      "";

    assertEquals(expectedOriginal, parser.getOriginal());
    assertEquals(expectedLocal, parser.getLocal());
    assertEquals(expectedLast, parser.getLast());
  }
}
