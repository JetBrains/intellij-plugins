<template>
    <div id="app" @click="show1">
        <img alt="Vue logo" src="./assets/logo.png" @click="show2()">
    </div>
</template>

<script lang="ts">

import mixins_ from "vue-typed-mixins";
import {test} from "./mixin";
import test2 from "./mixin2";

export default mixins_(test, test2).extend({
    name: 'app',
    data() {
        return {
            testData: this.resetData()
        }
    },
    methods: {
        show2() {
            alert("show2")
            var decoy = {show1() {}}
        },
        show3(){
            alert("show3");
            this.testData = this.resetData();
            this.show2();
        }
    }
});
</script>