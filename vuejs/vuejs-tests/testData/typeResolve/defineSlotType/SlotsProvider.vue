<script setup lang="ts">
defineSlots<{
  default: (props: { msg: string }) => any
  header?: (props: { pageTitle?: string }) => any
  footer: (props: { year?: number }) => any
}>()
</script>