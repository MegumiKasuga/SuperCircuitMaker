package com.technicalitiesmc.scm.placement;

import com.technicalitiesmc.lib.circuit.component.ComponentSlot;
import com.technicalitiesmc.lib.circuit.component.ComponentState;
import com.technicalitiesmc.lib.circuit.component.ComponentType;
import com.technicalitiesmc.lib.circuit.placement.ComponentPlacement;
import com.technicalitiesmc.lib.circuit.placement.PlacementContext;
import com.technicalitiesmc.lib.math.VecDirection;
import com.technicalitiesmc.scm.block.CircuitBlock;
import com.technicalitiesmc.scm.circuit.client.ClientTile;
import com.technicalitiesmc.scm.network.ComponentPlacePacket;
import com.technicalitiesmc.scm.network.SCMNetworkHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import javax.annotation.Nullable;

import static com.technicalitiesmc.scm.circuit.CircuitHelper.HEIGHT;

public class ComponentPlacementHandler {

    private static final Capability<CircuitBlock.Data> DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    private static final Capability<ComponentPlacement> COMPONENT_PLACEMENT_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    private static final Capability<PlayerPlacementData> PLAYER_PLACEMENT_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public static InteractionResult onClientUse(BlockState state, ClientLevel level, BlockPos pos, LocalPlayer player, InteractionHand hand, BlockHitResult hit) {
        var hitPos = CircuitBlock.resolveHit(hit);
        if (hitPos == null) {
            return InteractionResult.PASS;
        }
        var data = player.getCapability(PLAYER_PLACEMENT_DATA_CAPABILITY).orElse(null);

        var placement = data.getPlacement();
        var context = data.getContext();
        if (placement == null) {
            var p = player.getItemInHand(hand).getCapability(COMPONENT_PLACEMENT_CAPABILITY).orElse(null);
            placement = p != null ? p.begin() : null;
            if (placement == null) {
                return InteractionResult.PASS;
            }

            var entity = level.getBlockEntity(pos);
            if (entity == null) {
                return InteractionResult.PASS;
            }
            var cbd = entity.getCapability(DATA_CAPABILITY).orElse(null);
            if (cbd == null) {
                return InteractionResult.PASS;
            }
            var accessor = cbd.getOrCreateAccessor();
            if (!(accessor instanceof ClientTile ct)) {
                return InteractionResult.PASS;
            }
            context = new SimpleClientContext(player, hand, ct);

            data.set(placement, context, pos, hand);
        }

        if (placement.tick(context, hitPos.toAbsolute().pos(), VecDirection.fromDirection(hit.getDirection()))) {
            return InteractionResult.CONSUME_PARTIAL; // Keep receiving events for as long as the placement wants to tick
        }

        // We're done with this placement
        data.reset();

        // If placement is not valid, abort
        if (!placement.isValid(context)) {
            return InteractionResult.PASS;
        }

        // If it's valid, send it to the server
        SCMNetworkHandler.sendToServer(new ComponentPlacePacket(pos, hand, placement));
        return InteractionResult.SUCCESS;
    }

    public static boolean onClientStopUsing(ClientLevel level, LocalPlayer player) {
        var data = player.getCapability(PLAYER_PLACEMENT_DATA_CAPABILITY).orElse(null);
        if (!data.isPlacing()) {
            return false;
        }

        var placement = data.getPlacement();
        var context = data.getContext();
        var pos = data.getPos();
        var hand = data.getHand();

        placement.stopPlacing(context);

        data.reset();

        // If placement is not valid, abort
        if (!placement.isValid(context)) {
            return false;
        }

        // If it's valid, send it to the server
        SCMNetworkHandler.sendToServer(new ComponentPlacePacket(pos, hand, placement));
        return true;
    }

    private static class SimpleClientContext implements PlacementContext.Client {

        private final Player player;
        private final InteractionHand hand;
        private final ClientTile tile;

        private SimpleClientContext(Player player, InteractionHand hand, ClientTile tile) {
            this.player = player;
            this.hand = hand;
            this.tile = tile;
        }

        @Override
        public Player getPlayer() {
            return player;
        }

        @Override
        public InteractionHand getHand() {
            return hand;
        }

        @Override
        public VecDirection getFacing() {
            return getHorizontalFacing();
        }

        @Override
        public VecDirection getHorizontalFacing() {
            return VecDirection.fromDirection(player.getDirection());
        }

        @Nullable
        @Override
        public ComponentState get(Vec3i pos, ComponentSlot slot) {
            if (pos.getY() < 0 || pos.getY() >= HEIGHT) {
                return null;
            }
            return tile.getState(pos, slot);
        }

        @Override
        public boolean canPlace(Vec3i pos, ComponentType type) {
            if (pos.getY() < 0 || pos.getY() >= HEIGHT) {
                return false;
            }
            return tile.canFit(pos, type);
        }

        @Override
        public boolean isTopSolid(Vec3i pos) {
            if (pos.getY() == -1) {
                return true;
            }
            var state = get(pos, ComponentSlot.SUPPORT);
            return state != null && state.isTopSolid();
        }

        @Override
        public boolean isWithinBounds(Vec3i pos) {
            return pos.getY() >= 0 && pos.getY() < HEIGHT;
        }

    }

}