<template>
  <div>
    <div slot="name" <warning descr="slot-scope is deprecated, consult docs for better alternative">slot-scope</warning>="propName">
      {{ propName + <weak_warning descr="Unresolved variable or type wrongName">wrongName</weak_warning> }}
    </div>
    <div slot="name" <warning descr="slot-scope is deprecated, consult docs for better alternative">slot-scope</warning>="{prop1, prop2}">
      {{ prop1 }}
    </div>
    <div slot="name"></div>
    <div <warning descr="slot-scope is deprecated, consult docs for better alternative">slot-scope</warning>="propName"></div>
    <div slot <warning descr="slot-scope is deprecated, consult docs for better alternative"><warning descr="slot-scope requires value">slot-scope</warning></warning>></div>
    <div <warning descr="Attribute scope is not allowed here">scope</warning>="foo"></div>
    <template <warning descr="scope is deprecated, consult docs for better alternative">scope</warning>="foo"></template>
  </div>
</template>