package com.technicalitiesmc.scm.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.technicalitiesmc.lib.client.screen.TKMenuScreen;
import com.technicalitiesmc.scm.SuperCircuitMaker;
import com.technicalitiesmc.scm.circuit.util.BlueprintDataPacket;
import com.technicalitiesmc.scm.circuit.util.FileSaver;
import com.technicalitiesmc.scm.menu.BlueprintSavingMenu;
import com.technicalitiesmc.scm.menu.ConstantMenu;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.List;

import static com.technicalitiesmc.scm.circuit.CircuitHelper.FILE_VERSION;

public class BlueprintSavingScreen extends TKMenuScreen<BlueprintSavingMenu> {

    FileSaver fs;
    String playerName;

    HashMap<String,Integer> items;

    private static final Component[] TEXT = new Component[]{
            new TranslatableComponent("container." + SuperCircuitMaker.MODID + ".blueprint.save"),//保存按钮
            new TranslatableComponent("container." + SuperCircuitMaker.MODID + ".blueprint.cancelled"),//取消按钮
            new TranslatableComponent("container." + SuperCircuitMaker.MODID + ".blueprint.name_tag"),//名字
            new TranslatableComponent("container." + SuperCircuitMaker.MODID + ".blueprint.introduction"),//介绍
            new TranslatableComponent("container." + SuperCircuitMaker.MODID + ".blueprint.author"),//作者
            new TranslatableComponent("container." + SuperCircuitMaker.MODID + ".blueprint.version"),//文件版本
            new TranslatableComponent("container." + SuperCircuitMaker.MODID + ".blueprint.item"),//文件版本
            new TranslatableComponent("container." + SuperCircuitMaker.MODID + ".blueprint.size")//文件大小
    };
    //材质位置
    private static final ResourceLocation BACKGROUND = new ResourceLocation(SuperCircuitMaker.MODID, "textures/item/blueprint.png");

    public BlueprintSavingScreen(BlueprintSavingMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, BACKGROUND);
        imageWidth = 256;
        imageHeight = 256;
        titleLabelY = 10;
    }

    public void loadFileSaver(FileSaver fs,Player player,HashMap<String,Integer> items){
        this.fs = fs;
        playerName = player.getName().getContents();
        this.items = items;
    }

    private Button[] buttons = new Button[2];

    private EditBox nameBox,introBox;

    private static String name = "";
    private static String introduction = "";

    private static int editBoxWidth = 120;
    private static int editBoxHeight = 20;

    private static int leftOffset = 20;

    public int offset(int x){
        return x+(minecraft.screen.width-imageWidth)/2+leftOffset;
    }

    @Override
    protected void init(){
        super.init();



        int clicked = -1;
        //这是输入的文本框
        nameBox = new EditBox(minecraft.font,offset(0),45,editBoxWidth,editBoxHeight,TEXT[2]);
        introBox = new EditBox(minecraft.font,offset(0),80,editBoxWidth,editBoxHeight,TEXT[3]);
        addRenderableWidget(nameBox);
        addRenderableWidget(introBox);

        //命名tag
        addRenderableWidget(new PlainTextButton(offset(0),35,50,10,TEXT[2],b -> {},minecraft.font));
        //写介绍tag
        addRenderableWidget(new PlainTextButton(offset(0),70,50,10,TEXT[3],b -> {},minecraft.font));

        //作者tag
        var authorTag = TEXT[4].copy().append(": "+playerName);
        addRenderableWidget(new PlainTextButton(offset(0),110,50,10,authorTag,b -> {},minecraft.font));

        //版本tag
        var versionTag = TEXT[5].copy().append(": "+FILE_VERSION);
        addRenderableWidget(new PlainTextButton(offset(0),130,50,10,versionTag,b -> {},minecraft.font));

        //版本tag
        var sizeTag = TEXT[7].copy().append(": "+String.format("%.2f",((double)fs.getSize(name,introduction,playerName))/1024)+" kb");
        addRenderableWidget(new PlainTextButton(offset(0),150,50,10,sizeTag,b -> {},minecraft.font));

        //物品清单tag
        addRenderableWidget(new PlainTextButton(offset(135),35,50,10,TEXT[6].copy().append(":"),b -> {},minecraft.font));

        //物品清单
        int ix = 0;
        System.out.println(items.keySet().size());
        for(String key:items.keySet()){

            var itemTag = new TranslatableComponent("item." + SuperCircuitMaker.MODID + "." +key).copy().append(" * "+items.get(key));
            addRenderableWidget(new PlainTextButton(offset(140),47+12*ix,50,10,itemTag,b -> {},minecraft.font));
            ix++;
        }

        //这是按钮
        for (int i = 0; i < 2; i++) {
            var j = i;
            buttons[i] = addRenderableWidget(new Button(offset(0), 170+i*30, editBoxWidth, editBoxHeight, TEXT[i], b -> {
                if(b.equals(buttons[0])) {name = nameBox.getValue();
                    introduction = introBox.getValue();
                    if(fs.save(name,introduction,playerName)){
                        minecraft.player.displayClientMessage(new TranslatableComponent("msg." + SuperCircuitMaker.MODID + ".blueprint.save"), true);
                    }else {
                        minecraft.player.displayClientMessage(new TranslatableComponent("msg." + SuperCircuitMaker.MODID + ".blueprint.save.failed"), true);
                    }
                    onClose();}
                if(b.equals(buttons[1])) onClose();
            }));
        }
    }

    public void onClose(){
        super.onClose();
    }

    @Override
    protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) {
        var fullTitle = title.copy().append("");
        font.draw(p_97808_, fullTitle, (imageWidth - font.width(fullTitle)) / 2f, titleLabelY, 0xFFFFFF);
    }
}
