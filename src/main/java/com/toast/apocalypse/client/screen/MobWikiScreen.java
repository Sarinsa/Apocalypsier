package com.toast.apocalypse.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.toast.apocalypse.client.ClientUtil;
import com.toast.apocalypse.client.mobwiki.MobEntries;
import com.toast.apocalypse.client.mobwiki.MobEntry;
import com.toast.apocalypse.common.core.Apocalypse;
import com.toast.apocalypse.common.util.References;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;

public class MobWikiScreen extends Screen {

    private static final ResourceLocation ADDITIONAL_PAGE = Apocalypse.resourceLoc("textures/gui/mobwiki/text_page.png");
    private static final ResourceLocation FIRST_PAGE = Apocalypse.resourceLoc("textures/gui/mobwiki/illustration_page.png");

    private MobEntry currentEntry = MobEntries.EMPTY;
    private int currentPage = 0;

    private MobEntry[] unlockedEntries;

    private PageButton forwardButton;
    private PageButton backButton;

    public MobWikiScreen() {
        super(Component.translatable(References.FULL_MOON_MOB_BOOK));
    }

    @Override
    protected void init() {
        this.setFocused(null);

        this.createPages();

        int i = (this.width - 192) / 2;
        this.forwardButton = this.addWidget(new PageButton(i + 116, 159, true, (button) -> {
            this.nextPage();
        }, true));

        this.backButton = this.addWidget(new PageButton(i + 43, 159, false, (button) -> {
            this.previousPage();
        }, true));
    }

    private void createPages() {
        int size = ClientUtil.UNLOCKED_INDEXES.length;

        if (size == 0) {
            this.unlockedEntries = new MobEntry[] {};
            return;
        }

        this.unlockedEntries = new MobEntry[size];

        for (int i = 0; i < size; i++) {
            if (MobEntries.ENTRIES.containsKey(i)) {
                this.unlockedEntries[i] = MobEntries.ENTRIES.get(i);
            }
        }
    }

    private void nextPage() {
        if ((this.currentPage + 1) >= this.unlockedEntries.length) {
            return;
        }
        else {
            ++this.currentPage;
            this.currentEntry = this.unlockedEntries[this.currentPage];
        }
    }

    private void previousPage() {
        if ((this.currentPage - 1) < 0) {
            return;
        }
        else {
            --this.currentPage;
            this.currentEntry = this.unlockedEntries[this.currentPage];
        }
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (super.keyPressed(key, scancode, mods)) {
            return true;
        }
        else {
            switch(key) {
                case 266:
                    this.backButton.onPress();
                    return true;
                case 267:
                    this.forwardButton.onPress();
                    return true;
                default:
                    return false;
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float wut) {
        this.renderBackground(guiGraphics);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (this.currentEntry == MobEntries.EMPTY) {
            int i = (this.width - 280) / 2;
            // MatrixStack, x, y, uOffset, vOffset, uWidth, vHeight
            guiGraphics.blit(ADDITIONAL_PAGE, i, 25, 0, 0, 280, 280);
        }
        else {
            renderPageContent(guiGraphics, this.currentEntry);
        }
    }

    public void renderBackground(GuiGraphics guiGraphics) {
        if (minecraft.level != null) {
            // Gray transparent background
            guiGraphics.fillGradient(0, 0, width, height, -1072689136, -804253680);
        }
        else {
            // No idea how this could ever happen
            renderDirtBackground(guiGraphics);
        }
    }

    private void renderPageContent(GuiGraphics guiGraphics, MobEntry mobEntry) {
        int i = (this.width - 192) / 2;
        guiGraphics.blit(FIRST_PAGE, i, 2, 0, 0, 192, 192);
        guiGraphics.drawCenteredString(font, mobEntry.getMobName(), width / 2, 25, -1);
    }
}
