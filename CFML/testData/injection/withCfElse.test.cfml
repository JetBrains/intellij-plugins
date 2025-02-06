<cfquery datasource="test">
    <cfinclude template="test.cfm" />
    SELECT *                    <!---Common injection--->
    FROM Users u                <!---Common injection--->
    WHERE u.UserID = <cfqueryparam cfsqltype="cf_sql_integer" value="1"/>  <!---Common injection--->
    AND 1=1                     <!---Common injection--->
    <cfif apples IS apples>
        AND 2=2                 <!---Common injection--->
        <cfelseif apples IS apples>
        AND 3=3                 <!---Injection (2) --->
    <cfelse>
        AND 4=4                 <!---Injection (3) --->
    </cfif>
    AND 5=5                     <!---Common injection--->
</cfquery>