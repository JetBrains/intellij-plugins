<cfcomponent name="myComponent">
  <cffunction name="myFunction1">
    <cfscript>
      myFun<caret>ction();
    </cfscript>
  </cffunction>

  <cffunction name="myFunction">
  </cffunction>

  <cffunction name="myFunction1">
    <cfinvoke method="myFunction">
  </cffunction>
</cfcomponent>