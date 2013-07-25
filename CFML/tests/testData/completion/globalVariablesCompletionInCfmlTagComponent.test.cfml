<cfcomponent>
  <cfset ins = "sad">
  <cffunction name="setup" hint="setup method" access="public" returntype="void" output="false">
    <cfscript>
      injector = 12; //createObject("component", "coldspring.util.MethodInjector").init();
      object = 1; //createObject("component", "unittests.util.com.Observer").init();
    </cfscript>
    <cfset var adfg = "12">
  </cffunction>

  <cffunction name="testIncludeFile" hint="test including a file" access="public" returntype="void" output="true">
    <cfscript>
      writeOutput(inj<caret>);
    </cfscript>

  </cffunction>
</cfcomponent>

