<template lang="html"> <div id="app">  <img alt="Vue logo" src="./assets/logo.png">
         <HelloWorld msg="Welcome to Your Vue.js + TypeScript App"/>
  </div>
</template>
<template> <div id="app2">  <img alt="Vue logo"
                                 src="./assets/logo.png">
  <HelloWorld msg="Welcome to Your Vue.js + TypeScript App"/>
</div>
</template>
<template lang="pug">
 body
   h1.someClass Jade - node template engine
        #container
            |dfefef
            | erftrgfr
            |sdfverdgr5g
</template>
<script>
import         Vue from 'vue';

export default Vue.extend({
    name: 'app',  components: {
    HelloWorld},
    foo: {

            }
   });
</script>

<style>
    #app {
       font-family: 'Avenir', Helvetica, Arial, sans-serif;
  text-align: center;
 margin-top: 60px;}
</style>
<style lang="scss">
#main p {
      color: #00ff00;width: 97%;

     .redbox {
background-color: #ff0000;
       color: #000000;
  }
}
</style>