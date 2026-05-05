<script setup lang="ts">

export interface Props {
  items: string[]
}

const props = withDefaults(defineProps<Props>(), {
  items: () => [],
})

const props_string = withDefaults(
  defineProps<{
    title?: string;
  }>(),
  {
    title: "---",
  }
);

export interface PropsSecond {
  msg?: string
  labels?: string[]
}

const props_second = withDefaults(defineProps<PropsSecond>(), {
  msg: 'hello',
  labels: () => ['one', 'two']
});
</script>

<template>
</template>