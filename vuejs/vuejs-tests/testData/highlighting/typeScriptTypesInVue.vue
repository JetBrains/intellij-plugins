<script lang="ts">
interface Test {
  hello: string
}

const <warning descr="Unused constant test">test</warning>: Test = {
  hello: "not working",
}
</script>