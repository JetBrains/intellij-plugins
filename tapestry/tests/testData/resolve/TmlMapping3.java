class TestModule {
  @Contribute( ComponentClassResolver.class )
  public static void setupLibraryMapping(Configuration<LibraryMapping> configuration)
  {
    configuration.add(new LibraryMapping("wf", "dk.nesluop.librarymapping.framework"));
  }
}