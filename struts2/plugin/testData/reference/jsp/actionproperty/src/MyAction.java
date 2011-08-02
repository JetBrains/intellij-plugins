public class MyAction {

  private String myField;
  private boolean myBooleanField;

  private List readonlyList;

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

}