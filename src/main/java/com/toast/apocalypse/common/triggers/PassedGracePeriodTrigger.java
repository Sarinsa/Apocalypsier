package com.toast.apocalypse.common.triggers;

import com.google.gson.JsonObject;
import com.toast.apocalypse.common.core.Apocalypse;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class PassedGracePeriodTrigger extends SimpleCriterionTrigger<PassedGracePeriodTrigger.TriggerInstance> {

    private static final ResourceLocation ID = Apocalypse.resourceLoc("passed_grace_period");

    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public PassedGracePeriodTrigger.TriggerInstance createInstance(JsonObject jsonObject, ContextAwarePredicate predicate, DeserializationContext context) {
        return new PassedGracePeriodTrigger.TriggerInstance(predicate);
    }

    public void trigger(ServerPlayer player, long currentDifficulty) {
        this.trigger(player, (instance) -> instance.matches(currentDifficulty));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        public TriggerInstance(ContextAwarePredicate predicate) {
            super(PassedGracePeriodTrigger.ID, predicate);
        }

        public static PassedGracePeriodTrigger.TriggerInstance gracePeriodPassed() {
            return new PassedGracePeriodTrigger.TriggerInstance(ContextAwarePredicate.ANY);
        }

        public boolean matches(long currentDifficulty) {
            return currentDifficulty >= 0L;
        }

        public JsonObject serializeToJson(SerializationContext serializer) {
            return super.serializeToJson(serializer);
        }
    }
}
