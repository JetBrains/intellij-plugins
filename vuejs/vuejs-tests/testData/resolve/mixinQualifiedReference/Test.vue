<template>
    <div>
        <button v-on:click="cl<caret>icked('you just clicked on button  1')">
            Button 1
        </button>
    </div>
</template>
<script>
    import {Mixins} from "./clickMixin";

    export default {
    name: "Test",
    mixins: [
        Mixins.parameter
    ]
};
</script>
