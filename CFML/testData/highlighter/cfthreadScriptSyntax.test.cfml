<cfscript>
    lock
            scope="application"
            type="exlusive"
            timeout="10"
    {
        request.appData = duplicate(application);
    }

    transaction
            action="begin"
    {
        include "insert_query.cfm";
        transaction
                action="setSavePoint"
                savepoint="SP1"
        ;
        include "insert_query.cfm";
        if (false) {
            transaction action="rollback";
        }
        transaction
                action="rollback"
                savepoint="SP1"
        ;
    }
    thread
            action="run"
            name ="firstThread"
            appname="scriptdemo"
    {
        thread.returnValue = "From Thread One";
    }
    thread
            action="run"
            name ="secondThread"
            appname="scriptdemo"
    {
        thread.returnValue = "From Thread Two";
    }
    thread
            action="sleep"
            duration="10"
        ;
    if (cfthread.firstThread.status neq "completed") {
        thread
                action="terminate"
                name ="firstThread"
        ;
    }
    thread
            action="join"
            name ="secondThread"
        ;
</cfscript>