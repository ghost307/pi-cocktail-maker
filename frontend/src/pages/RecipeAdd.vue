<template>
  <q-page class="page-content" padding>
    <h5>Add Recipe</h5>
    <q-banner v-if="error !== ''" rounded dense class="text-white bg-red-5" style="margin-bottom: 20px">
      {{ error }}
    </q-banner>
    <q-card
      flat
    >
      <recipe-editor-form
        v-model="addRecipe"
        @valid="isValid = true"
        @invalid="isValid = false"
      >
        <template v-slot:below>
          <div class="q-pa-md q-gutter-sm">
            <q-btn
              style="width: 100px"
              color="negative"
              label="Abort"
              no-caps
              :to="{name: 'publicrecipes'}"
            />
            <q-btn
              type="submit"
              style="width: 100px"
              color="positive"
              label="Create"
              no-caps
              :disable="loading || !isValid"
              :loading="loading"
              @click="createRecipe"
            />
          </div>
        </template>
      </recipe-editor-form>
    </q-card>
  </q-page>
</template>

<script>
import RecipeEditorForm from '../components/RecipeEditorForm'
import RecipeService, { recipeDtoMapper } from '../services/recipe.service'
import Recipe from '../models/Recipe'
import { mapGetters } from 'vuex'

export default {
  name: 'RecipeAdd',
  components: { RecipeEditorForm },
  data () {
    return {
      addRecipe: {
        recipe: new Recipe(0, '', {}, '', [], [], 250),
        image: null
      },
      error: '',
      isValid: false,
      loading: false
    }
  },
  methods: {
    createRecipe () {
      this.loading = true
      this.addRecipe.recipe.ownerId = this.user.id
      const dto = recipeDtoMapper.toRecipeCreateDto(this.addRecipe.recipe)
      RecipeService.createRecipe(dto, this.addRecipe.image)
        .then((recipe) => {
          this.loading = false
          this.$q.notify({
            type: 'positive',
            message: 'Recipe created successfully'
          })
          this.$router.push({ name: 'recipedetails', params: { id: recipe.id } })
        }, error => {
          this.loading = false
          this.error = error.response.data.message
          this.$q.notify({
            type: 'negative',
            message: 'Couldn\'t update recipe. ' + error.response.data.message
          })
        })
    }
  },
  computed: {
    ...mapGetters({
      user: 'auth/getUser'
    })
  }
}
</script>

<style scoped>

</style>
