<cfcomponent name="myComponent">
  <cffunction name="myFunction1">
    <cfscript>
      myFunction();
    </cfscript>
  </cffunction>

  <cffunction name="myFunction">
  </cffunction>

  <cffunction name="myFunction1">
    <cfinvoke method="myFunc<caret>tion">
  </cffunction>
</cfcomponent>