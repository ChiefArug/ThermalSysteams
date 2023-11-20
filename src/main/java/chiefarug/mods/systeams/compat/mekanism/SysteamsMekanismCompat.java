package chiefarug.mods.systeams.compat.mekanism;

import chiefarug.mods.systeams.SysteamsRegistry;
import cofh.lib.fluid.FluidStorageCoFH;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.common.registries.MekanismGases;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import mekanism.api.chemical.gas.IGasHandler;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import static chiefarug.mods.systeams.Systeams.LGGR;
import static chiefarug.mods.systeams.compat.mekanism.SysteamsMekanismCompat.LiquidToGasHandler.fluidConversions;

public class SysteamsMekanismCompat {

	public static void activateMechanisedManifold(IEventBus bus) {
		bus.addListener((FMLCommonSetupEvent event) -> {
			fluidConversions.put(Fluids.EMPTY, MekanismAPI.EMPTY_GAS);
			fluidConversions.put(SysteamsRegistry.Fluids.STEAM.getStill(), MekanismGases.STEAM.get());
		});
	}

	public static LazyOptional<IGasHandler> wrapLiquidCapability(LazyOptional<IFluidHandler> fluidHandler) {
		if (fluidHandler.isPresent())
			//noinspection OptionalGetWithoutIsPresent
			return LazyOptional.of(() -> new LiquidToGasHandler(fluidHandler.resolve().get()));
		return LazyOptional.empty();
	}

	static class LiquidToGasHandler implements IGasHandler {

		public static final BiMap<Fluid, Gas> fluidConversions = HashBiMap.create();
		private final IFluidHandler fluidHandler;

		public LiquidToGasHandler(IFluidHandler fluidHandler) {
			this.fluidHandler = fluidHandler;
		}

		public static FluidStack gasToFluid(GasStack gas) {
			if (gas.isEmpty()) return FluidStack.EMPTY;
			Fluid fluid = fluidConversions.inverse().get(gas.getType());
			if (fluid == null) return FluidStack.EMPTY;
			if (gas.getAmount() > Integer.MAX_VALUE)
				LGGR.error("Systeams converted a massive amount of gas {} to fluid {}. {}mb has been voided!", gas.getTypeRegistryName(), ForgeRegistries.FLUIDS.getKey(fluid), gas.getAmount() - Integer.MAX_VALUE);
			return new FluidStack(fluid, (int) gas.getAmount());
		}

		public static Pair<FluidStack, Long> gasToFluidWithOverflow(GasStack gas) {
			if (gas.isEmpty()) return Pair.of(FluidStack.EMPTY, 0L);
			Fluid fluid = fluidConversions.inverse().get(gas.getType());
			if (fluid == null) return Pair.of(FluidStack.EMPTY, gas.getAmount());
			if (gas.getAmount() > Integer.MAX_VALUE)
				return Pair.of(new FluidStack(fluid, (int) gas.getAmount()), gas.getAmount() - Integer.MAX_VALUE);
			return Pair.of(new FluidStack(fluid, (int) gas.getAmount()), 0L);
		}

		public static GasStack fluidToGas(FluidStack fluid) {
			if (fluid.isEmpty()) return GasStack.EMPTY;
			Gas gas = fluidConversions.get(fluid.getFluid());
			if (gas == null) return GasStack.EMPTY;
			return gas.getStack(fluid.getAmount());
		}

		/**
		 * Returns the number of chemical storage units ("tanks") available
		 *
		 * @return The number of tanks available
		 */
		@Override
		public int getTanks() {
			return fluidHandler.getTanks();
		}

		/**
		 * Returns the {@link GasStack} in a given tank.
		 *
		 * <p>
		 * <strong>IMPORTANT:</strong> This {@link GasStack} <em>MUST NOT</em> be modified. This method is not for altering internal contents. Any implementers who are
		 * able to detect modification via this method should throw an exception. It is ENTIRELY reasonable and likely that the stack returned here will be a copy.
		 * </p>
		 *
		 * <p>
		 * <strong><em>SERIOUSLY: DO NOT MODIFY THE RETURNED CHEMICAL GasStack</em></strong>
		 * </p>
		 *
		 * @param tank Tank to query.
		 * @return {@link GasStack} in a given tank. {@link #getEmptyStack()} if the tank is empty.
		 */
		@NotNull
		@Override
		public GasStack getChemicalInTank(int tank) {
			return fluidToGas(fluidHandler.getFluidInTank(tank));
		}

		/**
		 * Overrides the stack in the given tank. This method may throw an error if it is called unexpectedly.
		 *
		 * @param tank Tank to modify
		 * @param gas  {@link GasStack} to set tank to (may be empty).
		 * @throws RuntimeException if the handler is called in a way that the handler was not expecting.
		 **/
		@Override
		public void setChemicalInTank(int tank, @NotNull GasStack gas) {
			if (fluidHandler instanceof FluidStorageCoFH storage) {
				storage.setFluidStack(gasToFluid(gas));
			} else {
				fluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
				fluidHandler.fill(gasToFluid(gas), IFluidHandler.FluidAction.EXECUTE);
			}
		}

		/**
		 * Retrieves the maximum amount of chemical that can be stored in a given tank.
		 *
		 * @param tank Tank to query.
		 * @return The maximum chemical amount held by the tank.
		 */
		@Override
		public long getTankCapacity(int tank) {
			return fluidHandler.getTankCapacity(tank);
		}

		/**
		 * <p>
		 * This function should be used instead of simulated insertions in cases where the contents and state of the tank are irrelevant, mainly for the purpose of automation
		 * and logic.
		 * </p>
		 * <ul>
		 * <li>isValid is false when insertion of the chemical type is never valid.</li>
		 * <li>When isValid is true, no assumptions can be made and insertion must be simulated case-by-case.</li>
		 * <li>The actual chemical in the tank, its fullness, or any other state are <strong>not</strong> considered by isValid.</li>
		 * </ul>
		 *
		 * @param tank  Tank to query.
		 * @param stack Stack to test with for validity
		 * @return true if the tank can accept the {@link GasStack}, not considering the current state of the tank. false if the tank can never support the given {@link GasStack}
		 * in any situation.
		 */
		@Override
		public boolean isValid(int tank, @NotNull GasStack stack) {
			return fluidHandler.isFluidValid(tank, gasToFluid(stack));
		}

		/**
		 * <p>
		 * Inserts a {@link GasStack} into a given tank and return the remainder. The {@link GasStack} <em>should not</em> be modified in this function!
		 * </p>
		 * Note: This behaviour is subtly different from
		 * {@link IFluidHandler#fill(FluidStack,
		 * IFluidHandler.FluidAction)}
		 *
		 * @param tank   Tank to insert to.
		 * @param stack  {@link GasStack} to insert. This must not be modified by the tank.
		 * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
		 * @return The remaining {@link GasStack} that was not inserted (if the entire stack is accepted, then return an empty {@link GasStack}). May be the same as the input
		 * {@link GasStack} if unchanged, otherwise a new {@link GasStack}. The returned {@link GasStack} can be safely modified after
		 */
		@NotNull
		@Override
		public GasStack insertChemical(int tank, @NotNull GasStack stack, Action action) {
			Pair<FluidStack, Long> fluid = gasToFluidWithOverflow(stack);
			int leftover = fluidHandler.fill(fluid.getFirst(), action.toFluidAction());
			if (fluid.getSecond() == 0 && leftover == 0) return GasStack.EMPTY;
			stack.setAmount(fluid.getSecond() + leftover);
			return stack;
		}

		/**
		 * Extracts a {@link GasStack} from a specific tank in this handler.
		 * <p>
		 * The returned value must be empty if nothing is extracted, otherwise its stack size must be less than or equal to {@code amount}.
		 * </p>
		 *
		 * @param tank   Tank to extract from.
		 * @param amount Amount to extract (may be greater than the current stack's amount or the tank's capacity)
		 * @param action The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
		 * @return {@link GasStack} extracted from the tank, must be empty if nothing can be extracted. The returned {@link GasStack} can be safely modified after, so the tank
		 * should return a new or copied stack.
		 */
		@NotNull
		@Override
		public GasStack extractChemical(int tank, long amount, Action action) {
			return fluidToGas(fluidHandler.drain((int) amount, action.toFluidAction()));
		}
	}
}
