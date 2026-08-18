.PHONY: help
help:
	echo "$$HELPTEXT"

export HELPTEXT
define HELPTEXT

Here is some useful help text...
     1. This is indented
     2. And so is this

   kubectl get pod -l pgo-pg-database=true -o name -n backing | xargs -I% kubectl exec % --\\
    psql -c "SELECT ssl.pid, usename, datname, ssl, client_addr, backend_type, wait_event\\
    FROM pg_catalog.pg_stat_ssl ssl, pg_catalog.pg_stat_activity a\\
    WHERE ssl.pid = a.pid;"

endef