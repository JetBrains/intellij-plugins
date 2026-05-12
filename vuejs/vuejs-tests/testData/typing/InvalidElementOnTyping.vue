<template>
   <view class="page">
      <div class="item" v-for="(item, index) in dataList" :key="item.id || index">
      </div>
   </view>
</template>

<script>
   const ZPMixin = {
      methods: {
         isPagingRefNotFound() {
            return !this.$refs.paging;
         },
      },
   };

   export default {
      mixins: [ZPMixin],
      data() {<caret>
         return {
            dataList: [],
            tabList: [
               {
                  name: '已确认',
                  code: 5,
               },
            ],
         };
      },
      computed: {},
      methods: {
         queryList(pageNo, pageSize) {
         },
      },
      watch: {},

      // 页面周期函数--监听页面加载
      onLoad() {
      }
   };
</script>