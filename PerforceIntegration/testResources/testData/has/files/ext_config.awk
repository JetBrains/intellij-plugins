#!/usr/bin/awk -f
#
# Configure the authentication extension for testing.
#

/^ExtP4USER:/ {
    print "ExtP4USER:\tsuper";
    next;
}

/Service-URL:/ {
    print;
    print "\t\thttps://auth-svc.doc:3000/";
    getline;
    next;
}

/enable-logging:/ {
    print;
    print "\t\ttrue";
    getline;
    next;
}

/name-identifier:/ {
    print;
    print "\t\tnameID";
    getline;
    next;
}

/non-sso-groups:/ {
    print;
    print "\t\tno_timeout";
    getline;
    next;
}

{print}
