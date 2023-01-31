package com.technicalitiesmc.scm.client;

import com.technicalitiesmc.lib.util.Utils;
import com.technicalitiesmc.scm.SuperCircuitMaker;
import com.technicalitiesmc.scm.block.CircuitBlock;
import com.technicalitiesmc.scm.circuit.util.ComponentSlotPos;
import com.technicalitiesmc.scm.client.screen.PaletteScreen;
import com.technicalitiesmc.scm.init.SCMItems;
import com.technicalitiesmc.scm.item.PaletteItem;
import com.technicalitiesmc.scm.placement.ComponentPlacementHandler;
import net.minecraft.BlockUtil;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.extensions.IForgeBlockGetter;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.swing.text.html.parser.Entity;

import static com.technicalitiesmc.scm.circuit.CircuitHelper.MAX_BLUEPRINT_SIZE;
import static com.technicalitiesmc.scm.circuit.CircuitHelper.getPositionFromIndex;

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

        InteractionResult result;
        if (event.isUseItem()) {
            //判断玩家手中的物品是不是蓝图
            if(minecraft.player.getMainHandItem().is(SCMItems.BLUEPRINT.get())){

                //如果此时玩家处于潜行状态，那么将认为玩家将要取消保存过程
                if(minecraft.player.isCrouching() && step != 0){
                    minecraft.player.displayClientMessage(new TranslatableComponent("msg." + SuperCircuitMaker.MODID + ".blueprint.save.cancelled"), true);
                    firstPos = null;
                    firstHitPos = null;
                    secondPos = null;
                    secondHitPos = null;
                    step = 0;
                    return;
                }

                if (step == 5 && firstPos != null && secondPos != null && firstHitPos != null && secondHitPos != null) {
                    int cache = 0;

                    //以下操作让firstPos的x和z轴坐标都小于secondPos的对应数值
                    if(((Vec3i) firstPos).getX() > ((Vec3i) secondPos).getX()){
                        cache = ((Vec3i) firstPos).getX();
                        firstPos = new BlockPos(new Vec3i(((Vec3i) secondPos).getX(),((Vec3i) firstPos).getY(),((Vec3i) firstPos).getZ()));
                        secondPos = new BlockPos(cache,((Vec3i) secondPos).getY(),((Vec3i) secondPos).getZ());
                    }
                    if(((Vec3i) firstPos).getZ() > ((Vec3i) secondPos).getZ()){
                        cache = ((Vec3i) firstPos).getZ();
                        firstPos = new BlockPos(new Vec3i(((Vec3i) firstPos).getX(),((Vec3i) firstPos).getY(),((Vec3i) secondPos).getZ()));
                        secondPos = new BlockPos(new Vec3i(((Vec3i) secondPos).getX(),((Vec3i) secondPos).getY(),cache));
                    }
                    try{
                        //Block scanner = getBlockByPos();

                    }catch (Exception e){e.printStackTrace();}

                    firstPos = null;
                    firstHitPos = null;
                    error();
                    step = 0;
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
                            if (secondHitPos.toAbsolute().pos().getX() == firstHitPos.toAbsolute().pos().getX() && secondHitPos.toAbsolute().pos().getY() == firstHitPos.toAbsolute().pos().getY() && secondHitPos.toAbsolute().pos().getZ() == firstHitPos.toAbsolute().pos().getZ() && ((Vec3i) firstPos).getX() == ((Vec3i) secondPos).getX() && ((Vec3i) firstPos).getZ() == ((Vec3i) secondPos).getZ()) {
                                minecraft.player.displayClientMessage(new TranslatableComponent("msg." + SuperCircuitMaker.MODID + ".blueprint.save.coincide"), true);
                                error();
                                return;
                            }
                            //成功选取
                            minecraft.player.displayClientMessage(new TranslatableComponent("msg." + SuperCircuitMaker.MODID + ".blueprint.save.pos2"), true);
                            step++;
                            return;
                        }
                        //文件夹存在与否的监测
                        //state.getBlock();
                        //BlockHitResult bhr = new BlockHitResult();

                        //要保存的方块
                        //CircuitBlock BlueprintBlock = (CircuitBlock) state.getBlock();
                        //BlueprintBlock.outputBlueprint(state, minecraft.player.level, hit.getBlockPos(), "name", "introduction", minecraft.player.getModelName());

                        //var pos = BlueprintBlock.getHitPos(state,minecraft.player.level,hit.getBlockPos(),minecraft.player);
                        //if(pos != null) System.out.println("place:"+pos.toAbsolute().pos().getX()+","+(pos.toAbsolute().pos().getY()-1)+","+pos.toAbsolute().pos().getZ());

                        //System.out.println(ci.getType().toString());
                    }
                }
            }

            if(step != 0) step = 0;

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
