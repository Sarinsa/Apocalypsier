package com.toast.apocalypse.common.inventory.container;

import com.toast.apocalypse.api.misc.MethodsReturnNonnullByDefault;
import com.toast.apocalypse.common.core.register.ApocalypseItems;
import com.toast.apocalypse.common.entity.living.Grump;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GrumpInventoryContainer extends AbstractContainerMenu {

    private final Container grumpInventory;
    private final Grump grump;

    public GrumpInventoryContainer(int containerId, Inventory playerInventory, Container grumpInventory, final Grump grump) {
        super(null, containerId);
        this.grumpInventory = grumpInventory;
        this.grump = grump;

        grumpInventory.startOpen(playerInventory.player);

        // Grump inventory
        addSlot(new Slot(grumpInventory, 0, 6, 18) {
            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return itemStack.getItem() == Items.SADDLE || itemStack.getItem() == ApocalypseItems.BUCKET_HELM.get() && !this.hasItem();
            }

            @Override
            @OnlyIn(Dist.CLIENT)
            public boolean isActive() {
                return true;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Player inventory
        for(int i1 = 0; i1 < 3; ++i1) {
            for(int k1 = 0; k1 < 9; ++k1) {
                addSlot(new Slot(playerInventory, k1 + i1 * 9 + 9, 8 + k1 * 18, 102 + i1 * 18 - 18));
            }
        }

        for(int j1 = 0; j1 < 9; ++j1) {
            addSlot(new Slot(playerInventory, j1, 8 + j1 * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return grumpInventory.stillValid(player) && grump.isAlive() && grump.distanceTo(player) < 8.0F;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            int i = grumpInventory.getContainerSize();

            if (index < i) {
                if (!moveItemStackTo(itemstack1, i, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (getSlot(1).mayPlace(itemstack1) && !getSlot(1).hasItem()) {
                if (!moveItemStackTo(itemstack1, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (getSlot(0).mayPlace(itemstack1)) {
                if (!moveItemStackTo(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (i <= 2 || !moveItemStackTo(itemstack1, 2, i, false)) {
                int j = i + 27;
                int k = j + 9;

                if (index >= j && index < k) {
                    if (!moveItemStackTo(itemstack1, i, j, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                else if (index < j) {
                    if (!moveItemStackTo(itemstack1, j, k, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                else if (!moveItemStackTo(itemstack1, j, j, false)) {
                    return ItemStack.EMPTY;
                }
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            }
            else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        grumpInventory.stopOpen(player);
    }
}
