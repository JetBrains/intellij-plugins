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
    <template <warning descr="Vue: the \"scope\" attribute for scoped slots have been deprecated and replaced by \"slot-scope\" since 2.5. The new \"slot-scope\" attribute can also be used on plain elements in addition to <template> to denote scoped slots."><warning descr="scope is deprecated, consult docs for better alternative">scope</warning>="foo"</warning>></template>
  </div>
</template>