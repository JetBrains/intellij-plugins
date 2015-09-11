<script type="text/javascript">
  var <info>asdf</info> = 123; // <info>TODO: fix this</info>
</script>

<cfscript>
   asdf = 123; // <info>TODO: fix this</info>
</cfscript>

<cfquery>
  SELECT *
  FROM test_database
  WHERE username = <cfqueryparam value="#attributes.username#">
  -- <info>TODO: fix it</info>
</cfquery>