export const doPumpsHaveAllIngredientsForRecipe = (state) => {
  return (recipe) => {
    let pumpIngredientIds = new Set();
    for(let pump of state.pumpLayout) {
      if(pump.currentIngredient) {
        pumpIngredientIds.add(pump.currentIngredient.id)
      }
    }
    let success = true;
    for(let productionSteps of recipe.productionSteps) {
      if (productionSteps.type !== 'addIngredients') {
        continue
      }
      for(let ingredientStep of productionSteps.stepIngredients) {
        success &= pumpIngredientIds.has(ingredientStep.ingredient.id)
      }
    }
    return success;
  }
};

export const getPumpIngredients = (state) => {
  let pumpIngredients = [];
  for(let pump of state.pumpLayout) {
    if(pump.currentIngredient && !pumpIngredients.some(x => x.id === pump.currentIngredient.id)) {
      pumpIngredients.push(pump.currentIngredient);
    }
  }
  return pumpIngredients;
};



export const isCleaning = (state) => {
  return (pumpId) => {
    for(let pump of state.pumpLayout) {
      if(pump.id === pumpId) {
        return pump.cleaning;
      }
    }
    return false;
  }
};

export const getLayout = (state) => state.pumpLayout;
