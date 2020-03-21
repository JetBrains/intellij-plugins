projects = someProject1 someProject2

define project_rule
    clean-$1:
		sbt 'project $1' clean
endef

$(foreach f,$(projects),$(eval $(call project_rule,$f)))