<script
  setup
  lang="ts"
  generic="Clearable extends boolean, ValueType extends string | number | null | undefined"
>
type OnChange<VT, C> = C extends true
  ? (value: VT | null) => void
  : (value: VT) => void;

defineProps<{
  clearable?: Clearable;
  value?: ValueType;
  onChange?: OnChange<ValueType, Clearable>;
}>()
</script>