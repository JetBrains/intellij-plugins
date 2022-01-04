<script>
export default {
  name: 'MyFieldMixin',

  model: {
    prop: 'myProp',
    event: 'change',
  },

  props: {
    myProp: {
      type: String,
      required: true,
    },
  },
}
</script>