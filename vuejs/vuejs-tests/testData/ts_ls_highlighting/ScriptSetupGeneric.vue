<script lang="ts">
interface MyObj {
  a: string
}
</script>

<script
  setup
  lang="ts"
  generic="Clearable extends Boolean, ValueType extends MyObj, Foo<error descr=", expected"> </error>Bar"
>
type OnChange<ValueType, Clearable> = Clearable extends true
  ? (value: ValueType | null) => void
  : (value: ValueType) => void;

const {value} = defineProps<{
  clearable?: Clearable;
  value: ValueType;
  onChange?: OnChange<ValueType, Clearable>;
}>()
<error descr="TS2322: Type 'number' is not assignable to type 'string'.">value.a</error> = 12
value.<error descr="TS2339: Property 'b' does not exist on type '(MyObj & false) | (MyObj & true)'.
  Property 'b' does not exist on type 'MyObj & false'.">b</error> = 12
</script>