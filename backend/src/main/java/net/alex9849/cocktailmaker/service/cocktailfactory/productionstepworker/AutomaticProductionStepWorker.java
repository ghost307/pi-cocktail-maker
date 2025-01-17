package net.alex9849.cocktailmaker.service.cocktailfactory.productionstepworker;

import net.alex9849.cocktailmaker.iface.IGpioController;
import net.alex9849.cocktailmaker.iface.IGpioPin;
import net.alex9849.cocktailmaker.model.Pump;
import net.alex9849.cocktailmaker.model.recipe.ingredient.AutomatedIngredient;
import net.alex9849.cocktailmaker.model.recipe.productionstep.ProductionStep;
import net.alex9849.cocktailmaker.model.recipe.productionstep.ProductionStepIngredient;
import net.alex9849.cocktailmaker.service.cocktailfactory.PumpPhase;
import net.alex9849.cocktailmaker.service.cocktailfactory.PumpTimingStepCalculator;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AutomaticProductionStepWorker extends AbstractPumpingProductionStepWorker {
    /**
     *
     * @param pumps pumps is an output parameter! The attribute fillingLevelInMl will be decreased according to the recipe
     */
    public AutomaticProductionStepWorker(Set<Pump> pumps, List<ProductionStepIngredient> productionStepInstructions, int minimalPumpTime, int minimalBreakTime) {
        Map<Long, List<Pump>> pumpsByIngredientId = pumps.stream()
                .filter(x -> x.getCurrentIngredient() != null)
                .collect(Collectors
                .groupingBy(x -> x.getCurrentIngredient().getId()));

        Map<ProductionStepIngredient, List<Pump>> matchingPumpByProductionStepIngredient = new HashMap<>();
        for (ProductionStepIngredient instruction : productionStepInstructions) {
            if (!(instruction.getIngredient() instanceof AutomatedIngredient)) {
                throw new IllegalArgumentException("Can't automatically fulfill productionstep. One or more given ingredients can only be added manually!");
            }
            AutomatedIngredient currAutoIngredient = (AutomatedIngredient) instruction.getIngredient();
            List<Pump> pumpsWithIngredient = pumpsByIngredientId.get(currAutoIngredient.getId());
            if (pumpsWithIngredient == null) {
                throw new IllegalArgumentException("Can't automatically fulfill productionstep. One or more required ingredients are not assigned to pumps!");
            }
            matchingPumpByProductionStepIngredient.put(instruction, pumpsWithIngredient);
        }
        PumpTimingStepCalculator pumpTimingStepCalculator = new PumpTimingStepCalculator(matchingPumpByProductionStepIngredient,
                minimalPumpTime, minimalBreakTime);

        this.setPumpPhases(pumpTimingStepCalculator.getPumpPhases());
    }
}
