<template>
  <b-alert show>Foo</b-alert>
  <b-container>
    <b-jumbotron header="BootstrapVue" lead="Bootstrap v4 Components for Vue.js 2">
      <p>For more information visit our website</p>
      <b-btn variant="primary" href="https://bootstrap-vue.js.org/">More Info</b-btn>
    </b-jumbotron>

    <b-form-group
      horizontal
      :label-cols="4"
      description="Let us know your name."
      label="Enter your name"
    >
      <b-form-input v-model.trim="<weak_warning descr="Unresolved variable or type names">names</weak_warning>"></b-form-input>
    </b-form-group>

    <b-alert variant="success" :show="<weak_warning descr="Unresolved variable or type showAlert">showAlert</weak_warning>">Hello {{ <weak_warning descr="Unresolved variable or type names">names</weak_warning> }}</b-alert>
  </b-container>
</template>
<script>
export default {
  name: 'app',
  components: {
  },
};
</script>