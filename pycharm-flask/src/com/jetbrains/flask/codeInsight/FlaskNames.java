/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package com.jetbrains.flask.codeInsight;

import com.jetbrains.python.psi.impl.PyQualifiedName;

/**
 * @author yole
 */
public class FlaskNames {
  public static final String FLASK_MODULE = "flask";
  public static final String FLASK_CLASS = "Flask";
  public static final String RENDER_TEMPLATE = "render_template";
  public static final String URL_FOR = "url_for";
  public static final String ROUTE = "route";
  public static final String TEMPLATES = "templates";
  public static final String HELPERS_PY = "helpers.py";
  public static final String GLOBALS_PY = "globals.py";
  public static final String DEFAULT_CONVERTERS = "DEFAULT_CONVERTERS";
  public static final String RULE_CLASS = "werkzeug.routing.Rule";
  public static final String REQUEST_CLASS = "flask.wrappers.Request";
  public static final String REQUEST = "request";
  public static final String SESSION = "session";
  public static final String G = "g";
  public static final String ABORT = "abort";
  public static final String REDIRECT = "redirect";
  public static final String GET_FLASHED_MESSAGES = "get_flashed_messages";
  public static final String FLASKEXT = "flaskext";
  public static final PyQualifiedName FLASK_EXT = PyQualifiedName.fromDottedString("flask.ext");
}
