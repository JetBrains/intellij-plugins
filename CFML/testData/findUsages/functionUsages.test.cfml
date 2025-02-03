<cffunction name="getInsertedID" output="no" returntype="numeric">
  <cfargument name="qryResult" type="struct" required="yes">
  <cfargument name="dsn" type="string" default="">

  <cfscript>
    switch (getDbType(arguments.dsn)) {
      case 'sqlserver':
        return arguments.qryResult.IDENTITYCOL;
      case 'mysql':
        return arguments.qryResult.GENERATED_KEY;
      case 'oracle':
        return arguments.qryResult.ROWID; // ??? cf docs: "Oracle only. The ID of an inserted row. This is not the primary key of the row, although you can retrieve rows based on this ID."
      case 'sybase':
        return arguments.qryResult.SYB_IDENTITY;
      case 'informix':
        return arguments.qryResult.SERIAL_COL;
      default:
      throw('[getInsertedID] Unsupported dbType.');
    }
  </cfscript>
</cffunction>
<cfcomponent>
  <cffunction name="getDbTyp<caret>e" output="no" returntype="string">
    <cfargument name="dsn" type="string" default="">
  </cffunction>

</cfcomponent>