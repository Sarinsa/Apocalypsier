package com.toast.apocalypse.client.screen.widget.config;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;

public class IntegerConfigTextField extends AbstractConfigTextField<Integer> {

    public IntegerConfigTextField(Font fontRenderer, Integer defaultValue, Integer minValue, Integer maxValue, int x, int y, int width, int height, @Nullable MutableComponent descriptor, @Nullable Tooltip tooltip) {
        super(fontRenderer, defaultValue, minValue, maxValue, x, y, width, height, descriptor, tooltip);
    }

    @Override
    protected boolean checkIsValidValue(String value) {
        try {
            int intValue = Integer.parseInt(value);

            return intValue >= this.minValue && intValue <= this.maxValue;
        }
        catch (Exception ignored) {
            return false;
        }
    }

    @Override
    protected void setCurrentValue(String value) {
        this.setCurrentValue(Integer.parseInt(value));
    }

    @Override
    protected boolean isValidCharacter(String value, char character, int index) {
        return Character.isDigit(character);
    }

    @Override
    protected int maxValueLength() {
        return 9;
    }
}
