<template>
  <div>
    <div <error descr="Vue: v-slot can only be used on components or <template> tags.">v-slot:<weak_warning descr="Unrecognized slot name">name</weak_warning>="propName"</error>>
      {{ propName + <weak_warning descr="Unresolved variable or type wrongName">wrongName</weak_warning> }}
    </div>
    <div <error descr="Vue: v-slot can only be used on components or <template> tags.">v-slot:<weak_warning descr="Unrecognized slot name">name</weak_warning>="{prop1, prop2}"</error>>
      {{ prop1 + <weak_warning descr="Unresolved variable or type wrongName">wrongName</weak_warning>}}
    </div>
    <div <error descr="Vue: v-slot can only be used on components or <template> tags.">v-slot:<weak_warning descr="Unrecognized slot name">name</weak_warning></error>></div>
    <div <error descr="Vue: v-slot can only be used on components or <template> tags.">v-slot="propName"</error>></div>
    <div <error descr="Vue: v-slot can only be used on components or <template> tags.">v-slot</error>></div>

    <div <error descr="Vue: v-slot can only be used on components or <template> tags.">#<weak_warning descr="Unrecognized slot name">name</weak_warning>="propName"</error>></div>
    <div <error descr="Vue: v-slot can only be used on components or <template> tags.">#<weak_warning descr="Unrecognized slot name">name</weak_warning></error>></div>

    <div v-<warning descr="Unrecognized Vue directive">slots</warning>:<warning descr="Unrecognized argument">name</warning>="<weak_warning descr="Unresolved variable or type propName">propName</weak_warning>"></div>
    <div v-<warning descr="Unrecognized Vue directive">slots</warning>:<warning descr="Unrecognized argument">name</warning>></div>
    <div v-<warning descr="Unrecognized Vue directive">slots</warning>="<weak_warning descr="Unresolved variable or type propName">propName</weak_warning>"></div>
    <div v-<warning descr="Unrecognized Vue directive">slots</warning>></div>

    <div <error descr="Vue: v-slot can only be used on components or <template> tags.">#<warning descr="Missing slot name"></warning>="propName"</error>></div>
    <div <error descr="Vue: v-slot can only be used on components or <template> tags.">#<warning descr="Missing slot name"></warning></error>></div>
  </div>
</template>