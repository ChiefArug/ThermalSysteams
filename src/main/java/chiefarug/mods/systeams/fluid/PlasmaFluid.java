package chiefarug.mods.systeams.fluid;

import cofh.lib.util.DeferredRegisterCoFH;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class PlasmaFluid extends SteamFluid {
    public PlasmaFluid(@NotNull DeferredRegisterCoFH<Fluid> fluidRegister, @NotNull DeferredRegisterCoFH<FluidType> typeRegister, @Nullable DeferredRegisterCoFH<Block> blockRegister, @Nullable DeferredRegisterCoFH<Item> itemRegister, String id) {
        super(fluidRegister, typeRegister, blockRegister, null, id);
        bucket = itemRegister == null ? null : itemRegister.register(id + "_ball", () -> new PlasmaBucketItem(this::getStill, bucketItemProperties));
    }

    public static class PlasmaBucketItem extends BucketItem {
        public PlasmaBucketItem(Supplier<? extends Fluid> supplier, Properties builder) {
            super(supplier, builder);
        }

        @Override // holding a bucket of plasma is not a smart idea
        public void inventoryTick(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull Entity entity, int pSlotId, boolean pIsSelected) {
            if (entity instanceof Player player && player.getAbilities().invulnerable) return;

            entity.hurt(entity.level().damageSources().onFire(), 4); //TODO: custom damage type with death message
            entity.setSecondsOnFire(5);
            pStack.setCount(0);
        }

        @Override
        public ICapabilityProvider initCapabilities(@NotNull ItemStack stack, @Nullable CompoundTag nbt) {
            return new FluidBucketWrapper(stack);
        }
    }
}
