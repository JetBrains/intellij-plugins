<html>
<cfcomponent name="mxunit-launcher">
        <cfset sep = createObject("java","java.lang.System").getProperty("file.separator")>

	<cffunction name="normalizeDirectory" access="private">
		<cfargument name="directory" type="string" required="true"/>
		<cfset var dir = createObject("java","java.io.File")>

		<cfif not DirectoryExists(arguments.directory)>
			<cfset arguments.directory = expandPath(arguments.directory) />
			<cfif not DirectoryExists(arguments.directory)>
                            <cfthrow message="Directory #directory# does not exist">
			</cfif>
		</cfif>

		<cfset dir.init(arguments.directory)>
		<cfset arguments.directory = dir.getCanonicalPath()>
		<cfset arguments.directory = replaceList(arguments.directory,"/,\","#sep#,#sep#")>
		<cfset arguments.directory = arguments.directory & sep>
		<cfreturn arguments.directory>
	</cffunction>

	<cffunction name="getDirectoryQuery" access="private">
		<cfargument name="directory" required="true" hint="directory of tests to run">
		<cfset var files = "">
		<cfset var runnerUtils = "">

		<cfif not DirectoryExists(arguments.directory)>
			<cfthrow message="Directory #directory# does not exist">
		</cfif>

		<cfif ListFirst(server.ColdFusion.ProductVersion) GT 6>
			<cfdirectory action="list" directory="#arguments.directory#" name="files" recurse="true" filter="*.cfc">
		<cfelse>
			<cfset runnerUtils = createObject("component","mxunit.framework.RunnerUtils")>
			<cfset files = runnerUtils.directoryList(directory = arguments.directory, filter="*.cfc", recurse = true)>
		</cfif>

		<cfreturn files>
	</cffunction>

	<cffunction name="accept" access="private" output="false">
		<cfargument name="test" required="true">

		<cfset var testName = ListLast(arguments.test,".")>

		<cfif NOT reFindNoCase("^test",testName) AND NOT reFindNoCase("test$",testName)>
			<cfreturn false>
		</cfif>
        <cfreturn true>
	</cffunction>

	<cffunction name="getTests" access="private">
		<cfargument name="directory" required="true">
		<cfargument name="directoryRelativeName" required="true">
		<cfset q_tests = getDirectoryQuery(directory)>
		<cfset a_tests = ArrayNew(1)>
		<cfset testPath = "">

		<cfloop query="q_tests">
    		<cfset testPath = replaceNoCase(q_tests.directory & sep & q_tests.Name, directory, directoryRelativeName & ".")>
            <cfset testPath =  reverse(replace(reverse(testPath), "cfc.", ""))>
            <cfset testPath = reReplace(testPath, "(\\|/|\.){1,}" ,".","all")>

			<cfif accept(testPath)>
              <cfset ArrayAppend(a_tests, testPath)>
			</cfif>
		</cfloop>
		<cfreturn a_tests>
	</cffunction>

	<cffunction name="executeDirectory" access="remote">
	    <cfargument name="directoryName" type="String" required="true">

            <cfif len(directoryName)>
                <cfset testsResult = createObject("component", "mxunit-result-capture")>
        		<cfset files = "">
        		<cfset i = 1>

        		<cfset directory = normalizeDirectory("." & sep & arguments.directoryName)>
        		<cfset files = getTests(directory, arguments.directoryName) />

        		<cfloop from="1" to="#ArrayLen(files)#" index="i">
                    <cftry>
                        <cfset obj = createObject("component", files[i])>
                        <cfset metaData = getMetaData(obj)>
                        <cfset testsResult.init(metaData.path)>
                        <cfset testsResult.formatOutput(testsResult.traceCommand("testSuiteStarted", "name", metaData.name,
                          "locationHint", "php_qn://" &  metaData.path))>
                            <cfset suite = createObject("component", "mxunit.framework.TestSuite")>
                            <cfset suite.addAll(metaData.name, obj)>
                            <cfset suite.run(testsResult)>
                        <cfset testsResult.formatOutput(testsResult.traceCommand("testSuiteFinished", "name", metaData.name))>
                    <cfcatch>
                    </cfcatch>
                    </cftry>
        		</cfloop>
            </cfif>
	</cffunction>

	<cffunction name="executeTestCase" access="remote">
            <cfargument name="componentName" type="String" required="true">
            <cfargument name="methodName" type="String" required="no" default="">

            <cfset suite = createObject("component", "mxunit.framework.TestSuite")>
            <cfset obj = createObject("component", componentName)>
            <cfset metaData = getMetaData(obj)>
            <cfset testsResult = createObject("component", "mxunit-result-capture")>
            <cfset testsResult.init(metaData.path)>

            <cfset testsResult.formatOutput(testsResult.traceCommand("testSuiteStarted", "name", componentName,
              "locationHint", "php_qn://" &  metaData.path))>
    		<cfif len(arguments.methodName)>
	    		<cfset suite.add(componentName, arguments.methodName, obj)>
		    	<cfset suite.run(testsResult, arguments.methodName)>
		    <cfelse>
                  <cfset suite.addAll(componentName, obj)>
		          <cfset allMethods = getComponentMethods(componentName)>
        		  <cfloop from="1" to="#arrayLen(allMethods)#" index="i">
                  </cfloop>
                  <cfset suite.run(testsResult)>
    		</cfif>
            <cfset testsResult.formatOutput(testsResult.traceCommand("testSuiteFinished", "name", componentName))>
    </cffunction>

	<cffunction name="getComponentMethods" access="remote" returntype="array">
		<cfargument name="ComponentName" required="true" type="string" hint="">
		<cfset var methods = ArrayNew(1)>
		<cfset var obj = "">
		<cftry>
			<cfset obj = createObject("component",ComponentName)>
			<cfset methods = obj.getRunnableMethods()>
		<cfcatch>
			<cfset ArrayAppend(methods, listLast(arguments.ComponentName,".") & " <ERROR: #cfcatch.Message#>")>
		</cfcatch>
		</cftry>

		<cfreturn methods>
	</cffunction>
</cfcomponent></html>