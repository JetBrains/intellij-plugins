<template>
  <comp-with-two-mixins first-mixin-prop=1 second-mixin-prop=2 third-mixin-prop=3></comp-with-two-mixins>
</template>
<script>
import FirstMixin from './FirstMixin';
import SecondMixin from './SecondMixin';

export default {
  name: 'CompWithTwoMixins',
  mixins: [FirstMixin, SecondMixin]
}
</script>