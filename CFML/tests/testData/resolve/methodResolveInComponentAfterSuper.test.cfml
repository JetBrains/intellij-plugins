<cfcomponent extends="folder.subfolder.ComponentName">
  <cffunction name="func2" returntype="MyComponentName">
    <cfset var salary = 1.5 * super.fun<caret>c2()  >
    <cfreturn salary>
  </cffunction>
</cfcomponent>