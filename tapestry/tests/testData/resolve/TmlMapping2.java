import org.apache.tapestry5.services;

class TestModule {
  private static final String CONSTANT = "foo";
  private static final String CONSTANT2 = "com.testapp.components.other";
  public static void contributeComponentClassResolver(){
    new LibraryMapping(CONSTANT, CONSTANT2);
  }
}