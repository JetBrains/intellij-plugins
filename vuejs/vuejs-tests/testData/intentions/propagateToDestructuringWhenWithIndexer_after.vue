<template>
    <div class="hello">
        <h1>{{ msg }}</h1>
        <ul>
            <li v-for="({name, price}<caret>, i) in list"> {{ i }}:{{ name }} - {{ price }}</li>
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

