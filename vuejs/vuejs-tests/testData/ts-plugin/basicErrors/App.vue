<script lang="ts">
const unusedVar = 213;
const <error descr="TS2322: Type 'number' is not assignable to type 'string'.">abc</error>: string = 213;
</script>