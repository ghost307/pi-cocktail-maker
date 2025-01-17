<template>
  <c-q-headlined-card
    :headline="headline"
    :card-class="cardClass"
    :icon="icon"
    :icon-background-class="iconBackgroundClass"
    :icon-class="iconClass"
    :hide-content-slot="tableRows.length === 0"
  >
    <template v-slot:content>
      <div class="row justify-center items-center q-pa-sm">
        <q-table
          :columns="columns"
          :rows="tableRows"
          hide-bottom
          hide-header
          dense
          flat
          grid
          class="full-width"
          :class="cardClass"
          card-container-class="justify-center"
        >
          <template v-slot:item="{ row }">
            <div
              class="q-pa-xs col-xs-12 col-sm-9 col-md-6 col-lg-3 grid-style-transition"
            >
              <c-make-cocktail-dialog-ingredient-group-replacements-card
                :class="{'bg-green-4': !!row.replacement, 'bg-deep-orange-3': !row.replacement }"
                @ReplacementUpdate="onReplacementUpdate(row.productionStep, row.ingredientGroup.id, $event)"
                :replacement-entry="row"
              />
            </div>
          </template>
        </q-table>
      </div>
    </template>
  </c-q-headlined-card>
</template>

<script>
import CQHeadlinedCard from 'components/CQHeadlinedCard'
import { mdiClose, mdiCheck } from '@quasar/extras/mdi-v5'
import CMakeCocktailDialogIngredientGroupReplacementsCard
  from 'components/CMakeCocktailDialogIngredientGroupReplacementsCard'
export default {
  name: 'CMakeCocktailDialogIngredientGroupReplacements',
  components: { CMakeCocktailDialogIngredientGroupReplacementsCard, CQHeadlinedCard },
  props: {
    ingredientGroupReplacements: Array,
    allIngredientGroupsReplaced: Boolean
  },
  emits: ['ReplacementUpdate'],
  data () {
    return {
      columns: [
        { name: 'productionStep', label: 'Production step', field: 'productionStep' },
        { name: 'ingredientGroup', label: 'Ingredient group', field: 'ingredientGroup' },
        { name: 'replacement', label: 'Replacement', field: 'replacement' }
      ]
    }
  },
  methods: {
    onReplacementUpdate (prodStepNr, toReplaceId, replacement) {
      this.$emit('ReplacementUpdate', { prodStepNr: prodStepNr - 1, toReplaceId, replacement })
    }
  },
  computed: {
    tableRows () {
      const data = []
      let prodStepNr = 0
      for (const prodStep of this.ingredientGroupReplacements) {
        prodStepNr++
        for (const ingredientGroupReplacement of prodStep) {
          data.push({
            productionStep: prodStepNr,
            ingredientGroup: ingredientGroupReplacement.ingredientGroup,
            replacement: ingredientGroupReplacement.selectedReplacement
          })
        }
      }
      return data
    },
    isFulfilled () {
      return this.allIngredientGroupsReplaced
    },
    cardClass () {
      return {
        'bg-warning': !this.isFulfilled,
        'bg-light-blue-3': this.isFulfilled
      }
    },
    headline () {
      if (this.isFulfilled) {
        return 'All ingredient-groups have been replaced with concrete ingredients!'
      } else {
        return 'The following ingredient-groups have to get real existing ingredients assigned:'
      }
    },
    iconClass () {
      return this.isFulfilled ? 'text-white' : 'text-negative'
    },
    iconBackgroundClass () {
      return this.isFulfilled ? 'bg-light-green-14' : 'bg-warning'
    },
    icon () {
      return this.isFulfilled ? mdiCheck : mdiClose
    }
  }
}
</script>

<style scoped>
</style>
