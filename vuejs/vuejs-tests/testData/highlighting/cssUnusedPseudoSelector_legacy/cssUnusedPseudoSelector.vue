<template>
  <div class="foo">
    <div class="bar">
      <span>text</span>
    </div>
  </div>

  <div class="subClass"></div>

  <div class="className">
    <div class="subClass"></div>
  </div>
</template>

<style scoped>
:deep(.foo > .bar) {
  color: red;
}

:deep(.foo) {
  color: red;
}

:local(.className .subClass) {
  color: green;
}

:not(.subClass) {
  color: beige;
}
</style>