import Vue from "vue";
export const test = Vue.extend({
  methods:{
    show1(){
      alert("show1");
    },
    resetData(){
      return 0;
    }
  }
});