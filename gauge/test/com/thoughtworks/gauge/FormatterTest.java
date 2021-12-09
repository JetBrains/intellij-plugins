/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge;

import com.thoughtworks.gauge.markdownPreview.Formatter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FormatterTest {
  @Test
  public void testFormat() {
    String text = "Steps Collection\n" +
                  "================\n" +
                  "\n" +
                  "tags: api\n" +
                  "\n" +
                  "* In an empty directory initialize a project with the <current> language\n" +
                  "* Create a specification \"Specification 1\" with the following contexts\n" +
                  "    |step text|implementation         |\n" +
                  "    |---------|-----------------------|\n" +
                  "    |context 1|\"inside first context\" |\n" +
                  "    |context 2|\"inside second context\"|\n";
    String actual = Formatter.format(text);

    String expected = "Steps Collection\n" +
                      "================\n" +
                      "\n" +
                      "tags: api\n" +
                      "\n" +
                      "* In an empty directory initialize a project with the &lt;current&gt; language\n" +
                      "* Create a specification \"Specification 1\" with the following contexts\n\n" +
                      "\t|step text|implementation         |\n" +
                      "\t|---------|-----------------------|\n" +
                      "\t|context 1|\"inside first context\" |\n" +
                      "\t|context 2|\"inside second context\"|\n";

    assertEquals(expected, actual);
  }

  @Test
  public void testFormatWithMultipleTables() {
    String text = "Steps Collection\n" +
                  "================\n" +
                  "\n" +
                  "tags: api\n" +
                  "\n" +
                  "* In an empty directory initialize a project with the <current> language\n" +
                  "* Create a specification \"Specification 1\" with the following contexts\n" +
                  "    |step text|implementation         |\n" +
                  "    |---------|-----------------------|\n" +
                  "    |context 1|\"inside first context\" |\n" +
                  "    |context 2|\"inside second context\"|\n" +
                  "* Create a specification \"Specification 1\" with the following contexts\n" +
                  "        |step text|implementation         |\n" +
                  "    |---------|-----------------------|\n" +
                  "       |context 1|\"inside first context\" |\n" +
                  "* Create a specification \"Specification 1\" with the following contexts\n\n\n" +
                  "    |step text|implementation         |\n" +
                  "    |---------|-----------------------|\n" +
                  "    |context 1|\"inside first context\" |\n";
    String actual = Formatter.format(text);

    String expected = "Steps Collection\n" +
                      "================\n" +
                      "\n" +
                      "tags: api\n" +
                      "\n" +
                      "* In an empty directory initialize a project with the &lt;current&gt; language\n" +
                      "* Create a specification \"Specification 1\" with the following contexts\n\n" +
                      "\t|step text|implementation         |\n" +
                      "\t|---------|-----------------------|\n" +
                      "\t|context 1|\"inside first context\" |\n" +
                      "\t|context 2|\"inside second context\"|\n" +
                      "* Create a specification \"Specification 1\" with the following contexts\n\n" +
                      "\t|step text|implementation         |\n" +
                      "\t|---------|-----------------------|\n" +
                      "\t|context 1|\"inside first context\" |\n" +
                      "* Create a specification \"Specification 1\" with the following contexts\n\n" +
                      "\t|step text|implementation         |\n" +
                      "\t|---------|-----------------------|\n" +
                      "\t|context 1|\"inside first context\" |\n";

    assertEquals(expected, actual);
  }
}