package com.toast.apocalypse.common.core.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;

/**
 * Apocalypse's client side config.
 */
public class ApocalypseClientConfig {

    public enum PositionWidthAnchor {
        MIDDLE,
        RIGHT,
        LEFT
    }

    public enum PositionHeightAnchor {
        MIDDLE,
        TOP,
        BOTTOM
    }

    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        Pair<Client, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
    }

    public static final class Client {

        private final ForgeConfigSpec.EnumValue<PositionWidthAnchor> difficultyRenderPosWidth;
        private final ForgeConfigSpec.EnumValue<PositionHeightAnchor> difficultyRenderPosHeight;
        private final ForgeConfigSpec.IntValue difficultyRenderXOffset;
        private final ForgeConfigSpec.IntValue difficultyRenderYOffset;

        private final ForgeConfigSpec.EnumValue<PositionWidthAnchor> worldConfigButtonPosWidth;
        private final ForgeConfigSpec.EnumValue<PositionHeightAnchor> worldConfigButtonPosHeight;
        private final ForgeConfigSpec.IntValue worldConfigButtonXOffset;
        private final ForgeConfigSpec.IntValue worldConfigButtonYOffset;

        private final ForgeConfigSpec.BooleanValue renderDifficultyInCreative;
        private final ForgeConfigSpec.BooleanValue keybindOnly;

        private final ForgeConfigSpec.ConfigValue<String> rainColor;


        private Client(ForgeConfigSpec.Builder configBuilder) {
            configBuilder.push("in_game_gui");
            this.difficultyRenderPosWidth = configBuilder.comment("Determines the base X position on the screen where the world difficulty count should render")
                    .defineEnum("difficultyRenderPosWidth", PositionWidthAnchor.MIDDLE, PositionWidthAnchor.values());

            this.difficultyRenderPosHeight = configBuilder.comment("Determines the base Y position on the screen where the world difficulty count should render")
                    .defineEnum("difficultyRenderPosHeight", PositionHeightAnchor.TOP, PositionHeightAnchor.values());

            this.difficultyRenderXOffset = configBuilder.comment("Additional X offset for where to render difficulty in-game")
                    .defineInRange("difficultyRenderXOffset", 0, -10000, 10000);

            this.difficultyRenderYOffset = configBuilder.comment("Additional Y offset for where to render difficulty in-game")
                    .defineInRange("difficultyRenderYOffset", 0, -10000, 10000);

            this.renderDifficultyInCreative = configBuilder.comment("Toggles difficulty being rendered when the player is in creative mode.")
                    .define("renderDifficultyInCreative", true);

            this.keybindOnly = configBuilder.comment("If enabled, difficulty will only be displayed if the Apocalypse difficulty keybind is pressed.")
                            .define("keybindOnly", false);
            configBuilder.pop();

            configBuilder.push("world_config_button");
            this.worldConfigButtonPosWidth = configBuilder.comment("Determines the base X position of the Apocalypse world config button.")
                    .defineEnum("worldConfigButtonPosWidth", PositionWidthAnchor.MIDDLE, PositionWidthAnchor.values());

            this.worldConfigButtonPosHeight = configBuilder.comment("Determines the base Y position of the Apocalypse world config button.")
                    .defineEnum("worldConfigButtonPosHeight", PositionHeightAnchor.BOTTOM, PositionHeightAnchor.values());

            this.worldConfigButtonXOffset = configBuilder.comment("Additional X offset.")
                    .defineInRange("worldConfigButtonXOffset", 0, -10000, 10000);

            this.worldConfigButtonYOffset = configBuilder.comment("Additional Y offset.")
                    .defineInRange("worldConfigButtonYOffset", -80, -10000, 10000);
            configBuilder.pop();

            configBuilder.push("acid_rain_properties");
            this.rainColor = configBuilder.comment("Decides the color to be used for acid rain. Must be hex color. Keep in mind that the color of the rain texture itself will \"mix\" with this setting.")
                            .define("rainColor", "#ACFF75", (o) -> {
                                try {
                                    if (o instanceof String) {
                                        Color.decode((String) o);
                                        return true;
                                    }
                                    return false;
                                }
                                catch(NumberFormatException ignored) {
                                    return false;
                                }
                            });

            configBuilder.pop();
        }

        public PositionWidthAnchor getDifficultyRenderPosWidth() {
            return this.difficultyRenderPosWidth.get();
        }

        public PositionHeightAnchor getDifficultyRenderPosHeight() {
            return this.difficultyRenderPosHeight.get();
        }

        public int getDifficultyRenderXOffset() {
            return this.difficultyRenderXOffset.get();
        }

        public int getDifficultyRenderYOffset() {
            return this.difficultyRenderYOffset.get();
        }

        public PositionWidthAnchor getWorldConfigButtonPosWidth() {
            return this.worldConfigButtonPosWidth.get();
        }

        public PositionHeightAnchor getWorldConfigButtonPosHeight() {
            return this.worldConfigButtonPosHeight.get();
        }

        public int getWorldConfigButtonXOffset() {
            return this.worldConfigButtonXOffset.get();
        }

        public int getWorldConfigButtonYOffset() {
            return this.worldConfigButtonYOffset.get();
        }

        public boolean getRenderDifficultyInCreative() {
            return this.renderDifficultyInCreative.get();
        }

        public boolean getKeybindOnly() {
            return this.keybindOnly.get();
        }

        public String getAcidRainColor() {
            return this.rainColor.get();
        }
    }
}
