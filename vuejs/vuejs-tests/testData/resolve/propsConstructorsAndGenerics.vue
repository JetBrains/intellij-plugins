<script lang="ts">
import {defineComponent} from "vue";
import type {PropOptions} from "vue";

export const useInputComponentProps = <T>() => ({
  value: {} as PropOptions<T | undefined>,
});

export const useTextFieldComponentProps = <T>() => ({
  ...useInputComponentProps<T>(),
  autofocus: Boolean,
});

export default defineComponent(
  {
    name: 'PropsConstructorsAndGenerics',
    props: {
      ...useTextFieldComponentProps<number>(),
      msg: {type: String, required: true}
    }
  }
);
</script>

<template>
  <PropsConstructorsAndGenerics msg="You did it!" autofocus :value="23"/>
</template>