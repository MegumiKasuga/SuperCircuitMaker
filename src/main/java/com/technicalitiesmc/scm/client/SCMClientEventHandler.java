package com.technicalitiesmc.scm.client;

import com.mojang.math.Vector3f;
import com.technicalitiesmc.lib.circuit.component.ComponentSlot;
import com.technicalitiesmc.lib.circuit.component.ComponentState;
import com.technicalitiesmc.lib.util.Utils;
import com.technicalitiesmc.scm.SuperCircuitMaker;
import com.technicalitiesmc.scm.block.CircuitBlock;
import com.technicalitiesmc.scm.circuit.util.*;
import com.technicalitiesmc.scm.client.screen.BlueprintSavingScreen;
import com.technicalitiesmc.scm.client.screen.PaletteScreen;
import com.technicalitiesmc.scm.init.SCMItems;
import com.technicalitiesmc.scm.item.PaletteItem;
import com.technicalitiesmc.scm.menu.BlueprintSavingMenu;
import com.technicalitiesmc.scm.placement.ComponentPlacementHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import static com.technicalitiesmc.scm.circuit.CircuitHelper.FOLDER_NAME;
import static com.technicalitiesmc.scm.circuit.CircuitHelper.MAX_BLUEPRINT_SIZE;

@Mod.EventBusSubscriber(modid = SuperCircuitMaker.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SCMClientEventHandler {

    private static int busyTimer = 0;
    private static boolean partial = false;
    private static KeyMapping partialMapping;
    private static InteractionHand partialHand;

    private static int step = 0;

    private static BlockPos firstPos, secondPos;
    private static ComponentSlotPos firstHitPos, secondHitPos;

    public static void error(){
        secondPos = null;
        secondHitPos = null;
    }

    public static Block getBlockByPos(BlockPos blockPos){
        try{return Minecraft.getInstance().level.getBlockEntity(blockPos).getBlockState().getBlock();}catch (Exception e){return null;}
    }

    public static BlockState getBlockStateByPos(BlockPos blockPos){
        try{return Minecraft.getInstance().level.getBlockEntity(blockPos).getBlockState();}catch (Exception e){return null;}
    }

    public static void flipHitPos(String axis){
        int cache;
        switch (axis){
            case "x":
                cache = firstHitPos.pos().x();
                firstHitPos = new ComponentSlotPos(new ComponentPos(secondHitPos.pos().x(), firstHitPos.pos().y(), firstHitPos.pos().z()), firstHitPos.slot());
                secondHitPos = new ComponentSlotPos(new ComponentPos(cache,secondHitPos.pos().y(),secondHitPos.pos().z()),secondHitPos.slot());
                return;
            case "y":
                cache = firstHitPos.pos().y();
                firstHitPos = new ComponentSlotPos(new ComponentPos(firstHitPos.pos().x(), secondHitPos.pos().y(), firstHitPos.pos().z()), firstHitPos.slot());
                secondHitPos = new ComponentSlotPos(new ComponentPos(secondHitPos.pos().x(),cache,secondHitPos.pos().z()),secondHitPos.slot());
                return;
            case "z":
                cache = firstHitPos.pos().z();
                firstHitPos = new ComponentSlotPos(new ComponentPos(firstHitPos.pos().x(),firstHitPos.pos().y(),secondHitPos.pos().z()),firstHitPos.slot());
                secondHitPos = new ComponentSlotPos(new ComponentPos(secondHitPos.pos().x(),secondHitPos.pos().y(),cache),secondHitPos.slot());
                return;
            default:
                return;
        }
    }

    public static boolean sameBlock(){
        try{return ((Vec3i) firstPos).getX() == ((Vec3i) secondPos).getX() && ((Vec3i) firstPos).getZ() == ((Vec3i) secondPos).getZ();}catch (Exception e){return false;}
    }

    public static boolean isXBottomEdge(BlockPos pos){
        return pos.getX() == firstPos.getX();
    }

    public static boolean isZBottomEdge(BlockPos pos){
        return pos.getZ() == firstPos.getZ();
    }

    public static boolean isXTopEdge(BlockPos pos){
        return pos.getX() == secondPos.getX();
    }

    public static boolean isZTopEdge(BlockPos pos){
        return pos.getZ() == secondPos.getZ();
    }

    static ParticleDirector pdFirst = null;
    static ParticleDirector pbe = null;


    @SubscribeEvent
    public static void onClickInput(InputEvent.ClickInputEvent event) {
        if (busyTimer > 0) {
            event.setCanceled(true);
            return;
        }

        var minecraft = Minecraft.getInstance();
        if (!(minecraft.hitResult instanceof BlockHitResult hit)) {
            return;
        }
        var state = Utils.resolveHit(minecraft.level, hit);
        if (!(state.getBlock() instanceof CircuitBlock block)) {
            return;
        }

        if(pdFirst == null) pdFirst = new ParticleDirector(minecraft);
        if(pbe == null) pbe = new ParticleDirector(minecraft);

        InteractionResult result;
        if (event.isUseItem()) {
            //判断玩家手中的物品是不是蓝图
            if(minecraft.player.getMainHandItem().is(SCMItems.BLUEPRINT.get())){

                //如果此时玩家处于潜行状态，那么将认为玩家将要取消保存过程
                if(minecraft.player.isCrouching() && step != 0){
                    minecraft.player.displayClientMessage(new TranslatableComponent("msg." + SuperCircuitMaker.MODID + ".blueprint.save.cancelled"), true);
                    firstPos = null;
                    firstHitPos = null;
                    if(pdFirst != null) pdFirst.stop();
                    if(pbe != null) pbe.stop();
                    pdFirst = null;
                    pbe = null;
                    error();
                    step = 0;
                    return;
                }

                //step == 5 执行保存过程
                if (step == 5 && firstPos != null && secondPos != null && firstHitPos != null && secondHitPos != null) {
                    int cache = 0;

                    //以下操作让firstPos的x和z轴坐标都小于secondPos的对应数值，并使得componentSlotPos与blockPos相对应
                    if(((Vec3i) firstPos).getX() > ((Vec3i) secondPos).getX()) {
                        cache = ((Vec3i) firstPos).getX();
                        firstPos = new BlockPos(new Vec3i(((Vec3i) secondPos).getX(), ((Vec3i) firstPos).getY(), ((Vec3i) firstPos).getZ()));
                        secondPos = new BlockPos(cache, ((Vec3i) secondPos).getY(), ((Vec3i) secondPos).getZ());

                        flipHitPos("x");
                    }
                    if(((Vec3i) firstPos).getZ() > ((Vec3i) secondPos).getZ()){
                        cache = ((Vec3i) firstPos).getZ();
                        firstPos = new BlockPos(new Vec3i(((Vec3i) firstPos).getX(),((Vec3i) firstPos).getY(),((Vec3i) secondPos).getZ()));
                        secondPos = new BlockPos(new Vec3i(((Vec3i) secondPos).getX(),((Vec3i) secondPos).getY(),cache));

                        flipHitPos("z");
                    }

                    //处理y轴
                    if(firstHitPos.pos().y()>secondHitPos.pos().y()){
                        flipHitPos("y");
                    }

                    //方块相同时处理两个hitPos点
                    if(sameBlock()){
                        if(firstHitPos.pos().x()>secondHitPos.pos().x()) flipHitPos("x");
                        if(firstHitPos.pos().z()>secondHitPos.pos().z()) flipHitPos("z");
                    }

                    //处理完成后，我们可以保证坐标较小的方块数据在前，坐标较大的方块数据在后

                    BlockPos blockPos = null;
                    Block scannerBlock = null;
                    CircuitBlock cb = null;
                    CircuitBlock.Data data = null;
                    BlueprintDataPacket bdp = null;
                    ArrayList<BlueprintDataPacket> datas = new ArrayList<BlueprintDataPacket>();
                    HashMap<String,Integer> itemTab = new HashMap<>();
                    ArrayList<byte[]> posTab = new ArrayList<>();

                    //对这个范围内的block进行遍历
                    for(int x_axis = firstPos.getX(); x_axis <=secondPos.getX(); x_axis++){
                        for(int z_axis = firstPos.getZ(); z_axis <=secondPos.getZ(); z_axis++){
                            blockPos = new BlockPos(new Vec3i(x_axis,firstPos.getY(),z_axis));
                            scannerBlock = getBlockByPos(blockPos);
                            if(scannerBlock != null && scannerBlock instanceof CircuitBlock){
                                //componentSlot的xz朝向和world的xz朝向是一样的
                                cb = (CircuitBlock) scannerBlock;
                                data = cb.getData(minecraft.player.level,blockPos);
                                if(data != null){
                                    //获得每个方块的元件数据
                                    bdp = cb.getBlueprintData(data,new ComponentSlotPos(isXBottomEdge(blockPos) ? firstHitPos.pos().x() : 0,firstHitPos.pos().y(),isZBottomEdge(blockPos) ? firstHitPos.pos().z() : 0,ComponentSlot.DEFAULT),new ComponentSlotPos(isXTopEdge(blockPos) ? secondHitPos.pos().x() : 7,secondHitPos.pos().y(),isZTopEdge(blockPos) ? secondHitPos.pos().z() : 7,ComponentSlot.DEFAULT));
                                    if(bdp != null) System.out.println(bdp.isEmpty());
                                    if(bdp != null && !bdp.isEmpty()){
                                        //将这整张网格进行平移，使得整张网格成为以(0,0)开始
                                        bdp.offset((x_axis-firstPos.getX())*8-firstHitPos.pos().x(),(z_axis-firstPos.getZ())*8-firstHitPos.pos().z());
                                        //存储
                                        datas.add(bdp);
                                        itemTab = BlueprintDataPacket.itemMapMerge(bdp.getItems(),itemTab);
                                        posTab = BlueprintDataPacket.posListMerge(posTab,bdp.getPosList());
                                    }
                                }
                            }
                        }
                    }

                    if(!itemTab.isEmpty() && !posTab.isEmpty()){

                        BlueprintSavingScreen bss = new BlueprintSavingScreen(new BlueprintSavingMenu(minecraft.player.getId(),minecraft.player.getInventory()),minecraft.player.getInventory(),new TranslatableComponent("container."+SuperCircuitMaker.MODID+".blueprint"));


                        byte[] itemBytes = BlueprintDataPacket.getItemsSerialize(itemTab);//物品清单
                        byte[] posBytes = BlueprintDataPacket.getPosSerialize(posTab);//位置

                        bss.loadFileSaver(new FileSaver(itemBytes,posBytes,datas), minecraft.player,itemTab);
                        minecraft.setScreen(bss);

                        firstPos = null;
                        firstHitPos = null;
                        error();
                        step = 0;

                        pdFirst.stop();
                        pbe.stop();
                        pdFirst = null;
                        pbe = null;

                        System.gc();
                        return;
                    }
                }

                //玩家点击到的方块必须是电路板方块
                if(state.getBlock() instanceof CircuitBlock) {
                    //这是block的cache
                    BlockPos posCache = hit.getBlockPos();
                    ComponentSlotPos hitPosCache = ((CircuitBlock)state.getBlock()).getHitPos(state,minecraft.player.level,posCache,minecraft.player);

                    if (step % 2 == 0) {
                        step++;//这个语句用于防止重复触发
                        return;
                    } else {
                            //这个状态下将会保存第一顶点
                        if (step == 1 && posCache != null && hitPosCache != null) {
                            firstPos = posCache;
                            firstHitPos = hitPosCache;

                            //用粒子效果标注位置
                            pdFirst.loadLocation(firstPos,firstHitPos);
                            pdFirst.on();

                            minecraft.player.displayClientMessage(new TranslatableComponent("msg." + SuperCircuitMaker.MODID + ".blueprint.save.pos1"), true);
                            step++;
                            return;
                        }
                            //这个状态下会保存第二顶点
                        if (step == 3 && posCache != null && hitPosCache != null) {
                            secondPos = posCache;
                            secondHitPos = hitPosCache;
                            //面积太大
                            if(Math.abs(((Vec3i) firstPos).getX()-((Vec3i) secondPos).getX())>MAX_BLUEPRINT_SIZE-1 || Math.abs(((Vec3i) firstPos).getZ()-((Vec3i) secondPos).getZ())>MAX_BLUEPRINT_SIZE-1){
                                minecraft.player.displayClientMessage(new TranslatableComponent("msg." + SuperCircuitMaker.MODID + ".blueprint.save.size_too_large"), true);
                                error();
                                return;
                            }
                            //第一第二顶点方块坐标不在同一xz平面
                            if (((Vec3i) firstPos).getY() != ((Vec3i) secondPos).getY()) {
                                minecraft.player.displayClientMessage(new TranslatableComponent("msg." + SuperCircuitMaker.MODID + ".blueprint.save.dif_y_axis"), true);
                                error();
                                return;
                            }
                            //第一第二顶点坐标重合的情况
                            if (secondHitPos.toAbsolute().pos().getX() == firstHitPos.toAbsolute().pos().getX() && secondHitPos.toAbsolute().pos().getY() == firstHitPos.toAbsolute().pos().getY() && secondHitPos.toAbsolute().pos().getZ() == firstHitPos.toAbsolute().pos().getZ() && sameBlock()) {
                                minecraft.player.displayClientMessage(new TranslatableComponent("msg." + SuperCircuitMaker.MODID + ".blueprint.save.coincide"), true);
                                error();
                                return;
                            }
                            //成功选取
                            pbe.loadLocation(secondPos,secondHitPos);
                            pbe.on();

                            minecraft.player.displayClientMessage(new TranslatableComponent("msg." + SuperCircuitMaker.MODID + ".blueprint.save.pos2"), true);
                            step++;
                            return;
                        }
                    }
                }
            }

            //step自动归零
            if(step != 0) step = 0;

            if(pdFirst != null)pdFirst.stop();
            if(pbe != null)pbe.stop();
            pdFirst = null;
            pbe = null;

            if (partial) {
                result = InteractionResult.CONSUME_PARTIAL;
            } else {
                result = minecraft.player.isCrouching() ? InteractionResult.PASS : block.onClientUse(state, minecraft.level, hit.getBlockPos(), minecraft.player, event.getHand(), hit);
            }
            if (result == InteractionResult.PASS) {
                result = ComponentPlacementHandler.onClientUse(state, minecraft.level, hit.getBlockPos(), minecraft.player, event.getHand(), hit);
            }
            if (result == InteractionResult.CONSUME_PARTIAL) {
                partial = true;
                partialMapping = event.getKeyMapping();
                partialHand = event.getHand();
            }
        } else if (event.isAttack()) {
            result = block.onClientClicked(state, minecraft.level, hit.getBlockPos(), minecraft.player, event.getHand(), hit);
        } else if (event.isPickBlock()) {
            CircuitBlock.picking = true;
            return;
        } else {
            return;
        }
        if (result == InteractionResult.PASS) {
            return;
        }
        event.setCanceled(result.consumesAction());
        event.setSwingHand(result.shouldSwing());
        if (result.consumesAction() && result != InteractionResult.CONSUME_PARTIAL) {
            busyTimer = 5;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        CircuitBlock.picking = false;
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (busyTimer > 0) {
            busyTimer--;
        }

        if (!partial) {
            return;
        }
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        if (minecraft.hitResult instanceof BlockHitResult hit) {
            var state = Utils.resolveHit(minecraft.level, hit);
            if (state.getBlock() instanceof CircuitBlock && partialMapping.isDown()) {
                var result = ComponentPlacementHandler.onClientUse(state, minecraft.level, hit.getBlockPos(), minecraft.player, partialHand, hit);
                if (result != InteractionResult.CONSUME_PARTIAL) {
                    partial = false;
                    busyTimer = 5;
                }
                return;
            }
        }
        if (ComponentPlacementHandler.onClientStopUsing(minecraft.level, minecraft.player) || busyTimer > 0) {
            partial = false;
            busyTimer = 2;
        }
    }

    @SubscribeEvent
    public static void onDrawBlockHighlight(DrawSelectionEvent.HighlightBlock event) {
        var minecraft = Minecraft.getInstance();
        if (!(minecraft.hitResult instanceof BlockHitResult hit)) {
            return;
        }
        var state = Utils.resolveHit(minecraft.level, hit);
        if (!(state.getBlock() instanceof CircuitBlock)) {
            return;
        }

        if (ComponentPlacementHandler.onDrawBlockHighlight(minecraft.level, minecraft.player, event.getMultiBufferSource(), event.getPoseStack(), event.getPartialTicks())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientTickPalette(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            return;
        }
        var mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (SCMKeyMappings.OPEN_PALETTE.isDown() && mc.screen == null) {
            var stack = mc.player.getMainHandItem();
            if (!stack.isEmpty() && stack.is(SCMItems.PALETTE.get())) {
                mc.setScreen(new PaletteScreen(PaletteItem.getColor(stack)));
                return;
            }
            stack = mc.player.getOffhandItem();
            if (!stack.isEmpty() && stack.is(SCMItems.PALETTE.get())) {
                mc.setScreen(new PaletteScreen(PaletteItem.getColor(stack)));
            }
        }
    }

    @SubscribeEvent
    public static void onDrawPaletteOverlay(RenderGameOverlayEvent.Post event) {
        // TODO: Migrate to overlay?
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        var stack = mc.player.getMainHandItem();
        if (!stack.isEmpty() && stack.is(SCMItems.PALETTE.get())) {
            var poseStack = event.getMatrixStack();
            poseStack.pushPose();
            poseStack.translate(
                    mc.getWindow().getGuiScaledWidth() - PaletteScreen.TOTAL_SIZE * 0.75f - 24,
                    mc.getWindow().getGuiScaledHeight() - PaletteScreen.TOTAL_SIZE * 0.75f - 24,
                    0
            );
            poseStack.scale(0.75f, 0.75f, 1f);
            PaletteScreen.drawPalette(event.getMatrixStack(), PaletteItem.getColor(stack), 0);
            poseStack.popPose();
            return;
        }

        stack = mc.player.getOffhandItem();
        if (!stack.isEmpty() && stack.is(SCMItems.PALETTE.get())) {
            var poseStack = event.getMatrixStack();
            poseStack.pushPose();
            poseStack.translate(
                    24,
                    mc.getWindow().getGuiScaledHeight() - PaletteScreen.TOTAL_SIZE * 0.75f - 24,
                    0
            );
            poseStack.scale(0.75f, 0.75f, 1f);
            PaletteScreen.drawPalette(event.getMatrixStack(), PaletteItem.getColor(stack), 0);
            poseStack.popPose();
        }
    }

}
