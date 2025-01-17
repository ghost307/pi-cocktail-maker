package net.alex9849.cocktailmaker.payload.dto.pump;

import lombok.*;
import lombok.experimental.Accessors;
import net.alex9849.cocktailmaker.model.Pump;
import net.alex9849.cocktailmaker.payload.dto.recipe.ingredient.AutomatedIngredientDto;
import net.alex9849.cocktailmaker.service.PumpService;
import net.alex9849.cocktailmaker.service.PumpUpService;
import net.alex9849.cocktailmaker.utils.SpringUtility;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PumpDto {
    private interface Id { long getId(); }
    private interface TimePerClInMs { @NotNull @Min(1) int getTimePerClInMs(); }
    private interface TubeCapacityInMl { @NotNull @Min(1) double getTubeCapacityInMl(); }
    private interface BcmPin { @NotNull @Min(0) @Max(31) int getBcmPin(); }
    private interface FillingLevelInMl { @NotNull @Min(0) int getFillingLevelInMl(); }
    private interface CurrentIngredientId { Long getCurrentIngredientId();}
    private interface CurrentIngredient { AutomatedIngredientDto.Response.Detailed getCurrentIngredient();}
    private interface IsReversed { boolean isReversed(); }
    private interface Occupation { PumpService.PumpOccupation getOccupation(); }
    private interface IsPowerStateHigh { boolean isPowerStateHigh(); }
    private interface IsPumpedUp { boolean isPumpedUp(); }

    private interface PatchIsPumpedUp { Boolean getIsPumpedUp(); }
    private interface PatchRemoveIngredient { Boolean getIsRemoveIngredient(); }
    private interface PatchFillingLevelInMl { @Min(0) Integer getFillingLevelInMl(); }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Request {
        @Getter @Setter @EqualsAndHashCode
        public static class Create implements TimePerClInMs, TubeCapacityInMl, BcmPin, FillingLevelInMl, CurrentIngredientId, IsPowerStateHigh, IsPumpedUp {
            int timePerClInMs;
            double tubeCapacityInMl;
            int bcmPin;
            int fillingLevelInMl;
            Long currentIngredientId;
            boolean isPowerStateHigh;
            boolean isPumpedUp;
        }

        @Getter @Setter @EqualsAndHashCode
        public static class Patch implements PatchFillingLevelInMl, CurrentIngredientId, PatchIsPumpedUp, PatchRemoveIngredient {
            Integer fillingLevelInMl;
            Long currentIngredientId;
            Boolean isPumpedUp;
            Boolean isRemoveIngredient;
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Response {
        @Getter @Setter @EqualsAndHashCode
        public static class Detailed implements Id, TimePerClInMs, TubeCapacityInMl, BcmPin, FillingLevelInMl,
                CurrentIngredient, Occupation, IsReversed, IsPowerStateHigh, IsPumpedUp {
            long id;
            int timePerClInMs;
            double tubeCapacityInMl;
            int bcmPin;
            int fillingLevelInMl;
            AutomatedIngredientDto.Response.Detailed currentIngredient;
            PumpService.PumpOccupation occupation;
            boolean isReversed;
            boolean isPowerStateHigh;
            boolean isPumpedUp;

            public Detailed(Pump pump) {
                BeanUtils.copyProperties(pump, this);
                PumpService pService = SpringUtility.getBean(PumpService.class);
                PumpUpService puService = SpringUtility.getBean(PumpUpService.class);
                this.occupation = pService.getPumpOccupation(pump);
                this.isReversed = puService.isPumpDirectionReversed();
                if(pump.getCurrentIngredient() != null) {
                    this.currentIngredient = new AutomatedIngredientDto.Response.Detailed(pump.getCurrentIngredient());
                }
            }
        }
    }


}
