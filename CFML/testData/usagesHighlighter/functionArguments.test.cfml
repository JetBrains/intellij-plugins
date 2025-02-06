<cfcomponent name="component">
  <cffunction name="func1">
    <cfargument name="arg">
  </cffunction>

  <cffunction name="func">
    <cfargument name="arg">
      <cfset a = arguments.arg>
      <cfset v = a + 10>
      <cfset v = a<caret>rg + 10>
  </cffunction>

  <cffunction name="func2">
    <cfargument name="arg">
  </cffunction>
</cfcomponent>