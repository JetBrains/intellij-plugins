<cfcomponent name="myComponent">
  <cffunction name="myFunction1">
    <cfscript>
      myFunction();
    </cfscript>
  </cffunction>

  <cffunction name="myFunc<caret>tion">
  </cffunction>

  <cffunction name="myFunction1">
    <cfinvoke method="myFunction">
  </cffunction>
</cfcomponent>