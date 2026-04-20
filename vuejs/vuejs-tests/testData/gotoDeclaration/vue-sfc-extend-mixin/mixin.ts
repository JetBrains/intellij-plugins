import Vue from 'vue'

export default Vue.extend({
    computed: {
        classes() {
            return {
                test3: 'test',
                test4: 'test'
            }
        }
    }
})