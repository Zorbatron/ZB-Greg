package com.zorbatron.zbgt.common.metatileentities.multi.multiblockpart;

import java.math.BigInteger;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.zorbatron.zbgt.api.render.ZBGTTextures;
import com.zorbatron.zbgt.common.metatileentities.multi.MetaTileEntityYOTTank;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.*;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.fluids.util.AEFluidStack;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

public class MetaTileEntityYOTTankMEHatch extends MetaTileEntityMultiblockPart
                                          implements IGridProxyable, IActionHost, ICellContainer,
                                          IMEInventory<IAEFluidStack>, IMEInventoryHandler<IAEFluidStack> {

    private int priority;
    private AccessRestriction readMode;

    private boolean tickRateOverride;
    private int overriddenTickRate;

    private AENetworkProxy aeProxy;
    private int tickRate;
    private BigInteger lastAmount;
    private FluidStack lastFluid;

    public MetaTileEntityYOTTankMEHatch(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.priority = 0;
        this.readMode = AccessRestriction.READ_WRITE;
        this.tickRate = 20;
        this.lastAmount = BigInteger.ZERO;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityYOTTankMEHatch(metaTileEntityId, getTier());
    }

    @Override
    public void update() {
        super.update();

        if (getOffsetTimer() % (tickRateOverride ? overriddenTickRate : tickRate) == 0) {
            if (getController() instanceof MetaTileEntityYOTTank metaTileEntityYOTTank) {
                if (isChanged(metaTileEntityYOTTank)) {
                    notifyME();
                    faster();
                    updateLast(metaTileEntityYOTTank);
                } else {
                    slower();
                }
            }
        }
    }

    private void notifyME() {
        if (getProxy() != null) {
            if (getProxy().getNode() != null) {
                getProxy().getNode().getGrid().postEvent(new MENetworkCellArrayUpdate());
            }
        }
    }

    private boolean isChanged(MetaTileEntityYOTTank metaTileEntityYOTTank) {
        return !this.lastAmount.equals(metaTileEntityYOTTank.getStorageCurrent()) ||
                this.lastFluid != metaTileEntityYOTTank.getFluid();
    }

    private void updateLast(MetaTileEntityYOTTank metaTileEntityYOTTank) {
        if (metaTileEntityYOTTank != null) {
            this.lastAmount = metaTileEntityYOTTank.getStorageCurrent();
            this.lastFluid = metaTileEntityYOTTank.getFluid();
        } else {
            this.lastAmount = BigInteger.ZERO;
            this.lastFluid = null;
        }
    }

    private void faster() {
        if (this.tickRate > 15) {
            this.tickRate -= 5;
        }
    }

    private void slower() {
        if (this.tickRate < 100) {
            this.tickRate += 5;
        }
    }

    private void setReadMode(int readMode) {
        this.readMode = AccessRestriction.values()[readMode];
        notifyME();
        markDirty();
    }

    private int getReadMode() {
        return this.readMode.ordinal();
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 170, 95);
        builder.label(6, 6, getMetaFullName());

        builder.widget(new ImageCycleButtonWidget(6, 6 + 18, 18, 18, ZBGTTextures.AE2_RW_STATES, 4,
                this::getReadMode, this::setReadMode));

        return builder.build(getHolder(), entityPlayer);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);

        ZBGTTextures.YOTTANK_ME_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);

        if (this.getProxy() != null) {
            this.aeProxy.setValidSides(EnumSet.of(getFrontFacing()));
        }
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(getWorld(), getPos());
    }

    @Override
    public @NotNull AECableType getCableConnectionType(@NotNull AEPartLocation part) {
        if (part.getFacing() != this.frontFacing) {
            return AECableType.NONE;
        }

        return AECableType.SMART;
    }

    @Nullable
    private AENetworkProxy createProxy() {
        AENetworkProxy proxy = new AENetworkProxy(this, "mte_proxy", this.getStackForm(), true);
        proxy.setFlags(GridFlags.REQUIRE_CHANNEL);
        proxy.setIdlePowerUsage(ConfigHolder.compat.ae2.meHatchEnergyUsage);
        proxy.setValidSides(EnumSet.of(this.getFrontFacing()));
        return proxy;
    }

    @Override
    public @Nullable AENetworkProxy getProxy() {
        if (this.aeProxy == null) {
            return this.aeProxy = this.createProxy();
        }

        if (!this.aeProxy.isReady() && getWorld() != null) {
            this.aeProxy.onReady();
        }

        return this.aeProxy;
    }

    @Nullable
    @Override
    public IGridNode getGridNode(@NotNull AEPartLocation aePartLocation) {
        AENetworkProxy gp = getProxy();
        return gp != null ? gp.getNode() : null;
    }

    @Override
    public void securityBreak() {}

    @Override
    public void blinkCell(int i) {}

    @NotNull
    @Override
    public IGridNode getActionableNode() {
        return getProxy().getNode();
    }

    @Override
    public List<IMEInventoryHandler> getCellArray(IStorageChannel<?> iStorageChannel) {
        if (iStorageChannel == AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class)) {
            return Collections.singletonList(this);
        }
        return Collections.emptyList();
    }

    @Override
    public AccessRestriction getAccess() {
        return this.readMode;
    }

    @Override
    public boolean isPrioritized(IAEFluidStack iaeFluidStack) {
        return true;
    }

    @Override
    public boolean canAccept(IAEFluidStack iaeFluidStack) {
        return (fill(iaeFluidStack, false) > 0) &&
                !(readMode.equals(AccessRestriction.NO_ACCESS) || readMode.equals(AccessRestriction.WRITE));
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public boolean validForPass(int i) {
        return true;
    }

    @Override
    public void saveChanges(@Nullable ICellInventory<?> iCellInventory) {}

    private long fill(IAEFluidStack iaeFluidStack, boolean doFill) {
        if (!(getController() instanceof MetaTileEntityYOTTank controller)) return 0;
        if (!controller.isWorkingEnabled()) return 0;

        FluidStack controllerFluid = controller.getFluid();
        FluidStack controllerLockedFluid = controller.getLockedFluid();

        if (controllerLockedFluid != null && !controllerLockedFluid.isFluidEqual(iaeFluidStack.getFluidStack()))
            return 0;
        if (controllerFluid == null || controllerFluid.isFluidEqual(iaeFluidStack.getFluidStack())) {
            if (controllerFluid == null) {
                controller.setFluid(iaeFluidStack.getFluidStack().copy());
            }

            BigInteger controllerStorageCurrent = controller.getStorageCurrent();

            if (controller.addFluid(iaeFluidStack.getStackSize(), doFill)) {
                return iaeFluidStack.getStackSize();
            } else {
                final long returned;
                if (controller.isVoiding()) {
                    returned = iaeFluidStack.getStackSize();
                } else {
                    final BigInteger delta = controller.getStorage().subtract(controllerStorageCurrent);
                    returned = delta.longValueExact();
                }

                if (doFill) controller.setStorage(controllerStorageCurrent);
                return returned;
            }
        }

        return 0;
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack iaeFluidStack, Actionable actionable, IActionSource iActionSource) {
        if (readMode.equals(AccessRestriction.NO_ACCESS) || readMode.equals(AccessRestriction.READ))
            return iaeFluidStack;
        long amount = fill(iaeFluidStack, actionable.equals(Actionable.MODULATE));
        if (amount == 0) return iaeFluidStack;

        iaeFluidStack = iaeFluidStack.copy();
        iaeFluidStack.decStackSize(amount);

        if (iaeFluidStack.getStackSize() <= 0) return null;

        return iaeFluidStack;
    }

    private IAEFluidStack drain(IAEFluidStack iaeFluidStack, boolean doDrain) {
        if (!(getController() instanceof MetaTileEntityYOTTank controller)) return null;
        if (!controller.isWorkingEnabled()) return null;

        FluidStack controllerFluid = controller.getFluid();
        if (controllerFluid == null || !controllerFluid.isFluidEqual(iaeFluidStack.getFluidStack())) return null;

        long ready;
        if (controller.getStorageCurrent().compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) >= 0) {
            ready = Long.MAX_VALUE;
        } else {
            ready = controller.getStorageCurrent().longValueExact();
        }

        ready = Math.min(ready, iaeFluidStack.getStackSize());
        if (doDrain) controller.reduceFluid(ready);

        return AEFluidStack.fromFluidStack(controllerFluid).setStackSize(ready);
    }

    @Override
    public IAEFluidStack extractItems(IAEFluidStack iaeFluidStack, Actionable actionable, IActionSource iActionSource) {
        if (readMode.equals(AccessRestriction.NO_ACCESS) || readMode.equals(AccessRestriction.WRITE)) return null;
        IAEFluidStack ready = drain(iaeFluidStack, false);

        if (ready != null) {
            if (actionable.equals(Actionable.MODULATE)) drain(ready, true);
            return ready;
        } else return null;
    }

    @Override
    public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> iItemList) {
        if (readMode.equals(AccessRestriction.NO_ACCESS) || readMode.equals(AccessRestriction.WRITE)) return iItemList;
        if (!(getController() instanceof MetaTileEntityYOTTank controller)) return iItemList;
        if (!controller.isWorkingEnabled()) return iItemList;

        final BigInteger controllerCurrent = controller.getStorageCurrent();

        if (controller.getFluid() == null || controllerCurrent.signum() <= 0) return iItemList;

        long ready;
        if (controllerCurrent.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) >= 0) {
            ready = Long.MAX_VALUE;
        } else {
            ready = controllerCurrent.longValueExact();
        }

        iItemList.add(AEFluidStack.fromFluidStack(new FluidStack(controller.getFluid(), 1)).setStackSize(ready));
        return iItemList;
    }

    @Override
    public IStorageChannel<IAEFluidStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
    }

    public void shutdown() {
        if (getProxy() != null) {
            getProxy().getNode().getGrid().postEvent(new MENetworkCellArrayUpdate());
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);

        buf.writeInt(this.priority);
        buf.writeByte(this.readMode.ordinal());
        buf.writeInt(this.overriddenTickRate);
        buf.writeBoolean(this.tickRateOverride);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);

        this.priority = buf.readInt();
        this.readMode = AccessRestriction.values()[buf.readByte()];
        this.overriddenTickRate = buf.readInt();
        this.tickRateOverride = buf.readBoolean();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("Priority", this.priority);
        data.setInteger("ReadMode", this.readMode.ordinal());
        data.setInteger("OverriddenTickRate", this.overriddenTickRate);
        data.setBoolean("TickRateOverride", this.tickRateOverride);

        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        this.priority = data.getInteger("Priority");
        this.readMode = AccessRestriction.values()[data.getInteger("ReadMode")];
        this.overriddenTickRate = data.getInteger("OverriddenTickRate");
        this.tickRateOverride = data.getBoolean("TickRateOverride");
    }
}