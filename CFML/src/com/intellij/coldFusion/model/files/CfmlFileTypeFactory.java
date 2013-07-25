/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package com.intellij.coldFusion.model.files;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: vnikolaenko
 * Date: 26.12.2008
 * Time: 13:48:45
 * To change this template use File | Settings | File Templates.
 */
public class CfmlFileTypeFactory extends FileTypeFactory {
  public void createFileTypes(final @NotNull FileTypeConsumer consumer) {
    consumer.consume(CfmlFileType.INSTANCE, StringUtil.join(CfmlFileType.INSTANCE.getExtensions(), ";"));
  }
}
