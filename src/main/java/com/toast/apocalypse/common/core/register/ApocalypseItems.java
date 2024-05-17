package com.toast.apocalypse.common.core.register;

import com.google.common.collect.ImmutableList;
import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.item.BucketHelmetItem;
import com.toast.apocalypse.common.item.FatherlyToastItem;
import com.toast.apocalypse.common.item.MobWikiBookItem;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ApocalypseItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Apocalypse.MODID);
    public static final Map<ResourceKey<CreativeModeTab>, List<RegistryObject<? extends Item>>> TAB_ITEMS = new HashMap<>();


    public static final RegistryObject<Item> FRAGMENTED_SOUL = registerSimpleItem("fragmented_soul", CreativeModeTabs.INGREDIENTS);
    public static final RegistryObject<Item> MIDNIGHT_STEEL_INGOT = registerSimpleItem("midnight_steel_ingot", CreativeModeTabs.INGREDIENTS);
    public static final RegistryObject<Item> FATHERLY_TOAST = registerItem("fatherly_toast", FatherlyToastItem::new, CreativeModeTabs.FOOD_AND_DRINKS);
    public static final RegistryObject<Item> BUCKET_HELM = registerItem("bucket_helm", BucketHelmetItem::new, CreativeModeTabs.COMBAT);
    public static final RegistryObject<Item> LUNAR_CLOCK = registerItem("lunar_clock", () -> new Item(new Item.Properties().stacksTo(1)), CreativeModeTabs.TOOLS_AND_UTILITIES);
    public static final RegistryObject<Item> APOCALYPSE_COMPENDIUM = registerItem("apocalypse_compendium", MobWikiBookItem::new, CreativeModeTabs.TOOLS_AND_UTILITIES);
    public static final RegistryObject<ForgeSpawnEggItem> GHOST_SPAWN_EGG = registerSpawnEgg("ghost", ApocalypseEntities.GHOST, 0xBCBCBC, 0x708899);
    public static final RegistryObject<ForgeSpawnEggItem> DESTROYER_SPAWN_EGG = registerSpawnEgg("destroyer", ApocalypseEntities.DESTROYER, 0x877B6F, 0x912820);
    public static final RegistryObject<ForgeSpawnEggItem> SEEKER_SPAWN_EGG = registerSpawnEgg("seeker", ApocalypseEntities.SEEKER, 0x766F87, 0x912820);
    public static final RegistryObject<ForgeSpawnEggItem> GRUMP_SPAWN_EGG = registerSpawnEgg("grump", ApocalypseEntities.GRUMP, 0xF9F9F9, 0x2D41F4);
    public static final RegistryObject<ForgeSpawnEggItem> BREECHER_SPAWN_EGG = registerSpawnEgg("breecher", ApocalypseEntities.BREECHER, 0x0DA70B, 0xF9F9F9);
    public static final RegistryObject<ForgeSpawnEggItem> FEARWOLF_SPAWN_EGG = registerSpawnEgg("fearwolf", ApocalypseEntities.FEARWOLF, 0x222127, 0x912820);

    @SafeVarargs
    protected static <T extends Item> RegistryObject<T> registerItem(String name, Supplier<T> itemSupplier, ResourceKey<CreativeModeTab>... creativeTabs) {
        RegistryObject<T> regObj = ITEMS.register(name, itemSupplier);
        queueForCreativeTabs(regObj, creativeTabs);
        return regObj;
    }

    @SafeVarargs
    private static RegistryObject<Item> registerSimpleItem(String name, ResourceKey<CreativeModeTab>... creativeTabs) {
        RegistryObject<Item> regObj = ITEMS.register(name, () -> new Item(new Item.Properties()));
        queueForCreativeTabs(regObj, creativeTabs);
        return regObj;
    }

    private static <E extends Mob> RegistryObject<ForgeSpawnEggItem> registerSpawnEgg(String name, Supplier<EntityType<E>> entityTypeSupplier, int backgroundColor, int highlightColor) {
        RegistryObject<ForgeSpawnEggItem> spawnEgg = ITEMS.register(name + "_spawn_egg", () -> new ForgeSpawnEggItem(entityTypeSupplier, backgroundColor, highlightColor, new Item.Properties()));
        queueForCreativeTabs(spawnEgg, CreativeModeTabs.SPAWN_EGGS);
        return spawnEgg;
    }

    @SafeVarargs
    protected static void queueForCreativeTabs(RegistryObject<? extends Item> item, ResourceKey<CreativeModeTab>... creativeTabs) {
        for (ResourceKey<CreativeModeTab> tab : creativeTabs) {
            if (!TAB_ITEMS.containsKey(tab)) {
                List<RegistryObject<? extends Item>> list = new ArrayList<>();
                list.add(item);
                TAB_ITEMS.put(tab, list);
            } else {
                TAB_ITEMS.get(tab).add(item);
            }
        }
    }

    /**
     * Called when creative tabs gets populated with items.
     */
    public static void onCreativeTabPopulate(BuildCreativeModeTabContentsEvent event) {
        if (TAB_ITEMS.containsKey(event.getTabKey())) {
            List<RegistryObject<? extends Item>> items = TAB_ITEMS.get(event.getTabKey());
            items.forEach((regObj) -> event.accept(regObj.get()));
        }
    }

    public static void onMissingMappings(MissingMappingsEvent event) {
        List<MissingMappingsEvent.Mapping<Item>> mappings = event.getMappings(ForgeRegistries.ITEMS.getRegistryKey(), Apocalypse.MODID);

        for (MissingMappingsEvent.Mapping<Item> mapping : mappings) {
            if (mapping.getKey().getPath().equals("lunarium_ingot")) {
                mapping.remap(ApocalypseItems.MIDNIGHT_STEEL_INGOT.get());
            }
            else if (mapping.getKey().getPath().equals("soul_fragment")) {
                mapping.remap(ApocalypseItems.FRAGMENTED_SOUL.get());
            }
        }
    }
}
