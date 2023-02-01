package com.technicalitiesmc.scm.menu;

import com.mojang.blaze3d.vertex.PoseStack;
import com.technicalitiesmc.lib.menu.TKMenu;
import com.technicalitiesmc.lib.util.value.Reference;
import com.technicalitiesmc.lib.util.value.Value;
import com.technicalitiesmc.scm.client.screen.BlueprintSavingScreen;
import com.technicalitiesmc.scm.init.SCMMenus;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;

import java.awt.*;
import java.util.function.Predicate;

public class BlueprintSavingMenu extends TKMenu {
    //protected final Inventory playerInv;
    //private final ResourceLocation texture;
    //private final int width, height;
    private final Predicate<Player> accessTester;

    public BlueprintSavingMenu(int id, Inventory playerInv, Predicate<Player> accessTester, Reference<Integer> name) {
        super(SCMMenus.BLUEPRINTSAVING, id, playerInv);
        this.accessTester = accessTester;
        //this.output = output;
        addDataSlot(new DataSlot() {
            @Override
            public int get(){return 0;}

            @Override
            public void set(int value) {}
        });
    }

    public BlueprintSavingMenu(int id, Inventory playerInv) {
        this(id, playerInv, p -> true, new Value<>(0));
    }

    @Override
    public boolean stillValid(Player player) {
        return accessTester.test(player);
    }


}
