package com.toast.apocalypse.common.core.register;

import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.menus.DynamicTrapMenu;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ApocalypseMenus {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Apocalypse.MODID);


    public static final RegistryObject<MenuType<DynamicTrapMenu>> DYNAMIC_TRAP = register("dynamic_trap", () -> new MenuType<>(DynamicTrapMenu::new, FeatureFlags.VANILLA_SET));


    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> register(String name, Supplier<MenuType<T>> menuTypeSupplier) {
        return MENU_TYPES.register(name, menuTypeSupplier);
    }
}
