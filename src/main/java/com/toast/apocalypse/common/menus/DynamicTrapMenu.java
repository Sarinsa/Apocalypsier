package com.toast.apocalypse.common.menus;

import com.toast.apocalypse.common.core.register.ApocalypseMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DynamicTrapMenu extends AbstractContainerMenu {

    private final Container dynamicTrap;
    private final ContainerData data;
    @Nullable
    private final BlockPos trapPos;


    public DynamicTrapMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(9), new SimpleContainerData(5), null);
    }

    public DynamicTrapMenu(int containerId, Inventory inventory, Container container, ContainerData containerData, @Nullable BlockPos pos) {
        super(ApocalypseMenus.DYNAMIC_TRAP.get(), containerId);

        checkContainerSize(container, 9);
        checkContainerDataCount(containerData, 5);
        dynamicTrap = container;
        data = containerData;
        trapPos = pos;
        container.startOpen(inventory.player);

        // Dynamic Trap inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                addSlot(new Slot(container, j + i * 3, 62 + j * 18, 17 + i * 18));
            }
        }

        // Player inventory
        for (int k = 0; k < 3; ++k) {
            for (int i1 = 0; i1 < 9; ++i1) {
                addSlot(new Slot(inventory, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
            }
        }

        for (int l = 0; l < 9; ++l) {
            addSlot(new Slot(inventory, l, 8 + l * 18, 142));
        }

        addDataSlots(containerData);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack itemInSlot = slot.getItem();
            itemStack = itemInSlot.copy();

            if (slotIndex < 9) {
                if (!moveItemStackTo(itemInSlot, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (!moveItemStackTo(itemInSlot, 0, 9, false)) {
                return ItemStack.EMPTY;
            }

            if (itemInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            }
            else {
                slot.setChanged();
            }

            if (itemInSlot.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemInSlot);
        }
        return itemStack;
    }

    public int getPreparationTime() {
        return data.get(0);
    }

    public int getMaxPreparationTime() {
        return data.get(1);
    }

    public BlockPos getTrapPos() {
        return new BlockPos(data.get(2), data.get(3), data.get(4));
    }

    @Override
    public boolean stillValid(Player player) {
        return dynamicTrap.stillValid(player);
    }

    public void removed(Player player) {
        super.removed(player);
        dynamicTrap.stopOpen(player);
    }
}
