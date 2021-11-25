<i18n locale="en" lang="yaml"></i18n>
<i18n src="foo.json"></i18n>
<i18n lang="json">
{
  "foo"<EOLError descr="':' expected, got '}'"></EOLError>
}
</i18n>
<i18n lang=<warning descr="foo is not a valid value for lang. Expected: json or yaml">"foo"</warning>></i18n>
<i18n <warning descr="Attribute foo is not allowed here">foo</warning>="foo"></i18n>