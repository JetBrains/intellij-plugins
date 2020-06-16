<template>
  <div id="schedules">
    <h2>Openingsuren</h2>
    <ul>
      <li v-for="{time_closed, time_open : newName} in schedules">
        {{ n<caret>ewName }} - {{ time_closed }}
      </li>
    </ul>
  </div>
</template>
