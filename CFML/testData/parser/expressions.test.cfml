<cfscript>
  var arr = [];
  var obj = {};

  public array function getOptions() {
    return (isNull(variables.options)) ? [] : variables.options;
  }

  public void function setOptions(array options = []) {

  }

  public void function setOptions(struct options = {}) {

  }

  someComponent.somefunction(somearg = 1, test = { "somekey" = "somevalue" });

  public void function addRoute( any routes, string target, any methods = [ ], string statusCode = '' ) {
  }
</cfscript>