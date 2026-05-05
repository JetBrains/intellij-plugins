<template>
  <div class="hello">
    <h1>{{ msg }}</h1>
    <ul>
      <li v-for="({name, price}, i) in list"> {{ i }}:{{ name }} - {{ price }}</li>
      <li v-for="{name, price} in list"> {{ 111 }}:{{ name }} - {{ price }}</li>
      <li v-for="(x, k, i) in list"> {{ k + i }}:{{ x.name }} - {{ x.price }}</li>
      <li v-for="(x, k, i<error descr=") expected"> </error>in list"> {{ k + i }}:{{ x.name }} - {{ x.price }}</li>
      <li v-for="(x, k, i<error descr=") expected">,</error> j) in list"> {{ k + i }}:{{ x.name }} - {{ x.price }}</li>
    </ul>
  </div>
</template>
<script>
export default {
  name: 'HelloWorld',
  data: function () {
    return {
      list: [
        {name: 'Product 1', price: 300},
        {name: 'Product 2', price: 100},
        {name: 'Product 3', price: 200}
      ]
    }
  },
  props: {
    msg: String
  }
};
</script>