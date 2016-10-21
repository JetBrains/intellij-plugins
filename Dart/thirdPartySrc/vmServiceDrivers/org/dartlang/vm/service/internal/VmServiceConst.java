/*
 * Copyright (c) 2015, the Dart project authors.
 *
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
 package org.dartlang.vm.service.internal;

/**
 * JSON constants used when communicating with the VM observatory service.
 */
public interface VmServiceConst {
  public static final String CODE = "code";
  public static final String ERROR = "error";
  public static final String EVENT = "event";
  public static final String ID = "id";
  public static final String MESSAGE = "message";
  public static final String METHOD = "method";
  public static final String PARAMS = "params";
  public static final String RESULT = "result";
  public static final String STREAM_ID = "streamId";
  public static final String TYPE = "type";
}
