<!-- DemoGrid component template -->
<template>
  <div>
    <table v-if="filteredData.length">
      <thead>
        <tr>
          <th
            v-for="key in columns"
            @click="sortBy(key)"
            :class="{ active: state.sortKey === key }"
          >
            {{ capitalize(key) }}
            <span
              class="arrow"
              :class="state.sortOrders[key] > 0 ? 'asc' : 'dsc'"
            >
            </span>
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="entry in filteredData">
          <td v-for="key in columns">
            {{ entry[key] }}
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else>No matches found.</p>
  </div>
</template>
<!-- DemoGrid component script -->
<script>
import { reactive, computed } from "@vue/composition-api";

const capitalize = str => str.charAt(0).toUpperCase() + str.slice(1);

export default {
  props: {
    data: Array,
    columns: Array,
    filterKey: String
  },
  setup(props) {
    const state = reactive({
      sortKey: "",
      sortOrders: props.columns.reduce((o, key) => ((o[key] = 1), o), {})
    });

    const filteredData = computed(() => {
      let { data, filterKey } = props;
      if (filterKey) {
        filterKey = filterKey.toLowerCase();
        data = data.filter(row => {
          return Object.keys(row).some(key => {
            return (
              String(row[key])
                .toLowerCase()
                .indexOf(filterKey) > -1
            );
          });
        });
      }
      const { sortKey } = state;
      if (sortKey) {
        const order = state.sortOrders[sortKey];
        data = data.slice().sort((a, b) => {
          a = a[sortKey];
          b = b[sortKey];
          return (a === b ? 0 : a > b ? 1 : -1) * order;
        });
      }
      return data;
    });

    function sortBy(key) {
      state.sortKey = key;
      state.sortOrders[key] *= -1;
    }

    return {
      state,
      filteredData,
      sortBy,
      capitalize
    };
  }
};
</script>
