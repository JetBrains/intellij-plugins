import template from "./template.html"
import Vue from "vue"

Vue.component("Bar", {
  template,
  props: ["foo"]
})
