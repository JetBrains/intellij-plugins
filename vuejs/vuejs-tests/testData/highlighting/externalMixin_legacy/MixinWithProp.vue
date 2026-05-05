<script>
export default {
  props: {
    mixinProp:  {
      type: String
    },
    requiredMixinProp: {
      required: true
    }
  },
  methods: {
    helloFromMixin: function () {
      console.log('hello from mixin!')
    }
  }
}
</script>