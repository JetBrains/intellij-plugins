
<script>
export default {
  name: 'DemoWatch',
  data() {
    return {
      question: '',
      answer: 'Questions usually contain a question mark. ;-)'
    }
  },
  computed: {
    newName()
    {
      return {a: 12, b: 12};
    }
  },
  watch: {
    newName() {

    }
  }
};
</script>