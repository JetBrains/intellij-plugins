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

public class ActionClass {

  private String myField;

  public String validActionMethod() {
    return null;
  }

  public String validActionMethodWithException() throws Exception {
    return null;
  }

  public String getValidActionMethodNoUnderlyingField() {
    return null;
  }

  public com.opensymphony.xwork2.Result validActionMethodResult() {
    return null;
  }

  // invalid action-method
  public String getMyField() {
    return myField;
  }

  public boolean invalidActionMethodDueToWrongReturnType() throws Exception {
    return false;
  }

}