package com.technicalitiesmc.scm.init;

import com.technicalitiesmc.lib.circuit.component.ClientComponent;
import com.technicalitiesmc.lib.circuit.component.ComponentSlot;
import com.technicalitiesmc.lib.circuit.component.ComponentType;
import com.technicalitiesmc.scm.SuperCircuitMaker;
import com.technicalitiesmc.scm.component.digital.*;
import com.technicalitiesmc.scm.component.misc.LevelIOComponent;
import com.technicalitiesmc.scm.component.misc.PlatformComponent;
import com.technicalitiesmc.scm.component.wire.ColoredWireComponent;
import com.technicalitiesmc.scm.component.wire.VerticalWireComponent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public final class SCMComponents {

    public static final DeferredRegister<ComponentType> REGISTRY = DeferredRegister.create(ComponentType.class, SuperCircuitMaker.MODID);

    public static final RegistryObject<ComponentType> TORCH_BOTTOM = register(
            "torch_bottom", TorchBottomComponent::new, TorchBottomComponent::createState,
            new TorchBottomComponent.Client(),
            ComponentSlot.DEFAULT, ComponentSlot.OVERLAY, ComponentSlot.SUPPORT
    );
    public static final RegistryObject<ComponentType> TORCH_TOP = register(
            "torch_top", TorchTopComponent::new,
            new TorchTopComponent.Client(),
            ComponentSlot.DEFAULT, ComponentSlot.OVERLAY
    );

    public static final RegistryObject<ComponentType> RANDOMIZER = register(
            "randomizer", RandomizerComponent::new, RandomizerComponent::createState,
            new RandomizerComponent.Client(),
            ComponentSlot.DEFAULT, ComponentSlot.OVERLAY
    );
    public static final RegistryObject<ComponentType> DELAY = register(
            "delay", DelayComponent::new, DelayComponent::createState,
            new DelayComponent.Client(),
            ComponentSlot.DEFAULT, ComponentSlot.OVERLAY
    );
    public static final RegistryObject<ComponentType> PULSAR = register(
            "pulsar", PulsarComponent::new, PulsarComponent::createState,
            new PulsarComponent.Client(),
            ComponentSlot.DEFAULT, ComponentSlot.OVERLAY
    );

    public static final RegistryObject<ComponentType> LAMP = register(
            "lamp", LampComponent::new, LampComponent::createState,
            new LampComponent.Client(),
            ComponentSlot.DEFAULT, ComponentSlot.OVERLAY
    );

    public static final RegistryObject<ComponentType> LEVER = register(
            "lever", LeverComponent::new, LeverComponent::createState,
            new LeverComponent.Client(),
            ComponentSlot.DEFAULT, ComponentSlot.OVERLAY
    );
    public static final RegistryObject<ComponentType> BUTTON = register(
            "button", ButtonComponent::new, ButtonComponent::createState,
            new ButtonComponent.Client(),
            ComponentSlot.DEFAULT, ComponentSlot.OVERLAY
    );

    public static final RegistryObject<ComponentType> PLATFORM = register(
            "platform", PlatformComponent::new,
            new PlatformComponent.Client(),
            ComponentSlot.SUPPORT
    );

    public static final RegistryObject<ComponentType> REDSTONE_WIRE = register(
            "redstone_wire", ColoredWireComponent::new, builder -> ColoredWireComponent.createState(builder),
            new ColoredWireComponent.Client(),
            ComponentSlot.DEFAULT
    );
    public static final RegistryObject<ComponentType> VERTICAL_WIRE = register(
            "vertical_wire", VerticalWireComponent::new,
            new VerticalWireComponent.Client(),
            ComponentSlot.OVERLAY
    );

    public static final RegistryObject<ComponentType> LEVEL_IO = register(
            "level_io", LevelIOComponent::new,
            ClientComponent.DEFAULT,
            ComponentSlot.DEFAULT
    );

    @Nonnull
    private static RegistryObject<ComponentType> register(
            String name,
            ComponentType.Factory factory,
            ClientComponent dummy,
            ComponentSlot slot,
            ComponentSlot... additionalSlots
    ) {
        return register(name, factory, b -> {}, dummy, slot, additionalSlots);
    }

    @Nonnull
    private static RegistryObject<ComponentType> register(
            String name,
            ComponentType.Factory factory,
            ComponentType.StateBuilder stateBuilder,
            ClientComponent dummy,
            ComponentSlot slot,
            ComponentSlot... additionalSlots
    ) {
        return REGISTRY.register(name, () -> new ComponentType(factory, stateBuilder, dummy, slot, additionalSlots));
    }

}