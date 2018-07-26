<cfscript>
// Lock the current temp on the named lock.
    lock
            scope="application"
            type="exlusive"
            timeout="10"
    {
// Copy the application scope.
        request.appData = duplicate(application);
    }
// Start a transaction
// NOTE: We are not going to run any queries since that
// is not relevant at this point.
// NOTE: Trasnaction functionality has greatly increased
// in ColdFusion 9 as well, but can be covered later.
    transaction
            action="begin"
    {
// Run a query.
        include "insert_query.cfm";
// Save this rollback point.
// NOTE: This could have also been accomplished with the
// function-based alternateive:
// transactionSetSavepoint( "SP1" );
        transaction
                action="setSavePoint"
                savepoint="SP1"
        ;
// Run a query.
        include "insert_query.cfm";
// Check to see if something went wrong.
        if (false) {
// Roll back the transaction.
// NOTE: This can also be accomplished with the
// function-based laternative:
// transactionRollback();
            transaction action="rollback";
        }
// Roll back to our save point above.
// NOTE: This could have also been accomplished with the
// function-based alternative:
// transactionRollback( "SP1" );
        transaction
                action="rollback"
                savepoint="SP1"
        ;
    }
// Launch an asynchronous thread.
    thread
            action="run"
            name ="firstThread"
            appname="scriptdemo"
    {
// Store a return value.
        thread.returnValue = "From Thread One";
    }
// Launch a second thread.
    thread
            action="run"
            name ="secondThread"
            appname="scriptdemo"
    {
// Store a return value.
        thread.returnValue = "From Thread Two";
    }
// Sleep the current thread.
    thread
            action="sleep"
            duration="10"
        ;
// Check to see if our async thread has completed. If it has
// not completed, then terminate it.
    if (cfthread.firstThread.status neq "completed") {
// Terminate the thread.
// NOTE: This could have been accomplished with the
// function-based alternative:
// threadTerminate( "firstThread" );
        thread
                action="terminate"
                name ="firstThread"
        ;
    }
// Join the second thread to the page.
// NOTE: This could have also been accomplished with the
// function-based alternateive:
// threadJoin( "secondThread" );
    thread
            action="join"
            name ="secondThread"
        ;
</cfscript>