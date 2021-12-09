Vue.component("main-component", {
  template: `
            <div class="main-component">
                <div>{{value}}</div>
                <button v-on:click="increment">Increment</button>
            </div>
        `,
  data() {
    return {
      value: 0
    }
  },
  methods: {
    increment() {
      this.value++;
    }
  }
});