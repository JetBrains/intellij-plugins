<template>
  <div>
    <div v-slot:<weak_warning descr="Unrecognized slot name">name</weak_warning>="propName">
      {{ propName + <weak_warning descr="Unresolved variable or type wrongName">wrongName</weak_warning> }}
    </div>
    <div v-slot:<weak_warning descr="Unrecognized slot name">name</weak_warning>="{prop1, prop2}">
      {{ prop1 + <weak_warning descr="Unresolved variable or type wrongName">wrongName</weak_warning>}}
    </div>
    <div v-slot:<weak_warning descr="Unrecognized slot name">name</weak_warning>></div>
    <div v-slot="propName"></div>
    <div v-slot></div>

    <div #<weak_warning descr="Unrecognized slot name">name</weak_warning>="propName"></div>
    <div #<weak_warning descr="Unrecognized slot name">name</weak_warning>></div>

    <div v-<warning descr="Unrecognized Vue directive">slots</warning>:<warning descr="Unrecognized argument">name</warning>="<weak_warning descr="Unresolved variable or type propName">propName</weak_warning>"></div>
    <div v-<warning descr="Unrecognized Vue directive">slots</warning>:<warning descr="Unrecognized argument">name</warning>></div>
    <div v-<warning descr="Unrecognized Vue directive">slots</warning>="<weak_warning descr="Unresolved variable or type propName">propName</weak_warning>"></div>
    <div v-<warning descr="Unrecognized Vue directive">slots</warning>></div>

    <div #<warning descr="Missing slot name"></warning>="propName"></div>
    <div #<warning descr="Missing slot name"></warning>></div>
  </div>
</template>