<script lang="ts">
import Vue from 'vue';

export default Vue.extend({
  name: 'TrackManager',

  watch: {
    async folderId() {
      await this.fetchTracks();
    },

    async tableOptions(options, oldOptions) {
      if (oldOptions.page !== options.page) {
        // await this.$vuetify.goTo(0);
      }
      await this.fetchTracks/*watch*/();
    },
  },

  computed: {
    foo_comp: function() {
      return await this.fetchTracks/*computed*/();
    }
  }

  async created() {
    await this.fetchTracks/*base*/();
  },

  methods: {
    async fetchTracks() {
      this.loading = true;

      // const { page, itemsPerPage } = this.tableOptions;

      try {
        // this.tracks = await getTracks(this.folderId, {
        //   limit: itemsPerPage,
        //   offset: (page - 1) * itemsPerPage,
        // });
      } catch (e) {
        // this.$reportError(e, { while: 'loading tracks' });
      } finally {
        this.loading = false;
      }
    },
    async foo() {
      this.fetchTracks/*methods*/()
    }
  },
});
</script>
