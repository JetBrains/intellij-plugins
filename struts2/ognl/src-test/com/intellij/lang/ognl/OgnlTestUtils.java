/*
 * Copyright 2011 The authors
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

package com.intellij.lang.ognl;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileTypeManager;

/**
 * @author Yann C&eacute;bron
 */
public final class OgnlTestUtils {

  public static final String DUMMY_OGNL_FILE_NAME = "test." + OgnlFileType.INSTANCE.getDefaultExtension();

  private OgnlTestUtils() {
  }

  /**
   * Wraps text in OGNL expression holder for parsing.
   *
   * @param text Text.
   * @return OGNL expression.
   */
  public static String createExpression(final String text) {
    return OgnlLanguage.EXPRESSION_PREFIX + text + OgnlLanguage.EXPRESSION_SUFFIX;
  }

  /**
   * Installs OGNL file-type for using {@code .ognl} files.
   */
  public static void installOgnlFileType() {
    associateOgnlFileType(false);
  }

  /**
   * Removes OGNL file-type.
   */
  public static void removeOgnlFileType() {
    associateOgnlFileType(true);
  }

  private static void associateOgnlFileType(final boolean remove) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        if (remove) {
          FileTypeManager.getInstance().removeAssociatedExtension(OgnlFileType.INSTANCE,
                                                                  OgnlFileType.INSTANCE.getDefaultExtension());
          return;
        }
        FileTypeManager.getInstance().associateExtension(OgnlFileType.INSTANCE,
                                                         OgnlFileType.INSTANCE.getDefaultExtension());
      }
    });
  }
}
