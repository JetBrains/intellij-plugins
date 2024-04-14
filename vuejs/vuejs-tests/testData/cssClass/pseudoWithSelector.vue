<template>
  <div class="foo">
    <div class="bar">
      <span>text</span>
    </div>
  </div>

  <div class="background">
    <div class="card"></div>
  </div>
</template>

<style scoped>
:deep(.foo > .bar) {
  color: red;
}

:deep(.foo) {
  color: red;
}

:deep(.background > .card) {
  color: white;
}
</style>