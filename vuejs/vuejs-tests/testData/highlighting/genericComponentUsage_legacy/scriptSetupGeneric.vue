<script setup lang="ts" generic="Clearable extends boolean, ValueType extends MyData">
export type MyData = {
  name3: string,
  phone: string,
}

type OnChange<VT, C> = C extends true
  ? (value: VT | null) => void
  : (value: VT) => void;

defineProps<{
  clearable?: Clearable;
  value?: ValueType;
  onChange?: OnChange<ValueType, Clearable>;
}>()

</script>
