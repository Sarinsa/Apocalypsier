package com.toast.apocalypse.client.screen.widget.config;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Supplier;

public class InfoPoint extends Button {

    private static final MutableComponent GAMERE = Component.literal("?");

    public InfoPoint(int x, int y, Tooltip tooltip) {
        super(x, y, 20, 20, GAMERE, (button) -> {}, Supplier::get);
        setTooltip(tooltip);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public void onPress() {

    }

    @Override
    public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
        return false;
    }
}
