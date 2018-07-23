import org.apache.tapestry5.services;

class TestModule {
  public static void contributeComponentClassResolver(){
    new LibraryMapping("foo", "com.testapp.components.other");
  }
}