<template>
  <div id="schedules">
    <h2>Openingsuren</h2>
    <ul>
      <li v-for="{time_closed, time_open} in schedules">
        {{ t<caret>ime_open }} - {{ time_closed }}
      </li>
    </ul>
  </div>
</template>
