package com.toast.apocalypse.common.triggers;

import com.google.gson.JsonObject;
import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.core.register.ApocalypseEntities;
import com.toast.apocalypse.common.entity.living.Grump;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;


public class TamedGrumpTrigger extends SimpleCriterionTrigger<TamedGrumpTrigger.TriggerInstance> {

    private static final ResourceLocation ID = Apocalypse.resourceLoc("tame_grump");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public TamedGrumpTrigger.TriggerInstance createInstance(JsonObject jsonObject, ContextAwarePredicate predicate, DeserializationContext context) {
        ContextAwarePredicate predicate2 = EntityPredicate.fromJson(jsonObject, "entity", context);
        return new TamedGrumpTrigger.TriggerInstance(predicate, predicate2);
    }

    public void trigger(ServerPlayer player, Grump grump) {
        LootContext lootContext = EntityPredicate.createContext(player, grump);
        this.trigger(player, (instance) -> instance.matches(lootContext));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ContextAwarePredicate entity;

        public TriggerInstance(ContextAwarePredicate predicate1, ContextAwarePredicate predicate2) {
            super(TamedGrumpTrigger.ID, predicate1);
            entity = predicate2;
        }

        public static TamedGrumpTrigger.TriggerInstance tamedGrump() {
            return new TamedGrumpTrigger.TriggerInstance(ContextAwarePredicate.ANY, EntityPredicate.wrap(EntityPredicate.Builder.entity().of(ApocalypseEntities.GRUMP.get()).build()));
        }

        public boolean matches(LootContext lootContext) {
            return entity.matches(lootContext);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject jsonObject = super.serializeToJson(context);
            jsonObject.add("entity", entity.toJson(context));
            return jsonObject;
        }
    }
}
