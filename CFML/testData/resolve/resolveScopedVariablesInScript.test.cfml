<cfscript>
  url.test = "1";
  variables.test2 = "2";
  form.test3 = "3";
  variables.atest4 = "4";
  request.atest4 = 12;
  writeOutput(test);
  writeOutput(test2);
  writeOutput(test3);
  writeOutput(at<caret>est4);
</cfscript>