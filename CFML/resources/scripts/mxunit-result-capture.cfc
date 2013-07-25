<cfcomponent displayname="mxunit-result-capture" output="no" extends="mxunit.framework.TestResult">

   <cfparam name="myTestCaseName" type="any" default="" />
   <cfparam name="myTestComponentName" type="any" default="" />
   <cfparam name="myComponentFilePath" type="any" default="" />

 <cffunction name="init">
  <cfargument name="path" type="any" required="yes">
  <cfscript>
    myComponentFilePath = arguments.path;
  </cfscript>
 </cffunction>

 <cffunction name="formatOutput">
    <cfargument name="string" type="any" required="yes">
    <cfscript>
        writeoutput(/*"<br>" & */arguments.string);
    </cfscript>
    <cfflush interval="1">
 </cffunction>

 <cffunction name="getExceptionAsString">
    <cfargument name="exception" type="any" required="yes">
  <cfscript>
    var e = arguments.exception;
    var resultString = "Error type: " &
        e.type & "\n";
    if (e.detail != "") {
        resultString = resultString & "Detail: " & e.detail & "\n";
    }
    if (e.errorcode != "") {
        resultString = resultString & "Error code: " & e.errorcode & "\n";
    }
  </cfscript>

    <cfset tArray = e.tagContext>
    <cfloop from="1" to="#arrayLen(tArray)#" index="i">
        <cfset resultString = resultString & tArray[i].template & " (" & tArray[i].line & ")\n">
    </cfloop>
    <cfscript>
    return resultString;
    </cfscript>
 </cffunction>


<cffunction name="escape">
    <cfargument name="text" type="any" required="yes">
    <cfscript>
        var t = arguments.text;
        t = replace(t, "|", "||", "all");
        t = replace(t, "'", "|'", "all");
        t = replace(t, "\n", "|n", "all");
        t = replace(t, "\r", "|r", "all");
        t = replace(t, "]", "|]", "all");
        return t;
    </cfscript>
</cffunction>

<cffunction name="traceCommand">
    <cfargument name="command" type="any" required="yes">
    <cfargument name="param1Name" required="yes">
    <cfargument name="param1Value" required="yes">
    <cfargument name="param2Name" required="no" default="">
    <cfargument name="param2Value" required="no" default="">
    <cfargument name="param3Name" required="no" default="">
    <cfargument name="param3Value" required="no" default="">

    <cfscript>
        var line = "####teamcity[" & arguments.command & " " & arguments.param1Name & "='" & escape(arguments.param1Value) & "'";
        if (arguments.param2Name != "") {
            line = line & " " & arguments.param2Name & "='" & escape(arguments.param2Value) & "'";
        }
     if (arguments.param3Name != "") {
         line = line & " " & arguments.param3Name & "='" & escape(arguments.param3Value) & "'";
     }

     line = line & "]" & Chr(10);
     return line;
    </cfscript>

</cffunction>

<!---
  Initialize the test result item struct each time and populate it with meta data
 --->
 <cffunction name="startTest" access="public" returntype="void" >
   <cfargument name="testCase" type="any" required="yes" />
   <cfargument name="componentName" type="any" required="yes" />
    <cfscript>
        myTestComponentName = arguments.componentName;
        myTestCaseName = arguments.testCase;
        formatOutput(traceCommand("testStarted", "name", myTestCaseName,
        "locationHint", "php_qn://" & myComponentFilePath & "::" & myTestCaseName));
        super.startTest(arguments.testCase, arguments.componentName);
   </cfscript>
 </cffunction>


<!---
  Add the test result item to the test results array
 --->
 <cffunction name="endTest" access="public" returntype="any">
   <cfargument name="testCase" type="any" required="yes" />
    <cfscript>
        var testName = arguments.testCase;
        formatOutput(traceCommand("testFinished", "name", testName/*, "duration", (int)(round($time, 2) * 1000)*/ ));
        // arrayAppend(this.results,this.resultItem);
        super.endTest(arguments.testCase);
    </cfscript>
 </cffunction>


<!---
 If anything goes wrong, capture the entire exception.
 --->
<cffunction name="addError" access="public" returntype="void">
  <cfargument name="exception" type="any" required="yes" />
  <cfscript>
    formatoutput(traceCommand("testFailed", "name", myTestCaseName, "message", arguments.exception.message, "details", getExceptionAsString(arguments.exception)));
    super.addError(arguments.exception);
  </cfscript>
</cffunction>

<cffunction name="addFailure" access="public" returntype="void">
  <cfargument name="exception" type="any" required="yes" />
  <!--- TestResult.addError() <br /> --->
  <cfscript>
  //         print(traceCommand("testFailed", "name", $test->getName(), "message", $message, "details", /*getPrettyTrace($e->getTraceAsString())*/ getTraceMessage($e->getTrace(), $this->myfilename)));

    formatoutput(traceCommand("testFailed", "name", myTestCaseName, "message", arguments.exception.message, "details", getExceptionAsString(arguments.exception)));
    super.addFailure(arguments.exception);
  </cfscript>
</cffunction>


<!---
  If the test passes, store that.
 --->
<cffunction name="addSuccess" access="public" returntype="void">
  <cfargument name="message" type="string" required="yes" />
 <cfscript>
    // formatOutput("Success: " & arguments.message);
    super.addSuccess(arguments.message);
  </cfscript>
</cffunction>

<!---
 <cffunction name="addTrace" access="public" returntype="void">
  <cfargument name="message" type="any" required="no" default="" />
  <cfscript>
    formatOutput("Trace: " & message.toString());
    super.addTrace(arguments.message);
  </cfscript>
</cffunction>
--->
</cfcomponent>