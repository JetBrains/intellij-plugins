<script>
export default {
  name: 'NewComponent',
  props: {
    showTools: {}
  }
}
</script>

<template>
  <header class="">
    <h1>TEXT <a class="t-btn" @click="showTools"><span></span></a></h1>
  </header>
</template>

<style lang="scss" rel="stylesheet/scss">
header {
  position: relative;
  width: 100%;
  height: 70px;
  z-index: 100;

  h1 {
    position: relative;
    width: 100%;
    max-width: 800px;
    margin: 0 auto;
    line-height: 70px;
    text-align: center;
    color: #fff;

    a.t-btn {
      position: absolute;
      right: 10px;
      top: 22px;
      width: 30px;
      height: 26px;
      cursor: pointer;
    }

    span, span:before, span:after {
      position: absolute;
      left: 0;
      width: 30px;
      height: 4px;
      content: '';
      background: #fff;
    }

    span {
      top: 11px;

      &:before {
        top: 0;
        transform: translateY(-7px);
        transition: all .3s;
      }

      &:after {
        transform: translateY(7px);
        transition: all .3s;
      }
    }
  }
}
</style>