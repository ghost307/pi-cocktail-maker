<template>
  <q-table
    :columns="columns"
    :rows="sortedPumpLayout"
    :pagination="{rowsPerPage: 0}"
    hide-bottom
    flat
  >
    <template v-slot:body-cell-currentIngredient="props">
      <q-td
        key="currentIngredient"
        :props="props"
      >
        <c-ingredient-selector
          :selected="props.row.currentIngredient"
          @update:selected="updatePumpIngredient(props.row.id, $event)"
          :disable="props.row.occupation !== 'NONE'"
          clearable
          dense
          filter-manual-ingredients
          filter-ingredient-groups
          :bg-color="markPump(props.row)? 'green-3':undefined"
          :no-input-options="missingAutomaticIngredients"
          :loading="loadingPumpIdsCurrentIngredient.includes(props.row.id, 0)"
        >
          <template
            v-slot:afterIngredientName="{scope}"
          >
            <q-item-label
              v-if="!!missingAutomaticIngredients.some(x => x.id === scope.opt.id)"
              caption
              class="text-green"
            >
              Required ingredient
            </q-item-label>
          </template>
        </c-ingredient-selector>
      </q-td>
    </template>
    <template v-slot:body-cell-fillingLevelInMl="props">
      <q-td
        key="fillingLevelInMl"
        :props="props"
      >
        <q-input
          :model-value="props.row.fillingLevelInMl"
          @update:model-value="updatePumpFillingLevel(props.row.id, Number($event))"
          debounce="500"
          :loading="loadingPumpIdsFillingLevel.includes(props.row.id, 0)"
          :disable="props.row.occupation !== 'NONE'"
          type="number"
          dense
          outlined
        >
          <template v-slot:append>
            ml
          </template>
        </q-input>
      </q-td>
    </template>
    <template v-slot:body-cell-pumpedUp="props">
      <q-td
        key="pumpedUp"
        class="q-pa-md q-gutter-x-sm"
        :props="props"
      >
        <c-pumped-up-icon-button
          :pump-id="props.row.id"
          :read-only="false"
        />
      </q-td>
    </template>
    <template v-slot:body-cell-actions="props">
      <q-td
        key="actions"
        class="q-pa-md q-gutter-x-sm"
        :props="props"
      >
        <c-pump-up-button
          v-if="isAllowReversePumping"
          :pump-id="props.row.id"
          :current-pump-direction-reversed="props.row.reversed"
          :pump-up-direction-reversed="true"
        />
        <c-pump-up-button
          :pump-id="props.row.id"
          :current-pump-direction-reversed="props.row.reversed"
          :pump-up-direction-reversed="false"
        />
        <c-pump-turn-on-off-button
          :pump-id="props.row.id"
        />
      </q-td>
    </template>
  </q-table>
</template>

<script>
import PumpService, { pumpDtoMapper } from 'src/services/pump.service'
import { mapGetters } from 'vuex'
import CIngredientSelector from 'components/CIngredientSelector'
import CPumpUpButton from 'components/CPumpUpButton'
import CPumpTurnOnOffButton from 'components/CPumpTurnOnOffButton'
import CPumpedUpIconButton from 'components/CPumpedUpIconButton'

export default {
  name: 'CMakeCocktailDialogPumpEditor',
  components: { CPumpedUpIconButton, CPumpTurnOnOffButton, CPumpUpButton, CIngredientSelector },
  props: {
    neededIngredients: {
      type: Array,
      required: true
    }
  },
  data () {
    return {
      columns: [
        { name: 'id', label: 'Nr', field: 'id', align: 'left' },
        { name: 'currentIngredient', label: 'Current Ingredient', field: 'currentIngredient', align: 'center' },
        { name: 'fillingLevelInMl', label: 'Remaining filling level', field: 'fillingLevelInMl', align: 'center' },
        { name: 'pumpedUp', label: 'Pumped Up', field: 'pumpedUp', align: 'center' },
        { name: 'actions', label: 'Actions', field: '', align: 'center' }
      ],
      loadingPumpIdsCurrentIngredient: [],
      loadingPumpIdsFillingLevel: []
    }
  },
  methods: {
    markPump (pump) {
      if (!pump.currentIngredient || !this.isIngredientNeeded(pump.currentIngredient.id)) {
        return false
      }
      return this.sortedPumpLayout.find(x => {
        return !!x.currentIngredient &&
          pump.currentIngredient.id === x.currentIngredient.id
      }) === pump
    },
    isIngredientNeeded (ingredientId) {
      return this.neededIngredients.some(x => x.id === ingredientId)
    },
    updatePumpFillingLevel (pumpId, newFillingLevel) {
      if ((!newFillingLevel && newFillingLevel !== 0) || newFillingLevel < 0) {
        return
      }
      this.loadingPumpIdsFillingLevel.push(pumpId)
      PumpService.patchPump(pumpId, pumpDtoMapper.toPumpPatchDto({
        fillingLevelInMl: newFillingLevel
      }))
        .finally(() => {
          const array = this.loadingPumpIdsFillingLevel
          array.splice(array.indexOf(pumpId), 1)
        })
    },
    updatePumpIngredient (pumpId, newIngredient) {
      this.loadingPumpIdsCurrentIngredient.push(pumpId)
      PumpService.patchPump(pumpId, pumpDtoMapper.toPumpPatchDto({
        currentIngredient: newIngredient
      }))
        .finally(() => {
          const array = this.loadingPumpIdsCurrentIngredient
          array.splice(array.indexOf(pumpId), 1)
        })
    }
  },
  computed: {
    ...mapGetters({
      getPumpLayout: 'pumpLayout/getLayout',
      getPumpIngredients: 'pumpLayout/getPumpIngredients',
      isAllowReversePumping: 'common/isAllowReversePumping'
    }),
    sortedPumpLayout () {
      const sorted = []
      sorted.push(...this.getPumpLayout)
      return sorted.sort((a, b) => a.id - b.id)
    },
    unassignedIngredients () {
      return this.neededIngredients.filter(x => !this.getPumpIngredients.some(y => x.id === y.id))
    },
    missingAutomaticIngredients () {
      return this.unassignedIngredients.filter(x => x.type === 'automated')
    }
  }
}
</script>

<style scoped>

</style>
