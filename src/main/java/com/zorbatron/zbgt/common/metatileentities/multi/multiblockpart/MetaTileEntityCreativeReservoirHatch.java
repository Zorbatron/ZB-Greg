package com.zorbatron.zbgt.common.metatileentities.multi.multiblockpart;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.zorbatron.zbgt.client.ClientHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.impl.FilteredItemHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;

public class MetaTileEntityCreativeReservoirHatch extends MetaTileEntityMultiblockNotifiablePart implements
                                                  IMultiblockAbilityPart<IFluidTank> {

    private static final int FLUID_AMOUNT = Integer.MAX_VALUE;
    private FluidStack lockTank;
    private final InfiniteTank fluidTank;
    private boolean workingEnabled;

    public MetaTileEntityCreativeReservoirHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.MAX, false);
        this.fluidTank = new InfiniteTank(FLUID_AMOUNT, this);
        initializeInventory();
        this.workingEnabled = false;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCreativeReservoirHatch(metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createTankUI(fluidTank, getMetaFullName(), entityPlayer).build(getHolder(), entityPlayer);
    }

    public ModularUI.Builder createTankUI(IFluidTank fluidTank, String title, EntityPlayer entityPlayer) {
        // Create base builder/widget references
        ModularUI.Builder builder = ModularUI.defaultBuilder();

        TankWidget tankWidget = new PhantomTankWidget(fluidTank, 69, 52, 18, 18, () -> this.lockTank, (f) -> {
            // Runs when the tank empties and f ends up null??
            if (f != null) {
                this.lockTank = f.copy();
                this.fluidTank.setFluid(new FluidStack(f.copy(), FLUID_AMOUNT));
                this.fluidTank.onContentsChanged();
                setworkingEnabled(true);
            }
        })
                .setAlwaysShowFull(true).setDrawHoveringText(false).setContainerClicking(true, false);

        ClickButtonWidget clearButton = new ClickButtonWidget(8, 50, 35, 20, "Clear", (clickData) -> {
            this.lockTank = null;
            this.fluidTank.setFluid(null);
            setworkingEnabled(false);
        }).setTooltipText("zbgt.machine.creative_reservoir_hatch.clear.tooltip");

        builder.image(7, 16, 81, 55, GuiTextures.DISPLAY)
                .widget(new ImageWidget(91, 36, 14, 15, GuiTextures.TANK_ICON))
                .widget(new SlotWidget(exportItems, 0, 90, 53, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.OUT_SLOT_OVERLAY));

        // Add general widgets
        return builder.label(6, 6, title)
                .label(11, 20, "gregtech.gui.fluid_amount", 0xFFFFFF)
                .widget(new AdvancedTextWidget(11, 30, getFluidAmountText(tankWidget), 0xFFFFFF))
                .widget(new AdvancedTextWidget(11, 40, getFluidNameText(tankWidget), 0xFFFFFF))
                .widget(tankWidget)
                .widget(clearButton)
                .widget(new FluidContainerSlotWidget(importItems, 0, 90, 16, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY))
                .bindPlayerInventory(entityPlayer.inventory);
    }

    private Consumer<List<ITextComponent>> getFluidNameText(TankWidget tankWidget) {
        return (list) -> {
            TextComponentTranslation translation = tankWidget.getFluidTextComponent();
            if (translation != null) {
                list.add(translation);
            }
        };
    }

    private Consumer<List<ITextComponent>> getFluidAmountText(TankWidget tankWidget) {
        return (list) -> {
            String fluidAmount = tankWidget.getFormattedFluidAmount();
            if (!fluidAmount.isEmpty()) {
                list.add(new TextComponentString(fluidAmount));
            }
        };
    }

    @Override
    public MultiblockAbility<IFluidTank> getAbility() {
        return MultiblockAbility.IMPORT_FLUIDS;
    }

    @Override
    public void registerAbilities(List<IFluidTank> abilityList) {
        abilityList.add(fluidTank);
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            fillContainerFromInternalTank(fluidTank);
            if ((getOffsetTimer() % 20 == 0) & workingEnabled) {
                fluidTank.refill();
            }
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, @NotNull Matrix4 translation,
                                     IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            ClientHandler.WATER_OVERLAY_INFINITY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            // allow both importing and exporting from the tank
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidTank);
        }
        return super.getCapability(capability, side);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, fluidTank);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new FilteredItemHandler(this).setFillPredicate(
                FilteredItemHandler.getCapabilityFilter(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY));
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("gregtech.creative_tooltip.1") + TooltipHelper.RAINBOW +
                I18n.format("gregtech.creative_tooltip.2") + I18n.format("gregtech.creative_tooltip.3"));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        if (lockTank != null) {
            data.setTag("FluidInventory", lockTank.writeToNBT(new NBTTagCompound()));
        }
        data.setBoolean("isworkingEnabled", workingEnabled);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        if (data.hasKey("FluidInventory")) {
            this.lockTank = FluidStack.loadFluidStackFromNBT(data.getCompoundTag("FluidInventory"));
        }
        this.workingEnabled = data.getBoolean("isworkingEnabled");
        super.readFromNBT(data);
    }

    public void setworkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        if (!this.getWorld().isRemote) {
            this.writeCustomData(GregtechDataCodes.UPDATE_ACTIVE, (buf) -> buf.writeBoolean(workingEnabled));
        }
    }

    private class InfiniteTank extends NotifiableFluidTank {

        public InfiniteTank(int capacity, MetaTileEntity entityToNotify) {
            super(capacity, entityToNotify, false);
            // setFluid(new FluidStack(FluidRegistry.WATER, FLUID_AMOUNT));
            setFluid(null);
            setCanFill(false);
        }

        public void refill() {
            int fillAmount = Math.max(0, FLUID_AMOUNT - getFluidAmount());
            if (fillAmount > 0) {
                // call super since our overrides don't allow any kind of filling
                super.fillInternal(new FluidStack(lockTank.getFluid(), fillAmount), true);
            }
        }

        @Override
        public boolean canDrainFluidType(@Nullable FluidStack fluid) {
            return true;
        }

        // don't allow external filling
        @Override
        public int fillInternal(FluidStack resource, boolean doFill) {
            return 0;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return false;
        }

        @Override
        public void onContentsChanged() {
            super.onContentsChanged();
        }
    }
}
