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

public class MyAction {

  private String myField;
  private boolean myBooleanField;
  private boolean mySetterOnlyField;

  private List readonlyList;
  
  private User user;

  public User getUser() {
    return user;
  }
  
  public String getMyField() {
    return myField;
  }

  public void setMyField(String myField) {
    this.myField = myField;
  }

  public boolean isMyBooleanField() {
    return myBooleanField;
  }

  public void setMyBooleanField(boolean myBooleanField) {
    this.myBooleanField = myBooleanField;
  }

  public List getReadonlyList() {
    return readonlyList;
  }

  public void setMySetterOnlyField(final boolean mySetterOnlyField) {
    this.mySetterOnlyField = mySetterOnlyField;
  }

  public class User {
    
    private String foreName;

    public String getForeName() {
      return foreName;
    }

    public String setForeName(String foreName) {
      this.foreName = foreName;      
    }
    
  }
  
}