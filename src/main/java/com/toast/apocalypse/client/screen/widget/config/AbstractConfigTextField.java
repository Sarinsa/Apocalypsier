package com.toast.apocalypse.client.screen.widget.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;

public abstract class AbstractConfigTextField<T> extends EditBox {

    private final MutableComponent descriptor;
    private final Tooltip tooltip;
    private T currentValue;

    protected final T defaultValue;
    protected final T minValue;
    protected final T maxValue;

    public AbstractConfigTextField(Font fontRenderer, T defaultValue, T minValue, T maxValue, int x, int y, int width, int height, @Nullable MutableComponent descriptor, @Nullable Tooltip tooltip) {
        super(fontRenderer, x, y, width, height, Component.empty());
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.setValue(String.valueOf(defaultValue));
        this.currentValue = defaultValue;
        this.descriptor = descriptor == null ? null : descriptor.withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);
        this.tooltip = tooltip;
    }

    @Override
    @SuppressWarnings("all")
    public void onValueChange(String value) {
        super.onValueChange(value);

        if (checkIsValidValue(value)) {
            setTextColor(ChatFormatting.WHITE.getColor());
            setCurrentValue(value);
        }
        else {
            setTextColor(ChatFormatting.RED.getColor());
        }
    }

    @Override
    public boolean charTyped(char character, int upperCase) {
        if (!canConsumeInput()) {
            return false;
        }
        else if (isValidCharacter(getValue(), character, getCursorPosition())) {
            if (isEditable() && getValue().length() < maxValueLength()) {
                insertText(Character.toString(character));
            }
            return true;
        }
        else {
            return false;
        }
    }

    protected abstract boolean checkIsValidValue(String value);

    public final T getCurrentValue() {
        return currentValue;
    }

    public final void setCurrentValue(T value) {
        currentValue = value;
    }

    protected abstract void setCurrentValue(String value);

    protected abstract boolean isValidCharacter(String value, char character, int cursorPosition);

    protected abstract int maxValueLength();

    @Nullable
    public Component getDescriptor() {
        return descriptor;
    }

    @Nullable
    public Tooltip getTooltip() {
        return tooltip;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        super.render(guiGraphics, x, y, partialTick);

        if (visible && descriptor != null) {
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, descriptor, getX() + width / 2, getY() - (height / 2) - 3, -1);
        }
    }
}
