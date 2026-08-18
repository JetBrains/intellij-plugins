
# GSettings
@GSETTINGS_RULES@
gsettings_SCHEMAS = org.gnome.Publisher.gschema.xml

@example/lib:
	echo "build lib"

@example/build: @example/lib
	echo "build app"