<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>cfmap tag - how to generate earth type google map in coldfusion</title>
</head>

<body style="margin: 5px 5px 5px 15px;">
<h2 style="color:IndianRed; font-style:italic">
    cfmap tag - how to generate earth
    <br/> type google map in coldfusion
</h2>
<hr width="525" align="left" color="Pink"/>
<br/>

<!---
    Generate a Google map api key for this domain
http://localhost:8500/
----------------
Use your own google map api key to test this example
--->

<cfajaximport params="#{googlemapkey='please use your own google map api key here'}#">
gfhfghf
<cfscript>
    quote = CreateObject( "component", "nasdaq.quote" ) ;
        <!--- Invocation using ordered arguments. --->
    res = quote.getLastTradePrice( "macr" ) ;
    if(1){
        t = "asdasrf";
    }
    while(1){
            lsdflsdk();
        t = 's';
    }
</cfscript>


<cfscript>
    myList = 'George,Paul,John,Ringo'; // creates the variable myList, which holds 4 names
    for (i = 1; i LTE listLen(myList); i = i+1){
            writeOutput(listGetAt(myList, i) & "<br>");
    }
    switch(myList){
        case 1: t='a'; break;
        case 2: t='b'; break;
        default: t='c'; break;
    }

    myRandomNumber = randRange(1,100);
    if (myRandomNumber GTE 1 AND myRandomNumber LTE 25) {
            writeOutput(myRandomNumber & ' is in the first quarter');
    } else if (myRandomNumber GTE 26 AND myRandomNumber LTE 50) {
            writeOutput(myRandomNumber & ' is in the second quarter');
    } else if (myRandomNumber GTE 51 AND myRandomNumber LTE 76) {
            writeOutput(myRandomNumber & ' is in the third quarter');
    } else {
            writeOutput(myRandomNumber & ' is in the fourth quarter');
    }

        //Set the variables

        acceptedApplicants[1] = "Cora Cardozo";
    acceptedApplicants[2] = "Betty Bethone";
    acceptedApplicants[3] = "Albert Albertson";
    rejectedApplicants[1] = "Erma Erp";
    rejectedApplicants[2] = "David Dalhousie";
    rejectedApplicants[3] = "Franny Farkle";
    applicants.accepted=acceptedApplicants;
    applicants.rejected=rejectedApplicants;
         rejectCode=StructNew();
    rejectCode["David Dalhousie"] = "score";
    rejectCode["Franny Farkle"] = "too late";

        //Sort and display accepted applicants

        ArraySort(applicants.accepted,"text","asc");
        WriteOutput("The following applicants were accepted:<hr>");
    for (j=1;j lte ArrayLen(applicants.accepted);j=j+1) {
            WriteOutput(applicants.accepted[j] & "<br>");
    }
        WriteOutput("<br>");

        //sort and display rejected applicants with reasons information

            ArraySort(applicants.rejected,"text","asc");
        WriteOutput("The following applicants were rejected:<hr>");
                 for (j=1;j lte ArrayLen(applicants.rejected);j=j+1) {
            applicant=applicants.rejected[j];
                    WriteOutput(applicant & "<br>");
                if (StructKeyExists(rejectCode,applicant)) {
            switch(rejectCode[applicant]) {
                case "score":
                        WriteOutput("Reject reason: Score was too low.<br>");
                        break;
                case "late":
                        WriteOutput("Reject reason: Application was late.<br>");
                        break;
                default:
                    WriteOutput("Rejected with invalid reason code.<br>");
            } //end switch
        } //end if
                else {
                WriteOutput("Reject reason was not defined.<br>");
                 } //end else
            WriteOutput("<br>");
             } //end for
            </cfscript>
<!--- Author: Charlie Arehart -- carehart.org --->
<!--- Find cookies other than CFID/CFTOKEN --->
        <cfparam name="test" default="1">

<!--- Tag-based structure loop --->
    <cfloop collection="#cookie#" item="cname">
        <cfif cname neq "cfid" and cname neq "cftoken">
                <cfoutput>
                        #cname# <br>
                </cfoutput>
        </cfif>
  </cfloop>

  <cfscript>
        try {
            z = 'e';
    }
        catch(Expression exception) {
            throw("asd");
    }
    catch(Any exception) {
        b = 'sdf';
    }
        // Script-based structure loop
    for (cname in cookie) {
        if (cname != "cfid" && cname != "cftoken"){
                writeoutput(cname & "<br>");
        }
    }
        switch(expression) {
             case "value1":
                t='d';
                break;
        case "value2":
            t='d';
                break;
      default: t='d';
    }
    function myFunction(arg1, arg2) {
                var value = 0;
        value ++;
       return value;
        }
</cfscript>
<cffunction name="myFunction" returntype="numeric">
                 <cfargument name="arg1" type="numeric">
        <cfargument name="arg2" type="numeric">
        <cfset var value = 0>
            [logic]
        <cfreturn value>
</cffunction>
<cftry>
                         [logic]
        <cfcatch type="Expression">
                    [logic];
        </cfcatch>
        <cfcatch type="Any">
                    [logic];
        </cfcatch>
</cftry>
<cfloop condition="#expression#">
        <cfoutput>
                        asl;dkal;sdk
                 </cfoutput>

    </cfloop>
                <cfthrow
        type="BusinessRule.Customer"
                         errorCode="66020"
        message="Non-wholesale customer"
        detail="Only registered wholesale customers may place bulk orders.">
<cfloop query="queryName">
        <cfoutput>
                    lsdfjsdlf
        </cfoutput>
        </cfloop>


                <cfswitch expression="">
        <cfcase value="value1">
                    [logic]
        </cfcase>
                <cfcase value="value2">
                    [logic]
        </cfcase>
                            <cfdefaultcase>
                    [logic]
        </cfdefaultcase>
</cfswitch>

<cfmap centeraddress="Nebraska"
        height="425"
      width="650"
        hideborder="no"
        title="earth type google map (Nebraska)"
        zoomlevel="6"
        zoomcontrol="large3d"
        typecontrol="none"
        type="earth">
            <b>
                asdasedasd
            </b>
                sd;flkd;lf
    ;sdlfksd;lfk
   </cfmap>

               </body>
</html>
