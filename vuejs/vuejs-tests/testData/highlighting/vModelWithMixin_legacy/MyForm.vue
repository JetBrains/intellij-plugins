<template>
  <div>
    <MyField v-model="myProp" />
    <<warning descr="Element MyField doesn't have required attribute my-prop">MyField</warning>/>
  </div>
</template>

<script>
import MyField from './MyField'

export default {
  name: 'MyForm',

  components: {
    MyField,
  },

  data: () => ({
    myProp: '',
  }),
}
</script>
