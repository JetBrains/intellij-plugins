<cfcomponent output="false">
  <cffunction name="getComponentRelativeName">
    <cfset metaData = GetMetaData(this)>
    <cfset absoluteName = metaData.<weak_warning descr="Can't resolve">Name</weak_warning>>
    <cfreturn absoluteName>
  </cffunction>
  <cffunction name="getFoo">
    <cfset metaData = GetMetaData(this)>
  </cffunction>
</cfcomponent>