<template>
  <div>
    <v-card>
      <v-toolbar
        color="primary"
        dark
      >
        <v-icon class="mr-6">
          fa-folder-open
        </v-icon>
        <v-toolbar-title>
          {{ breadcrumbs }}
        </v-toolbar-title>
        <v-spacer/>
        <v-btn icon>
          <v-icon>
            fas fa-cloud-upload
          </v-icon>
        </v-btn>
      </v-toolbar>

      <v-data-table
        v-model="selectedItems"
        class="mt-2"
        calculate-widths
        fixed-header
        :footer-props="footerProps"
        :headers="headers"
        :items="tracks"
        :items-per-page="tableOptions.itemsPerPage"
        :loading="loading"
        loading-text="Loading tracks..."
        no-data-text="No tracks in this folder"
        :page="tableOptions.page"
        :server-items-length="serverCount"
        show-select
        @update:options="tableChange"
      >
        <template #item.duration="{ item }">
          {{ item.duration | hms }}
        </template>

        <template #item.createdAt="{ item }">
          {{ item.createdAt | localizedDateTime }}
        </template>

        <template #item.menu>
          <DotMenu :menu-items="menuItems"/>
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>

<script lang="ts">
import Vue from 'vue';

// async function getTracks(id:number , options: any): Promise<Track[]> {
//   return
// }

export default Vue.extend({
  name: 'TrackManager',
  props: {
    breadcrumbs: {
      type: String,
      default: 'Unfiled',
    },
    folderId: {
      type: Number,
      required: true,
    },
  },

  data: () => ({
    footerProps: Object.freeze({
      itemsPerPageOptions: [25, 50, 100],
    }),
    headers: Object.freeze([
      { text: '#', value: 'metadata.trackNum' },
      { text: '', value: 'play', sortable: false },
      { text: 'Name', value: 'name' },
      { text: 'Duration', value: 'duration' },
      { text: 'Uploaded', value: 'createdAt' },
      { text: '', value: 'menu' },
    ]),
    loading: true,
  }),

  watch: {
    async folderId() {
      await this.fetchTracks();
    },

    async tableOptions(options, oldOptions) {
      if (oldOptions.page !== options.page) {
        // await this.$vuetify.goTo(0);
      }
      await this.fetchTracks();
    },
  },

  async created() {
    await this.fetch<caret>Tracks();
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
  },
});
</script>
