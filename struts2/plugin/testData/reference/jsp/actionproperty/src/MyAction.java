public class MyAction {

  private String myField;
  private boolean myBooleanField;

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