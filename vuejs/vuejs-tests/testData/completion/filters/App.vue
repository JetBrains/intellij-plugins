<template>
    <div id="app" :title="'foo' | <caret>">
    </div>

</template>

<script>
    import Vue from 'vue';
    import HelloWorld from './components/HelloWorld.vue';

    export default Vue.extend({
        name: 'app',
        components: {
            HW:  HelloWorld,
        },
        filters: {
            localFilter: function (arg1, arg2, arg3) {
                return true
            }
        }
    });
</script>
