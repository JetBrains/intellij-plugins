<cfcomponent>
    <cffunction name="updateMapping" access="public" output="true" returntype="any">
        <cfargument name="name" type="string" required="true">
        <cfargument name="physicalDir" type="string" required="true">

        <cfadmin
                action="updateMapping"
                type="web"
                password="#variables.config.railoAdminPassword#"
                virtual="/#arguments.name#"
                physical="#arguments.physicalDir#"
                archive=""
                primary=false
                trusted=false>
    </cffunction>
</cfcomponent>
