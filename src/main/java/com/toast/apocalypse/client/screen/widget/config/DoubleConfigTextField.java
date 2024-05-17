package com.toast.apocalypse.client.screen.widget.config;


import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;

public final class DoubleConfigTextField extends AbstractConfigTextField<Double> {

    public DoubleConfigTextField(Font fontRenderer, double defaultValue, double minValue, double maxValue, int x, int y, @Nullable MutableComponent descriptor, @Nullable Tooltip tooltip) {
        super(fontRenderer, defaultValue, minValue, maxValue, x, y, 60, 20, descriptor, tooltip);
    }

    @Override
    protected boolean checkIsValidValue(String value) {
        try {
            double doubleValue = Double.parseDouble(value);
            return doubleValue >= this.minValue && doubleValue <= this.maxValue;
        }
        catch (NumberFormatException ignored) {
            return false;
        }
    }

    @Override
    protected void setCurrentValue(String value) {
        this.setCurrentValue(Double.parseDouble(value));
    }

    public double getDoubleValue() {
        return this.getCurrentValue();
    }

    private void setDoubleValue(double value) {
        this.setCurrentValue(value);
    }

    @Override
    protected boolean isValidCharacter(String value, char character, int cursorPosition) {
        if (Character.isDigit(character)) {
            return true;
        }
        else if (character == '.') {
            return !value.contains(".") && cursorPosition != 0;
        }
        else if (character == '-') {
            return !value.contains("-") && cursorPosition == 0;
        }
        return false;
    }

    @Override
    protected int maxValueLength() {
        return 9;
    }
}
