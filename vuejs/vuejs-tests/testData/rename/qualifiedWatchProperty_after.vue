
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
    doubleAge()
    {
      return {<caret>newName: 12, b: 12};
    }
  },
  watch: {
    "doubleAge.newName" () {

    }
  }
};
</script>