<script>
export default {
  props: {
    foo: { type: String, required: true }
  },
  emits:["update:foo"]
}
</script>