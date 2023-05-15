const app = Vue.createApp({
    data() {
        return {
            counter: 0

        };
    },
    methods: {
        add(num){
            this.counter += num;
        }
    }
})
app.mount("main");
