<cfzip file="c:\work\instr.zip" action="readBinary"
    entryPath="com/test/abc.jpg" variable="xyz">
<cfzip file="c:\work\copy_instr.zip">
    <cfzipparam entryPath="com/test/xyz.jpg" content="#xyz#">
</cfzip>