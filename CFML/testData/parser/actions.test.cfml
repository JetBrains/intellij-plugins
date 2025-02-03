<cfscript>
  // IDEA-85678 writeLog in cfscript shows a syntax error
  writeLog(application = true, file = "someLogFile", type = "error", text = "some log message");

  writeLog = {
    // an action
  };

  // IDEA-85991 param directive in cfscript shows a syntax error
  param name="rc.firstname" default="#rc.customer.getFirstName()#";
</cfscript>

