<template>
  <div class="hello">
    <h1>{{ myMessage }}</h1>
    <div :user-id="userId">User {{userId.id}}</div>
  </div>
</template>

<script>
export default {
  name: 'HelloWorld',
  props: {
    myMessage: String,
    userId: {
      type: Object,
      required: true,
      default() {
        return { id: 100 }
      }
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h3 {
  margin: 40px 0 0;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
</style>
