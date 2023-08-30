<script>
export default {
  props: {
    /** @type {import('vue').PropType<{strVar: string | string[]}>} */
    prop1: Object,
  },
  data() {
    return {
      prop1<caret>Val: this.prop1.strVar,
    }
  },
}
</script>