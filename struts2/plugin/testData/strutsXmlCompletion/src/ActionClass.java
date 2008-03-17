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

  public String invalidActionMethodDueToWrongExceptionType() throws IllegalStateException {
    return null;
  }

  public boolean invalidActionMethodDueToWrongReturnType() throws Exception {
    return false;
  }

}