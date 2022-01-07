<template>
  <div>
    <div slot="name" <weak_warning descr="Deprecated symbol used, consult docs for better alternative">slot-scope</weak_warning>="propName">
      {{ propName + <weak_warning descr="Unresolved variable or type wrongName">wrongName</weak_warning> }}
    </div>
    <div slot="name" <weak_warning descr="Deprecated symbol used, consult docs for better alternative">slot-scope</weak_warning>="{prop1, prop2}">
      {{ prop1 }}
    </div>
    <div slot="name"></div>
    <div <weak_warning descr="Deprecated symbol used, consult docs for better alternative">slot-scope</weak_warning>="propName"></div>
    <div slot <warning descr="slot-scope requires value"><weak_warning descr="Deprecated symbol used, consult docs for better alternative">slot-scope</weak_warning></warning>></div>
    <div <warning descr="Attribute scope is not allowed here">scope</warning>="foo"></div>
    <template <weak_warning descr="Deprecated symbol used, consult docs for better alternative">scope</weak_warning>="foo"></template>
  </div>
</template>