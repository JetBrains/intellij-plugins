SOME_SETTING = 2
CLI_BUILD_FLAGS = "-X main.version=${PDC_VERSION} -X main.date=$(shell date +"%Y-%m-%dT%TZ")"

uninstall-crds:
	kubectl get crds -o name | grep -E 'foo|bar|baz' | xargs -n1 kubectl delete

remove-finalizers:
	kubectl ${KUBECTL_ARGS} get foo,bar,baz -oname | xargs -I {} kubectl ${KUBECTL_ARGS} patch {} --type="merge" -p {"metadata":{"finalizers":[]}}

clean-force: remove-finalizers clean

clean:
	echo Cleaning up...