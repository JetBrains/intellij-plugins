<template>
  <mixins-in-completion <caret>></mixins-in-completion>
</template>
<script>
import FirstMixin from './FirstMixin';
import SecondMixin from './SecondMixin';

export default {
  name: 'mixins-in-completion',
  mixins: [FirstMixin, SecondMixin]
}
</script>