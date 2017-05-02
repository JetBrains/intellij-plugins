<cfset news = queryNew("id,title", "integer,varchar")>
<cfset queryAddRow(news)>
<cfset querySetCell(news, "id", "1")>
<cfset querySetCell(news, "title", "Dewey defeats Truman")>
<cfset queryAddRow(news)>
<cfset querySetCell(news, "id", "2")>
<cfset querySetCell(news, "title", "Men walk on Moon")>
<cfset writeDump(news)>

<cfquery datasource="#Application.DNSdatasource#" username="#Application.DSNuser#" password="#Application.DSNpass#" name="TEST" >
    SELECT TOP 1
        *
<!--- create a dummy query using queryNew --->
    FROM
        <CFIF 1 EQ 1>
  SB_INVOICE
<CFELSE>
  SB_INVOICE_ITEM
</CFIF>
  INNER JO<caret>IN SB_PATIENT_EPISODE
  ON SB_INVOICE.SB_EPISODE_NUMBER = SB_PATIENT_EPISODE.SB_EPISODE_NUMBER
  WHERE
  SB_INVOICE_ID IS NOT NULL
</cfquery>