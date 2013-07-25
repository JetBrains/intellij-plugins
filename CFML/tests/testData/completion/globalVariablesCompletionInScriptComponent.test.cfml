component {
  ins = "sad";
  function setup()
  {
    var injector = 12; //createObject("component", "coldspring.util.MethodInjector").init();
    object = 1; //createObject("component", "unittests.util.com.Observer").init();

  }

  function testIncludeFile()
  {
    writeOutput(obj<caret>);
  }

}
