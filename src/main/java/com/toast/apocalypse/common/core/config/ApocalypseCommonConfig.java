package com.toast.apocalypse.common.core.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.toast.apocalypse.common.core.config.util.ConfigGameObjList;
import com.toast.apocalypse.common.core.register.ApocalypseEntities;
import com.toast.apocalypse.common.core.register.ApocalypseItems;
import com.toast.apocalypse.common.entity.living.*;
import com.toast.apocalypse.common.util.RLHelper;
import com.toast.apocalypse.common.util.References;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

/**
 * Apocalypse's common config, synced between client and server.
 */
public class ApocalypseCommonConfig {

    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        Pair<Common, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = commonPair.getLeft();
        COMMON_SPEC = commonPair.getRight();
    }

    @SuppressWarnings("ConstantConditions")
    public static final class Common {

        // Cool lists
        private static final List<? extends String> DEFAULT_PENALTY_DIMENSIONS = Arrays.asList(
                BuiltinDimensionTypes.NETHER.location().toString(),
                BuiltinDimensionTypes.END.location().toString());

        private static final List<? extends String> DEFAULT_DESTROYER_PROOF_BLOCKS = Arrays.asList(
                ForgeRegistries.BLOCKS.getKey(Blocks.BARRIER).toString(),
                ForgeRegistries.BLOCKS.getKey(Blocks.BEDROCK).toString()
        );

        private static final List<? extends String> DEFAULT_CAN_HAVE_WEAPONS = Arrays.asList(
                ForgeRegistries.ENTITY_TYPES.getKey(EntityType.ZOMBIE).toString(),
                ForgeRegistries.ENTITY_TYPES.getKey(EntityType.ZOMBIE_VILLAGER).toString(),
                ForgeRegistries.ENTITY_TYPES.getKey(EntityType.DROWNED).toString(),
                ForgeRegistries.ENTITY_TYPES.getKey(EntityType.HUSK).toString(),
                ForgeRegistries.ENTITY_TYPES.getKey(EntityType.WITHER_SKELETON).toString(),
                ForgeRegistries.ENTITY_TYPES.getKey(EntityType.PIGLIN).toString(),
                ForgeRegistries.ENTITY_TYPES.getKey(EntityType.ZOMBIFIED_PIGLIN).toString(),
                ForgeRegistries.ENTITY_TYPES.getKey(EntityType.PIGLIN_BRUTE).toString(),
                ForgeRegistries.ENTITY_TYPES.getKey(EntityType.VINDICATOR).toString()
        );

        private static final List<? extends String> DEFAULT_CAN_HAVE_ARMOR = Arrays.asList(
                ForgeRegistries.ENTITY_TYPES.getKey(EntityType.ZOMBIE).toString(),
                ForgeRegistries.ENTITY_TYPES.getKey(EntityType.ZOMBIE_VILLAGER).toString(),
                ForgeRegistries.ENTITY_TYPES.getKey(EntityType.DROWNED).toString(),
                ForgeRegistries.ENTITY_TYPES.getKey(EntityType.HUSK).toString(),
                ForgeRegistries.ENTITY_TYPES.getKey(EntityType.WITHER_SKELETON).toString(),
                ForgeRegistries.ENTITY_TYPES.getKey(EntityType.PIGLIN).toString(),
                ForgeRegistries.ENTITY_TYPES.getKey(EntityType.ZOMBIFIED_PIGLIN).toString(),
                ForgeRegistries.ENTITY_TYPES.getKey(EntityType.PIGLIN_BRUTE).toString()
        );


        // Version check
        private final ForgeConfigSpec.BooleanValue sendUpdateMessage;


        // Rain
        private final ForgeConfigSpec.DoubleValue acidRainChance;
        private final ForgeConfigSpec.IntValue rainTickRate;
        private final ForgeConfigSpec.IntValue rainDamage;
        private final ForgeConfigSpec.BooleanValue rainDamageEnabled;


        // Difficulty
        private final ForgeConfigSpec.BooleanValue multiplayerDifficultyScaling;
        private final ForgeConfigSpec.DoubleValue difficultyMultiplayerRateMult;
        private final ForgeConfigSpec.DoubleValue sleepPenalty;
        private final ForgeConfigSpec.ConfigValue<List<? extends String>> dimensionsPenaltyList;
        private final ForgeConfigSpec.DoubleValue dimensionPenalty;
        private final ForgeConfigSpec.BooleanValue averageGroupDifficulty;
        private final ForgeConfigSpec.ConfigValue<CommentedConfig> mobDifficulties;

        // Full moon stuff
        private final ForgeConfigSpec.BooleanValue despawnMobsOnDeath;
        private final ForgeConfigSpec.DoubleValue difficultyUntilNextIncrease;
        private final HashMap<Class<? extends IFullMoonMob>, ForgeConfigSpec.LongValue> moonMobStartDifficulties = new HashMap<>();
        private final HashMap<Class<? extends IFullMoonMob>, ForgeConfigSpec.DoubleValue> moonMobAdditionalCount = new HashMap<>();
        private final HashMap<Class<? extends IFullMoonMob>, ForgeConfigSpec.IntValue> moonMobMinCount = new HashMap<>();
        private final HashMap<Class<? extends IFullMoonMob>, ForgeConfigSpec.IntValue> moonMobCountCap = new HashMap<>();


        // Attributes
        private final ForgeConfigSpec.BooleanValue enemiesOnly;

        private final ForgeConfigSpec.ConfigValue<List<? extends String>> healthBlacklist;
        private final ForgeConfigSpec.DoubleValue healthLunarFlatBonus;
        private final ForgeConfigSpec.DoubleValue healthLunarMultBonus;
        private final ForgeConfigSpec.DoubleValue healthTimeSpan;
        private final ForgeConfigSpec.DoubleValue healthFlatBonus;
        private final ForgeConfigSpec.DoubleValue healthFlatBonusMax;
        private final ForgeConfigSpec.DoubleValue healthMultBonus;
        private final ForgeConfigSpec.DoubleValue healthMultBonusMax;

        private final ForgeConfigSpec.ConfigValue<List<? extends String>> damageBlacklist;
        private final ForgeConfigSpec.DoubleValue damageLunarFlatBonus;
        private final ForgeConfigSpec.DoubleValue damageLunarMultBonus;
        private final ForgeConfigSpec.DoubleValue damageTimeSpan;
        private final ForgeConfigSpec.DoubleValue damageFlatBonus;
        private final ForgeConfigSpec.DoubleValue damageFlatBonusMax;
        private final ForgeConfigSpec.DoubleValue damageMultBonus;
        private final ForgeConfigSpec.DoubleValue damageMultBonusMax;

        private final ForgeConfigSpec.ConfigValue<List<? extends String>> speedBlacklist;
        private final ForgeConfigSpec.DoubleValue speedLunarMultBonus;
        private final ForgeConfigSpec.DoubleValue speedTimeSpan;
        private final ForgeConfigSpec.DoubleValue speedMultBonus;
        private final ForgeConfigSpec.DoubleValue speedMultBonusMax;

        private final ForgeConfigSpec.ConfigValue<List<? extends String>> knockbackResBlacklist;
        private final ForgeConfigSpec.DoubleValue knockbackResLunarFlatBonus;
        private final ForgeConfigSpec.DoubleValue knockbackResTimeSpan;
        private final ForgeConfigSpec.DoubleValue knockbackResFlatBonus;
        private final ForgeConfigSpec.DoubleValue knockbackResFlatBonusMax;


        // Equipment
        private final ForgeConfigSpec.ConfigValue<List<? extends String>> canHaveWeapons;
        private final ForgeConfigSpec.ConfigValue<CommentedConfig> weaponLists;
        private final ForgeConfigSpec.DoubleValue weaponsTimeSpan;
        private final ForgeConfigSpec.DoubleValue weaponsChance;
        private final ForgeConfigSpec.DoubleValue weaponsLunarChance;
        private final ForgeConfigSpec.DoubleValue weaponsMaxChance;
        private final ForgeConfigSpec.BooleanValue useCurrentWeaponTierOnly;

        private final ForgeConfigSpec.ConfigValue<List<? extends String>> canHaveArmor;
        private final ForgeConfigSpec.ConfigValue<CommentedConfig> armorList;
        private final ForgeConfigSpec.DoubleValue armorTimeSpan;
        private final ForgeConfigSpec.DoubleValue armorChance;
        private final ForgeConfigSpec.DoubleValue armorLunarChance;
        private final ForgeConfigSpec.DoubleValue armorMaxChance;
        private final ForgeConfigSpec.BooleanValue useCurrentArmorTierOnly;


        // Potion effect
        private final ForgeConfigSpec.ConfigValue<CommentedConfig> potionEffectMap;
        private final ForgeConfigSpec.DoubleValue potionEffectTimeSpan;
        private final ForgeConfigSpec.DoubleValue potionEffectChance;
        private final ForgeConfigSpec.DoubleValue potionEffectLunarChance;
        private final ForgeConfigSpec.DoubleValue potionEffectMaxChance;

        // Traps
        private final ForgeConfigSpec.IntValue ghostFreezeTrapRange;
        private final ForgeConfigSpec.IntValue armorShatterTrapRange;

        // Misc
        private final ForgeConfigSpec.BooleanValue destroyerTargetRespawnPos;
        private final ForgeConfigSpec.ConfigValue<List<? extends String>> destroyerProofBlocks;
        private final ForgeConfigSpec.DoubleValue grumpBucketHelmetChance;
        private final ForgeConfigSpec.IntValue seekerExplosionPower;
        private final ForgeConfigSpec.IntValue destroyerExplosionPower;
        private final ForgeConfigSpec.IntValue destroyerEquipmentDamage;
        private final ForgeConfigSpec.BooleanValue pauseDaylightCycle;



        private Common(ForgeConfigSpec.Builder configBuilder) {
            configBuilder.push("version_check");
            this.sendUpdateMessage = configBuilder.comment("If enabled, the player will receive an in-game message when a new mod update is released.")
                    .define("sendUpdateMessage", true);
            configBuilder.pop();

            configBuilder.push("acid_rain");
            this.acidRainChance = configBuilder.comment("The chance of triggering an Acid Rain event when it starts raining. 1 = 100% chance, 0.5 = 50% etc. Defaults to 0.33 (roughly 1/3 chance).")
                    .defineInRange("acidRainChance", 0.33D, 0.0D, 1.0D);

            this.rainTickRate = configBuilder.comment("Determines the interval in which acid rain damage should be dealt in seconds. A value of 2 will inflict acid rain damage on players every 2 seconds.")
                    .defineInRange("rainTickRate", 3, 1, 1000);

            this.rainDamage = configBuilder.comment("The amount of damage that should be dealt to players on acid rain tick.")
                    .defineInRange("rainDamage", 1, 1, 10000);

            this.rainDamageEnabled = configBuilder.comment("Set to false to disable acid rain damage, or to true to turn it on.")
                    .define("enableRainDamage", true);
            configBuilder.pop();

            configBuilder.push("difficulty");
            this.multiplayerDifficultyScaling = configBuilder.comment("If enabled, world difficulty will increased by the configured multiplier")
                    .define("multiplayerDifficultyScaling", true);

            this.difficultyMultiplayerRateMult = configBuilder.comment("Only relevant if multiplayer difficulty scaling is enabled. For example, a value of 0.05 will apply an additional +5% difficulty increment per online player (If only one player is online this multiplier will not be active)")
                    .defineInRange("difficultyMultiplayerRateMult", 0.0D, 0.0D, 10.0D);

            this.sleepPenalty = configBuilder.comment("Sets the multiplier used to increase world difficulty when players sleep through a night or thunderstorm.")
                    .defineInRange("sleepPenalty", 2.0D, 1.0D, 1000.0D);

            this.dimensionsPenaltyList = configBuilder.comment("A list of dimensions that should give difficulty penalty. Difficulty increases more in these dimensions.")
                    .defineListAllowEmpty(split("dimensionPenaltyList"), () -> DEFAULT_PENALTY_DIMENSIONS, isResourceLocation());

            this.dimensionPenalty = configBuilder.comment("The difficulty rate multiplier used when a player enters a dimension with difficulty penalty.")
                    .defineInRange("dimensionPenalty", 0.5D, 0.0D, 1000.0D);

            this.averageGroupDifficulty = configBuilder.comment("(Currently unused) If enabled, players that are close to each other will have the average of their difficulty added together used instead of the nearby player with the highest difficulty.")
                    .define("averageGroupDifficulty", false);

            this.mobDifficulties = configBuilder.comment("A list of mobs that can only start spawning naturally when the nearest player has reached a certain difficulty")
                    .define("mobDifficulties", createDefaultMobDifficulties());
            configBuilder.pop();

            configBuilder.comment("This section revolves around everything related to the full moon sieges.");
            configBuilder.push("full_moon");

            this.despawnMobsOnDeath = configBuilder.comment("If enabled, all full moon mobs spawned for a specific player will despawn if the player dies, which can help prevent spawn killing.")
                    .define("despawnMobsOnDeath", true);

            this.difficultyUntilNextIncrease = configBuilder.comment("How many levels of difficulty must pass before the additional full moon mob counts increases. For example, a value of 30.5 will increase the number of full moon mobs spawning during sieges for every 30.5 levels of difficulty passed.")
                    .defineInRange("difficultyUntilNextIncrease", 40.0D, 0.1D, 100000.0D);

            configBuilder.comment("The difficulty at which a specific type of full moon mob can start to spawn during sieges (It might be smart to let at least one type spawn at 0).");
            configBuilder.push("spawn_start_difficulties");
            createStartDifficulty(Breecher.class, "breecher", 5, configBuilder);
            createStartDifficulty(Ghost.class, "ghost", 45, configBuilder);
            createStartDifficulty(Destroyer.class, "destroyer", 100, configBuilder);
            createStartDifficulty(Seeker.class, "seeker", 70, configBuilder);
            createStartDifficulty(Grump.class, "grump", 20, configBuilder);
            configBuilder.pop();

            configBuilder.comment("The additional amount of a specific full moon mob that can spawn during a full moon siege. Increases with difficulty.");
            configBuilder.push("additional_spawn_count");
            createMobAdditionalCount(Breecher.class, "breecher", 2.0D, configBuilder);
            createMobAdditionalCount(Ghost.class, "ghost", 6.0D, configBuilder);
            createMobAdditionalCount(Destroyer.class, "destroyer", 1.0D, configBuilder);
            createMobAdditionalCount(Seeker.class, "seeker", 1.0D, configBuilder);
            createMobAdditionalCount(Grump.class, "grump", 2.5D, configBuilder);
            configBuilder.pop();

            configBuilder.comment("The minimum amount of a specific full moon mob that can spawn during a full moon siege.");
            configBuilder.push("min_spawn_count");
            createMobMinCount(Breecher.class, "breecher", 4, configBuilder);
            createMobMinCount(Ghost.class, "ghost", 4, configBuilder);
            createMobMinCount(Destroyer.class, "destroyer", 1, configBuilder);
            createMobMinCount(Seeker.class, "seeker", 1, configBuilder);
            createMobMinCount(Grump.class, "grump", 2, configBuilder);
            configBuilder.pop();

            configBuilder.comment("The maximum amount of a specific full moon mob that can spawn during a full moon siege. Keep in mind that since the additional moon mob count increases over time, these values should be carefully considered. Too many mobs will definitely cause problems.");
            configBuilder.push("max_spawn_count");
            createMobMaxCap(Breecher.class, "breecher", 20, configBuilder);
            createMobMaxCap(Ghost.class, "ghost", 25, configBuilder);
            createMobMaxCap(Destroyer.class, "destroyer", 8, configBuilder);
            createMobMaxCap(Seeker.class, "seeker", 8, configBuilder);
            createMobMaxCap(Grump.class, "grump", 18, configBuilder);
            configBuilder.pop();

            configBuilder.pop();

            configBuilder.push("attributes");
            configBuilder.comment("This section contains everything related to mob stat bonuses.");

            this.enemiesOnly = configBuilder.comment("If enabled, only hostile mobs will be given attribute bonuses and potion effects.")
                    .define("mobsOnly", true);

            configBuilder.push("health");
            this.healthBlacklist = configBuilder.comment("A list of entity types that do not gain any health bonuses. Empty by default. Example: [\"minecraft:creeper\", \"abundance:screecher\"]")
                    .defineListAllowEmpty(split("healthBlacklist"), ArrayList::new, isResourceLocation());

            this.healthLunarFlatBonus = configBuilder.comment("The flat bonus gained from a full moon. Default is 10.0 (+10 hearts on full moons).")
                    .defineInRange("healthLunarFlatBonus", 10.0D, 0.0D, 10000.0D);

            this.healthLunarMultBonus = configBuilder.comment("The multiplier bonus gained from a full moon in percentage. Default is 0.5 (+50% on full moons)")
                    .defineInRange("healthLunarMultBonus", 0.5D, 0.0D, 10000.0D);

            this.healthTimeSpan = configBuilder.comment("The difficulty value for each application of the below values.")
                    .defineInRange("healthTimeSpan", 40.0D, 0.1D, 10000.0D);

            this.healthFlatBonus = configBuilder.comment("The flat bonus given each \"_time_span\" days.")
                    .defineInRange("healthFlatBonus", 1.0D, 0.0D, 10000.0D);

            this.healthFlatBonusMax = configBuilder.comment("The maximum flat bonus that can be given over time. Default is -1.0 (no limit).")
                    .defineInRange("healthFlatBonusMax", -1.0D, -1.0D, 10000.0D);

            this.healthMultBonus = configBuilder.comment("The multiplier bonus given for each \"_time_span\" days. Default is 0.8 (+80%).")
                    .defineInRange("healthMultBonus", 0.8D, 0.0D, 10000.0D);

            this.healthMultBonusMax = configBuilder.comment("The maximum multiplier bonus that can be given over time. Default is -1.0 (no limit).")
                    .defineInRange("healthMultBonusMax", -1.0D, -1.0D, 10000.0D);
            configBuilder.pop();

            configBuilder.push("damage");
            this.damageBlacklist = configBuilder.comment("A list of entity types that do not gain any damage bonuses. Empty by default. Example: [\"minecraft:creeper\", \"abundance:screecher\"]")
                    .defineListAllowEmpty(split("damageBlacklist"), ArrayList::new, isResourceLocation());

            this.damageLunarFlatBonus = configBuilder.comment("The flat bonus gained from a full moon. Default is 1.0 (+1 damage on full moons).")
                    .defineInRange("damageLunarFlatBonus", 1.0D, 0.0D, 10000.0D);

            this.damageLunarMultBonus = configBuilder.comment("The multiplier bonus gained from a full moon. Default is 0.2 (+20% on full moons).")
                    .defineInRange("damageLunarMultBonus", 0.2D, 0.0D, 10000.0D);

            this.damageTimeSpan = configBuilder.comment("The difficulty value for each application of the below values.")
                    .defineInRange("damageTimeSpan", 40.0D, 0.1D, 10000.0D);

            this.damageFlatBonus = configBuilder.comment("The flat bonus given each \"_time_span\" days. Default is 1.0 (+1 damage)")
                    .defineInRange("damageFlatBonus", 1.0D, 0.0D, 10000.0D);

            this.damageFlatBonusMax = configBuilder.comment("The maximum flat bonus that can be given over time. Default is -1.0 (no limit).")
                    .defineInRange("damageFlatBonusMax", -1.0D, -1.0D, 10000.0D);

            this.damageMultBonus = configBuilder.comment("The multiplier bonus given for each \"_time_span\" days. Default is 0.3 (+30%).")
                    .defineInRange("damageMultBonus", 0.3D, 0.0D, 10000.0D);

            this.damageMultBonusMax = configBuilder.comment("The maximum multiplier bonus that can be given over time. Default is 5.0 (+500%).")
                    .defineInRange("damageMultBonusMax", 5.0D, -1.0D, 10000.0D);
            configBuilder.pop();

            configBuilder.push("movement_speed");
            this.speedBlacklist = configBuilder.comment("A list of entity types that do not gain any speed bonuses. Empty by default. Example: [\"minecraft:creeper\", \"abundance:screecher\"]")
                    .defineListAllowEmpty(split("speedBlacklist"), ArrayList::new, isResourceLocation());

            this.speedLunarMultBonus = configBuilder.comment("The multiplier bonus gained from a full moon in percentage. Default is 0.1 (+10% during full moons)")
                    .defineInRange("speedLunarMultBonus", 0.1D, 0.0D, 1000.0D);

            this.speedTimeSpan = configBuilder.comment("The difficulty value for each application of speed related values.")
                    .defineInRange("speedTimeSpan", 40.0D, 0.1D, 10000.0D);

            this.speedMultBonus = configBuilder.comment("The multiplier bonus given for each \"_time_span\" days. Default is 0.05 (+5%).")
                    .defineInRange("damageMultBonus", 0.05D, 0.0D, 10000.0D);

            this.speedMultBonusMax = configBuilder.comment("The maximum multiplier bonus that can be given over time. Default is 0.2 (+20%).")
                    .defineInRange("damageMultBonusMax", 0.2D, -1.0D, 10000.0D);
            configBuilder.pop();

            configBuilder.push("knockback_resistance");
            this.knockbackResBlacklist = configBuilder.comment("A list of entity types that do not gain any knockback resistance bonuses. Empty by default. Example: [\"minecraft:creeper\", \"abundance:screecher\"]")
                    .defineListAllowEmpty(split("KnockbackResistanceBlacklist"), ArrayList::new, isResourceLocation());

            this.knockbackResLunarFlatBonus = configBuilder.comment("The flat bonus gained from a full moon in percentage. Default is 0.2 (+20% on full moons)")
                    .defineInRange("knockbackResLunarFlatBonus", 0.2D, 0.0D, 10000.0D);

            this.knockbackResTimeSpan = configBuilder.comment("The difficulty value for each application of knockback resistance related values.")
                    .defineInRange("knockbackResTimeSpan", 40.0D, 0.1D, 10000.0D);

            this.knockbackResFlatBonus = configBuilder.comment("The flat bonus given each \"_time_span\" days. Default is 0.05 (+5%).")
                    .defineInRange("knockbackResFlatBonus", 0.05D, 0.0D, 10000.0D);

            this.knockbackResFlatBonusMax = configBuilder.comment("The maximum flat bonus that can be given over time. Default is 0.3 (+30%).")
                    .defineInRange("knockbackResFlatBonusMax", 0.3D, -1.0D, 10000.0D);
            configBuilder.pop();
            configBuilder.pop();

            configBuilder.push("equipment");
            this.weaponLists = configBuilder.comment("A list of weapon items that mobs can spawn with, divided into tiers. Each tier group is paired with a difficulty that decides when mobs can start spawning with a weapon from the given tier group.")
                    .define("weaponLists", this.createDefaultWeaponLists());

            this.canHaveWeapons = configBuilder.comment("A list of entity types that can be given weapons.")
                    .defineListAllowEmpty(split("canHaveWeapons"), () -> DEFAULT_CAN_HAVE_WEAPONS, isResourceLocation());

            this.weaponsTimeSpan = configBuilder.comment("The difficulty value for each application of weapon related values.")
                    .defineInRange("weaponsTimeSpan", 30.0D, 0.0D, 10000.0D);

            this.weaponsChance = configBuilder.comment("The chance that a mob will be given a weapon when it spawns. This value increases in accordance to weaponsTimeSpan.")
                    .defineInRange("weaponsChance", 0.05D, 0.0D, 1.0D);

            this.weaponsLunarChance = configBuilder.comment("The additional chance gained from a full moon. Default is 0.2 (+20% chance on full moon).")
                    .defineInRange("weaponsLunarChance", 0.2D, 0.0D, 1.0D);

            this.weaponsMaxChance = configBuilder.comment("The maximum weapon chance that can be given over time. Default is 0.95 (95% chance).")
                    .defineInRange("weaponsMaxChance", 0.95D, 0.0D, 1.0D);

            this.useCurrentWeaponTierOnly = configBuilder.comment("If enabled, only weapons from the most recently unlocked weapon tier will be given to mobs. When disabled, a random weapon will be picked from all unlocked tiers.")
                    .define("useCurrentWeaponTierOnly", false);

            this.armorList = configBuilder.comment("A list of armor items that mobs can spawn with, divided into tiers. Each tier group is paired with a difficulty that decides when mobs can start spawning with armor from the given tier group.")
                    .define("armorList", this.createDefaultArmorList());

            this.canHaveArmor = configBuilder.comment("A list of entity types that can be given armor.")
                    .defineListAllowEmpty(split("canHaveArmor"), () -> DEFAULT_CAN_HAVE_ARMOR, isResourceLocation());

            this.armorTimeSpan = configBuilder.comment("The difficulty value for each application of armor related values.")
                    .defineInRange("armorTimeSpan", 30.0D, 0.0D, 10000.0D);

            this.armorChance = configBuilder.comment("The chance that a mob will be given armor when it spawns. This value increases in accordance to armorTimeSpan.")
                    .defineInRange("armorChance", 0.05D, 0.0D, 1.0D);

            this.armorLunarChance = configBuilder.comment("The additional chance gained from a full moon. Default is 0.2 (+20% chance on full moon).")
                    .defineInRange("armorLunarChance", 0.2D, 0.0D, 1.0D);

            this.armorMaxChance = configBuilder.comment("The maximum armor chance that can be given over time. Default is 0.95 (95% chance).")
                    .defineInRange("armorMaxChance", 0.95D, 0.0D, 1.0D);

            this.useCurrentArmorTierOnly = configBuilder.comment("If enabled, only armor from the most recently unlocked armor tier will be given to mobs. When disabled, random armor pieces will be picked from all unlocked tiers.")
                    .define("useCurrentArmorTierOnly", false);

            configBuilder.pop();

            configBuilder.push("potion_effects");
            this.potionEffectMap = configBuilder.comment("A list of potion effects that mobs can spawn with. Each potion effect in the list has a difficulty unlock and an optional list of mobs that should not be given the effect.")
                    .define("potionEffectList", this.createDefaultPotionList());

            this.potionEffectTimeSpan = configBuilder.comment("The difficulty value for each application of weapon related values.")
                    .defineInRange("potionEffectTimeSpan", 30.0D, 0.0D, 10000.0D);

            this.potionEffectChance = configBuilder.comment("The chance that a mob will be given a potion effect when it spawns. This value increases in accordance to potionEffectTimeSpan.")
                    .defineInRange("potionEffectChance", 0.05D, 0.0D, 1.0D);

            this.potionEffectLunarChance = configBuilder.comment("The additional chance gained from a full moon. Default is 0.2 (+20% chance on full moon).")
                    .defineInRange("potionEffectLunarChance", 0.2D, 0.0D, 1.0D);

            this.potionEffectMaxChance = configBuilder.comment("The maximum potion effect chance that can be given over time. Default is 0.95 (95% chance).")
                    .defineInRange("potionEffectMaxChance", 0.95D, 0.0D, 1.0D);
            configBuilder.pop();

            configBuilder.push("dynamic_trap");
            this.ghostFreezeTrapRange = configBuilder.comment("The range in blocks of which the Ghost Freeze trap will affect ghosts.")
                            .defineInRange("ghostFreezeTrapRange", 20, 1, 60);
            this.armorShatterTrapRange = configBuilder.comment("The range in blocks of which the Armor Shatter trap will affect mobs.")
                    .defineInRange("ghostFreezeTrapRange", 20, 1, 60);

            configBuilder.pop();

            configBuilder.push("misc");
            this.destroyerTargetRespawnPos = configBuilder.comment("If enabled, Destroyers will attempt to blow up their target player's respawn point (bed, respawn anchor etc.). " +
                    "This does not apply to the global respawn point (where players respawn if they have no bed etc.)")
                    .define("destroyerTargetRespawnPos", false);

            this.destroyerProofBlocks = configBuilder.comment("A list of blocks that the destroyer cannot explode. Generally speaking this list should be empty since destroyers are supposed to destroy any block, but if an exception is absolutely needed, the block in question can be whitelisted here.")
                    .defineListAllowEmpty(split("destroyerProofBlocks"), () -> DEFAULT_DESTROYER_PROOF_BLOCKS, isResourceLocation());

            this.grumpBucketHelmetChance = configBuilder.comment("This is the chance in percentage for grumps to spawn with a bucket helmet equipped. Grumps with bucket helmets are heavily armored against arrows.")
                    .defineInRange("grumpBucketHelmetChance", 0.05D, 0.0D, 1.0D);

            this.seekerExplosionPower = configBuilder.comment("The explosion power of Seeker fireballs.")
                    .defineInRange("seekerExplosionPower", 4, 1, 10);

            this.destroyerExplosionPower = configBuilder.comment("The explosion power of Destroyer fireballs.")
                    .defineInRange("destroyerExplosionPower", 2, 1, 10);

            this.destroyerEquipmentDamage = configBuilder.comment("Additional damage destroyer fireballs deals to its target's equipment (armor, shield etc.). Set this to 0 to deal no extra damage.")
                    .defineInRange("destroyerEquipmentDamage", 50, 0, Integer.MAX_VALUE);

            this.pauseDaylightCycle = configBuilder.comment("(For dedicated servers) If enabled, the day-night cycle will pause if no players are online.")
                    .comment("Useful if you want your players to be unable to just skip through full moons by disconnecting.")
                    .define("pauseDaylightCycle", true);
            configBuilder.pop();
        }



        //
        // VERSION CHECK
        //
        public boolean getSendUpdateMessage() {
            return this.sendUpdateMessage.get();
        }


        //
        // RAIN DAMAGE
        //
        public double getAcidRainChance() {
            return this.acidRainChance.get();
        }

        public int getRainTickRate() { return this.rainTickRate.get() * 20; }

        public float getRainDamage() {
            return (float) this.rainDamage.get();
        }

        public boolean rainDamageEnabled() {
            return this.rainDamageEnabled.get();
        }


        //
        // DIFFICULTY
        //
        public boolean multiplayerDifficultyScaling() {
            return this.multiplayerDifficultyScaling.get();
        }

        public double getMultiplayerDifficultyRateMult() {
            return this.difficultyMultiplayerRateMult.get();
        }

        public double getSleepPenalty() {
            return this.sleepPenalty.get();
        }

        public List<? extends String> getDifficultyPenaltyDimensions() {
            return this.dimensionsPenaltyList.get();
        }

        public double getDimensionPenalty() {
            return this.dimensionPenalty.get();
        }

        public boolean getAverageGroupDifficulty() {
            return this.averageGroupDifficulty.get();
        }

        public CommentedConfig getMobDifficulties() {
            return this.mobDifficulties.get();
        }


        //
        // FULL MOON
        //
        public boolean getDespawnMobsOnDeath() {
            return this.despawnMobsOnDeath.get();
        }

        public double getDifficultyUntilNextIncrease() {
            return this.difficultyUntilNextIncrease.get();
        }

        public long getMoonMobStartDifficulty(Class<? extends IFullMoonMob> entityClass) {
            return this.moonMobStartDifficulties.containsKey(entityClass) ? (this.moonMobStartDifficulties.get(entityClass).get()) : 0;
        }

        public double getMoonMobAdditionalCount(Class<? extends IFullMoonMob> entityClass) {
            return this.moonMobAdditionalCount.containsKey(entityClass) ? this.moonMobAdditionalCount.get(entityClass).get() : 0.0D;
        }

        public int getMoonMobMinCount(Class<? extends IFullMoonMob> entityClass) {
            return this.moonMobMinCount.containsKey(entityClass) ? this.moonMobMinCount.get(entityClass).get() : 0;
        }

        public int getMoonMobMaxCount(Class<? extends IFullMoonMob> entityClass) {
            return this.moonMobCountCap.containsKey(entityClass) ? this.moonMobCountCap.get(entityClass).get() : 0;
        }


        //
        // ATTRIBUTES
        //
        public boolean getEnemiesOnly() {
            return this.enemiesOnly.get();
        }

        public List<? extends String> getHealthBlacklist() {
            return this.healthBlacklist.get();
        }

        public double getHealthLunarFlatBonus() {
            return this.healthLunarFlatBonus.get();
        }

        public double getHealthLunarMultBonus() {
            return this.healthLunarMultBonus.get();
        }

        public double getHealthTimeSpan() {
            return this.healthTimeSpan.get() * References.DAY_LENGTH;
        }

        public double getHealthFlatBonus() {
            return this.healthFlatBonus.get();
        }

        public double getHealthFlatBonusMax() {
            return this.healthFlatBonusMax.get();
        }

        public double getHealthMultBonus() {
            return this.healthMultBonus.get();
        }

        public double getHealthMultBonusMax() {
            return this.healthMultBonusMax.get();
        }

        public List<? extends String> getDamageBlacklist() {
            return this.damageBlacklist.get();
        }

        public double getDamageLunarFlatBonus() {
            return this.damageLunarFlatBonus.get();
        }

        public double getDamageLunarMultBonus() {
            return this.damageLunarMultBonus.get();
        }

        public double getDamageTimeSpan() {
            return this.damageTimeSpan.get() * References.DAY_LENGTH;
        }

        public double getDamageFlatBonus() {
            return this.damageFlatBonus.get();
        }

        public double getDamageFlatBonusMax() {
            return this.damageFlatBonusMax.get();
        }

        public double getDamageMultBonus() {
            return this.damageMultBonus.get();
        }

        public double getDamageMultBonusMax() {
            return this.damageMultBonusMax.get();
        }

        public List<? extends String> getSpeedBlacklist() {
            return this.speedBlacklist.get();
        }

        public double getSpeedLunarMultBonus() {
            return this.speedLunarMultBonus.get();
        }

        public double getSpeedTimeSpan() {
            return this.speedTimeSpan.get() * References.DAY_LENGTH;
        }

        public double getSpeedMultBonus() {
            return this.speedMultBonus.get();
        }

        public double getSpeedMultBonusMax() {
            return this.speedMultBonusMax.get();
        }

        public List<? extends String> getKnockbackResBlacklist() {
            return this.knockbackResBlacklist.get();
        }

        public double getKnockbackResLunarFlatBonus() {
            return this.knockbackResLunarFlatBonus.get();
        }

        public double getKnockbackResTimeSpan() {
            return this.knockbackResTimeSpan.get() * References.DAY_LENGTH;
        }

        public double getKnockbackResFlatBonus() {
            return this.knockbackResFlatBonus.get();
        }

        public double getKnockbackResFlatBonusMax() {
            return this.knockbackResFlatBonusMax.get();
        }


        //
        // EQUIPMENT
        //
        public CommentedConfig getWeaponList() {
            return this.weaponLists.get();
        }

        public List<? extends String> getCanHaveWeapons() {
            return this.canHaveWeapons.get();
        }

        public double getWeaponsTimeSpan() {
            return this.weaponsTimeSpan.get();
        }

        public double getWeaponsChance() {
            return this.weaponsChance.get();
        }

        public double getWeaponsLunarChance() {
            return this.weaponsLunarChance.get();
        }

        public double getWeaponsMaxChance() {
            return this.weaponsMaxChance.get();
        }

        public boolean getUseCurrentWeaponTierOnly() {
            return this.useCurrentWeaponTierOnly.get();
        }

        public CommentedConfig getArmorList() {
            return this.armorList.get();
        }

        public List<? extends String> getCanHaveArmor() {
            return this.canHaveArmor.get();
        }

        public double getArmorTimeSpan() {
            return this.armorTimeSpan.get();
        }

        public double getArmorChance() {
            return this.armorChance.get();
        }

        public double getArmorLunarChance() {
            return this.armorLunarChance.get();
        }

        public double getArmorMaxChance() {
            return this.armorMaxChance.get();
        }

        public boolean getUseCurrentArmorTierOnly() {
            return this.useCurrentArmorTierOnly.get();
        }



        //
        // POTION EFFECT
        //
        public CommentedConfig getPotionMap() {
            return this.potionEffectMap.get();
        }

        public double getPotionEffectTimeSpan() {
            return this.potionEffectTimeSpan.get();
        }

        public double getPotionEffectChance() {
            return this.potionEffectChance.get();
        }

        public double getPotionEffectLunarChance() {
            return this.potionEffectLunarChance.get();
        }

        public double getPotionEffectMaxChance() {
            return this.potionEffectMaxChance.get();
        }


        //
        // TRAPS
        //
        public int getGhostFreezeTrapRange() {
            return ghostFreezeTrapRange.get();
        }
        public int getArmorShatterTrapRange() {
            return armorShatterTrapRange.get();
        }

        //
        // MISC
        //
        public boolean getDestroyerTargetRespawnPos() {
            return destroyerTargetRespawnPos.get();
        }

        public List<? extends String> getDestroyerProofBlocks() {
            return this.destroyerProofBlocks.get();
        }

        public double getGrumpBucketHelmetChance() {
            return this.grumpBucketHelmetChance.get();
        }

        public int getSeekerExplosionPower() {
            return this.seekerExplosionPower.get();
        }

        public int getDestroyerExplosionPower() {
            return this.destroyerExplosionPower.get();
        }

        public int getDestroyerEquipmentDamage() {
            return this.destroyerEquipmentDamage.get();
        }

        public boolean getPauseDaylightCycle() {
            return this.pauseDaylightCycle.get();
        }





        private void createStartDifficulty(Class<? extends IFullMoonMob> entityClass, String name, long defaultStart, ForgeConfigSpec.Builder configBuilder) {
            this.moonMobStartDifficulties.put(entityClass, configBuilder.defineInRange(name, defaultStart, 0, 100000));
        }

        private void createMobAdditionalCount(Class<? extends IFullMoonMob> entityClass, String name, double defaultAdditional, ForgeConfigSpec.Builder configBuilder) {
            this.moonMobAdditionalCount.put(entityClass, configBuilder.defineInRange(name, defaultAdditional, 0, 100));
        }

        private void createMobMinCount(Class<? extends IFullMoonMob> entityClass, String name, int defaultMin, ForgeConfigSpec.Builder configBuilder) {
            this.moonMobMinCount.put(entityClass, configBuilder.defineInRange(name, defaultMin, 0, 100));
        }

        private void createMobMaxCap(Class<? extends IFullMoonMob> entityClass, String name, int defaultMax, ForgeConfigSpec.Builder configBuilder) {
            this.moonMobCountCap.put(entityClass, configBuilder.defineInRange(name, defaultMax, 0, 100));
        }


        /** Creates the default difficulty mob config. */
        private CommentedConfig createDefaultMobDifficulties() {
            CommentedConfig difficultyMobs = TomlFormat.newConfig();
            difficultyMobs.add(String.valueOf(40), ApocalypseEntities.FEARWOLF.getId().toString());
            return difficultyMobs;
        }


        /** Creates the default weapon lists config. */
        private CommentedConfig createDefaultWeaponLists() {
            CommentedConfig weaponLists = TomlFormat.newConfig();

            final ConfigGameObjList<Item> firstTier = new ConfigGameObjList<>(ForgeRegistries.ITEMS);
            final ConfigGameObjList<Item> secondTier = new ConfigGameObjList<>(ForgeRegistries.ITEMS);
            final ConfigGameObjList<Item> thirdTier = new ConfigGameObjList<>(ForgeRegistries.ITEMS);
            final ConfigGameObjList<Item> fourthTier = new ConfigGameObjList<>(ForgeRegistries.ITEMS);
            final ConfigGameObjList<Item> fifthTier = new ConfigGameObjList<>(ForgeRegistries.ITEMS);
            final ConfigGameObjList<Item> sixthTier = new ConfigGameObjList<>(ForgeRegistries.ITEMS);

            firstTier.addElements(
                    Items.WOODEN_SHOVEL,
                    Items.WOODEN_AXE,
                    Items.WOODEN_PICKAXE,
                    Items.WOODEN_SWORD
            );
            secondTier.addElements(
                    Items.STONE_SHOVEL,
                    Items.STONE_AXE,
                    Items.STONE_PICKAXE,
                    Items.STONE_SWORD
            );
            thirdTier.addElements(
                    Items.GOLDEN_SHOVEL,
                    Items.GOLDEN_AXE,
                    Items.GOLDEN_PICKAXE,
                    Items.GOLDEN_SWORD
            );
            fourthTier.addElements(
                    Items.IRON_SHOVEL,
                    Items.IRON_AXE,
                    Items.IRON_PICKAXE,
                    Items.IRON_SWORD
            );
            fifthTier.addElements(
                    Items.DIAMOND_SHOVEL,
                    Items.DIAMOND_AXE,
                    Items.DIAMOND_PICKAXE,
                    Items.DIAMOND_SWORD,
                    Items.TRIDENT
            );
            sixthTier.addElements(
                    Items.NETHERITE_SHOVEL,
                    Items.NETHERITE_AXE,
                    Items.NETHERITE_PICKAXE,
                    Items.NETHERITE_SWORD
            );
            weaponLists.add(String.valueOf(10), firstTier);
            weaponLists.add(String.valueOf(20), secondTier);
            weaponLists.add(String.valueOf(40), thirdTier);
            weaponLists.add(String.valueOf(60), fourthTier);
            weaponLists.add(String.valueOf(100), fifthTier);
            weaponLists.add(String.valueOf(150), sixthTier);

            return weaponLists;
        }

        /** Creates the default potion list config. */
        private CommentedConfig createDefaultPotionList() {
            CommentedConfig potionList = TomlFormat.newConfig();

            potionEntry(potionList, MobEffects.FIRE_RESISTANCE, 5, EntityType.MAGMA_CUBE, EntityType.ZOMBIFIED_PIGLIN, EntityType.BLAZE, EntityType.GHAST, EntityType.STRIDER);
            potionEntry(potionList, regName(MobEffects.DAMAGE_BOOST), 60, ApocalypseEntities.GHOST.getId());
            potionEntry(potionList, MobEffects.REGENERATION, 30, EntityType.WITHER, EntityType.ENDER_DRAGON);
            potionEntry(potionList, regName(MobEffects.DAMAGE_RESISTANCE), 100, ApocalypseEntities.GHOST.getId());

            return potionList;
        }

        /** Creates the default armor lists config. */
        private CommentedConfig createDefaultArmorList() {
            CommentedConfig armorList = TomlFormat.newConfig();

            armorEntry(armorList, 10, new Item[] {
                    Items.LEATHER_BOOTS, Items.LEATHER_LEGGINGS, Items.LEATHER_CHESTPLATE, Items.LEATHER_HELMET, Items.CARVED_PUMPKIN
            });
            armorEntry(armorList, 25, new ResourceLocation[] {
                    regName(Items.CHAINMAIL_BOOTS), regName(Items.CHAINMAIL_LEGGINGS), regName(Items.CHAINMAIL_CHESTPLATE), regName(Items.CHAINMAIL_HELMET), ApocalypseItems.BUCKET_HELM.getId()
            });
            armorEntry(armorList, 40, new Item[] {
                    Items.GOLDEN_BOOTS, Items.GOLDEN_LEGGINGS, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_HELMET, Items.TURTLE_HELMET
            });
            armorEntry(armorList, 60, new Item[] {
                    Items.IRON_BOOTS, Items.IRON_LEGGINGS, Items.IRON_CHESTPLATE, Items.IRON_HELMET
            });
            armorEntry(armorList, 100, new Item[] {
                    Items.DIAMOND_BOOTS, Items.DIAMOND_LEGGINGS, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_HELMET
            });
            armorEntry(armorList, 150, new Item[] {
                    Items.NETHERITE_BOOTS, Items.NETHERITE_LEGGINGS, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_HELMET
            });
            return armorList;
        }

        private static void armorEntry(CommentedConfig config, int roundedDifficulty, Item[] armorItems) {
            List<String> itemIds = new ArrayList<>();

            for (Item item : armorItems) {
                itemIds.add(regName(item).toString());
            }
            config.add(String.valueOf(roundedDifficulty), itemIds);
        }

        private static void armorEntry(CommentedConfig config, int roundedDifficulty, ResourceLocation[] armorItemIds) {
            List<String> itemIds = new ArrayList<>();

            for (ResourceLocation id : armorItemIds) {
                itemIds.add(id.toString());
            }
            config.add(String.valueOf(roundedDifficulty), itemIds);
        }

        private static void potionEntry(CommentedConfig config, MobEffect effect, int roundedDifficulty, EntityType<?>... entityTypes) {
            List<String> blacklistedMobs = new ArrayList<>();

            for (EntityType<?> entityType : entityTypes) {
                blacklistedMobs.add(regName(entityType).toString());
            }
            // Separate effect ID and unlock-difficulty with a space.
            config.add(regName(effect) + " " + roundedDifficulty, blacklistedMobs);
        }

        private static void potionEntry(CommentedConfig config, ResourceLocation effectId, int roundedDifficulty, ResourceLocation... entityIds) {
            List<String> blacklistedMobs = new ArrayList<>();

            for (ResourceLocation entityId : entityIds) {
                blacklistedMobs.add(entityId.toString());
            }
            // Separate effect ID and unlock-difficulty with a space.
            config.add(effectId.toString() + " " + roundedDifficulty, blacklistedMobs);
        }

        private static ResourceLocation regName(Object o) {
            if (o instanceof Item item) {
                return ForgeRegistries.ITEMS.getKey(item);
            }
            else if (o instanceof EntityType<?> entityType) {
                return ForgeRegistries.ENTITY_TYPES.getKey(entityType);
            }
            else if (o instanceof MobEffect mobEffect) {
                return ForgeRegistries.MOB_EFFECTS.getKey(mobEffect);
            }
            else if (o instanceof Block block) {
                return ForgeRegistries.BLOCKS.getKey(block);
            }
            return new ResourceLocation("");
        }

        private static Predicate<Object> isResourceLocation() {
            return (obj) -> obj instanceof String && RLHelper.isValidResourceLocation((String) obj);
        }

        // Borrowed from ForgeConfigSpec
        private static final Splitter DOT_SPLITTER = Splitter.on(".");
        private static List<String> split(String path) {
            return Lists.newArrayList(DOT_SPLITTER.split(path));
        }
    }
}
