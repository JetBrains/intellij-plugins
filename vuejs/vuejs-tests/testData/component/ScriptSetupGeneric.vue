<script
  setup
  lang="ts"
  generic="Clearable extends boolean, ValueType extends string | number | null | undefined"
>
type OnChange<ValueType, Clearable> = Clearable extends true
  ? (value: ValueType | null) => void
  : (value: ValueType) => void;

defineProps<{
  clearable?: Clearable;
  value?: ValueType;
  onChange?: OnChange<ValueType, Clearable>;
}>()
</script>