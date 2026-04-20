<template>
  <div :user-id="userId">User</div>
  <div :user-id="userId2">User 2</div>
</template>
<script>
  import props from './props';

  export default {
    name: 'TestComponent',
    props
  };
</script>
