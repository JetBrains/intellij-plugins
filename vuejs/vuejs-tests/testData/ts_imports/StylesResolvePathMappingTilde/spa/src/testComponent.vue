<template>
  <div class="bar"></div>
</template>

<script lang="ts">
console.log("hello")
</script>

<style lang="scss">
@import '~/styles/foo';
@import 'styles/bar';
@use '~/styles/subdir';
@import '~/styles/<error>subdirUnknown</error>';
@import 'styles/<error>unknownName</error>';

#app {
  @include subdir.syntax-colors(
          $variable: #60b
  );
  @include subdir.<warning>syntax-colors1</warning>(
            $variable: #60b
  )
}

</style>
