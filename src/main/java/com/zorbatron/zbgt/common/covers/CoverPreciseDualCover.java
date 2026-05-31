package com.zorbatron.zbgt.common.covers;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;

import org.apache.logging.log4j.message.FormattedMessage;
import org.jetbrains.annotations.NotNull;

import com.google.common.math.IntMath;
import com.zorbatron.zbgt.client.widgets.HideableFluidFilterContainer;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.util.GTTransferUtils;
import gregtech.common.covers.CoverConveyor;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.TransferMode;
import gregtech.common.covers.filter.SmartItemFilter;
import gregtech.common.pipelike.itempipe.net.ItemNetHandler;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class CoverPreciseDualCover extends CoverDualCover {

    protected TransferMode itemTransferMode;
    protected int itemsTransferBuffered;

    protected TransferMode fluidTransferMode;
    protected int fluidTransferAmount = 0;

    public CoverPreciseDualCover(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                                 @NotNull EnumFacing attachedSide, int tier, int itemsPerSecond, int mbPerTick) {
        super(definition, coverableView, attachedSide, tier, itemsPerSecond, mbPerTick);

        this.itemTransferMode = TransferMode.TRANSFER_ANY;
        this.fluidTransferMode = TransferMode.TRANSFER_ANY;

        this.itemFilterContainer.setTransferStackSize(1);
        this.fluidFilterContainer = new HideableFluidFilterContainer(this, this::shouldShowTip,
                maxFluidTransferRate * 100);
    }

    @Override
    protected boolean shouldShowTip() {
        return fluidTransferMode != TransferMode.TRANSFER_ANY;
    }

    @Override
    public void setBucketMode(CoverPump.BucketMode bucketMode) {
        super.setBucketMode(bucketMode);
        if (this.bucketMode == CoverPump.BucketMode.BUCKET) {
            setFluidTransferAmount(fluidTransferAmount / 1000 * 1000);
        }
    }

    private void adjustFluidTransferSize(int amount) {
        if (bucketMode == CoverPump.BucketMode.BUCKET)
            amount *= 1000;
        switch (this.fluidTransferMode) {
            case TRANSFER_EXACT -> setFluidTransferAmount(
                    MathHelper.clamp(this.fluidTransferAmount + amount, 0, this.maxFluidTransferRate));
            case KEEP_EXACT -> setFluidTransferAmount(
                    MathHelper.clamp(this.fluidTransferAmount + amount, 0, Integer.MAX_VALUE));
        }
    }

    private void setFluidTransferAmount(int transferAmount) {
        this.fluidTransferAmount = transferAmount;
        markDirty();
    }

    @Override
    protected int doTransferFluidsInternal(IFluidHandler myFluidHandler, IFluidHandler fluidHandler,
                                           int transferLimit) {
        IFluidHandler sourceHandler;
        IFluidHandler destHandler;

        if (pumpMode == CoverPump.PumpMode.IMPORT) {
            sourceHandler = fluidHandler;
            destHandler = myFluidHandler;
        } else if (pumpMode == CoverPump.PumpMode.EXPORT) {
            sourceHandler = myFluidHandler;
            destHandler = fluidHandler;
        } else {
            return 0;
        }
        TransferMode mode = fluidTransferMode;
        if (mode == TransferMode.TRANSFER_ANY) {
            return GTTransferUtils.transferFluids(sourceHandler, destHandler, transferLimit,
                    fluidFilterContainer::testFluidStack);
        } else if (mode == TransferMode.KEEP_EXACT) {
            return doKeepExactFluids(transferLimit, sourceHandler, destHandler,
                    fluidFilterContainer::testFluidStack,
                    this.fluidTransferAmount);
        } else if (mode == TransferMode.TRANSFER_EXACT) {
            return doTransferExactFluids(transferLimit, sourceHandler, destHandler,
                    fluidFilterContainer::testFluidStack, this.fluidTransferAmount);
        }
        return 0;
    }

    protected int doTransferExactFluids(int transferLimit, IFluidHandler sourceHandler, IFluidHandler destHandler,
                                        Predicate<FluidStack> fluidFilter, int supplyAmount) {
        int fluidLeftToTransfer = transferLimit;
        for (IFluidTankProperties tankProperties : sourceHandler.getTankProperties()) {
            FluidStack sourceFluid = tankProperties.getContents();
            if (this.fluidFilterContainer.getFilterWrapper().getFluidFilter() != null &&
                    fluidTransferMode != TransferMode.TRANSFER_ANY) {
                supplyAmount = this.fluidFilterContainer.getFilterWrapper().getFluidFilter()
                        .getFluidTransferLimit(sourceFluid);
            }
            if (fluidLeftToTransfer < supplyAmount)
                break;
            if (sourceFluid == null || sourceFluid.amount == 0 || !fluidFilter.test(sourceFluid)) continue;
            sourceFluid.amount = supplyAmount;
            if (GTTransferUtils.transferExactFluidStack(sourceHandler, destHandler, sourceFluid.copy())) {
                fluidLeftToTransfer -= sourceFluid.amount;
            }
            if (fluidLeftToTransfer == 0) break;
        }
        return transferLimit - fluidLeftToTransfer;
    }

    protected int doKeepExactFluids(final int transferLimit,
                                    final IFluidHandler sourceHandler,
                                    final IFluidHandler destHandler,
                                    final Predicate<FluidStack> fluidFilter,
                                    int keepAmount) {
        if (sourceHandler == null || destHandler == null || fluidFilter == null)
            return 0;

        final Map<FluidStack, Integer> sourceFluids = collectDistinctFluids(sourceHandler,
                IFluidTankProperties::canDrain, fluidFilter);
        final Map<FluidStack, Integer> destFluids = collectDistinctFluids(destHandler, IFluidTankProperties::canFill,
                fluidFilter);

        int transferred = 0;
        for (FluidStack fluidStack : sourceFluids.keySet()) {
            if (transferred >= transferLimit)
                break;

            if (this.fluidFilterContainer.getFilterWrapper().getFluidFilter() != null &&
                    fluidTransferMode != TransferMode.TRANSFER_ANY) {
                keepAmount = this.fluidFilterContainer.getFilterWrapper().getFluidFilter()
                        .getFluidTransferLimit(fluidStack);
            }

            // if fluid needs to be moved to meet the Keep Exact value
            int amountInDest;
            if ((amountInDest = destFluids.getOrDefault(fluidStack, 0)) < keepAmount) {

                // move the lesser of the remaining transfer limit and the difference in actual vs keep exact amount
                int amountToMove = Math.min(transferLimit - transferred,
                        keepAmount - amountInDest);

                // Nothing to do here, try the next fluid.
                if (amountToMove <= 0)
                    continue;

                // Simulate a drain of this fluid from the source tanks
                FluidStack drainedResult = sourceHandler.drain(copyFluidStackWithAmount(fluidStack, amountToMove),
                        false);

                // Can't drain this fluid. Try the next one.
                if (drainedResult == null || drainedResult.amount <= 0 || !fluidStack.equals(drainedResult))
                    continue;

                // account for the possibility that the drain might give us less than requested
                final int drainable = Math.min(amountToMove, drainedResult.amount);

                // Simulate a fill of the drained amount
                int fillResult = destHandler.fill(copyFluidStackWithAmount(fluidStack, drainable), false);

                // Can't fill, try the next fluid.
                if (fillResult <= 0)
                    continue;

                // This Fluid can be drained and filled, so let's move the most that will actually work.
                int fluidToMove = Math.min(drainable, fillResult);
                FluidStack drainedActual = sourceHandler.drain(copyFluidStackWithAmount(fluidStack, fluidToMove), true);

                // Account for potential error states from the drain
                if (drainedActual == null)
                    throw new RuntimeException(
                            "Misbehaving fluid container: drain produced null after simulation succeeded");

                if (!fluidStack.equals(drainedActual))
                    throw new RuntimeException(
                            "Misbehaving fluid container: drain produced a different fluid than the simulation");

                if (drainedActual.amount != fluidToMove)
                    throw new RuntimeException(new FormattedMessage(
                            "Misbehaving fluid container: drain expected: {}, actual: {}",
                            fluidToMove,
                            drainedActual.amount).getFormattedMessage());

                // Perform Fill
                int filledActual = destHandler.fill(copyFluidStackWithAmount(fluidStack, fluidToMove), true);

                // Account for potential error states from the fill
                if (filledActual != fluidToMove)
                    throw new RuntimeException(new FormattedMessage(
                            "Misbehaving fluid container: fill expected: {}, actual: {}",
                            fluidToMove,
                            filledActual).getFormattedMessage());

                // update the transferred amount
                transferred += fluidToMove;
            }
        }

        return transferred;
    }

    private static FluidStack copyFluidStackWithAmount(FluidStack fs, int amount) {
        FluidStack fs2 = fs.copy();
        fs2.amount = amount;
        return fs2;
    }

    private static Map<FluidStack, Integer> collectDistinctFluids(IFluidHandler handler,
                                                                  Predicate<IFluidTankProperties> tankTypeFilter,
                                                                  Predicate<FluidStack> fluidTypeFilter) {
        final Map<FluidStack, Integer> summedFluids = new Object2IntOpenHashMap<>();
        Arrays.stream(handler.getTankProperties())
                .filter(tankTypeFilter)
                .map(IFluidTankProperties::getContents)
                .filter(Objects::nonNull)
                .filter(fluidTypeFilter)
                .forEach(fs -> {
                    summedFluids.putIfAbsent(fs, 0);
                    summedFluids.computeIfPresent(fs, (k, v) -> v + fs.amount);
                });

        return summedFluids;
    }

    @Override
    protected int doTransferItems(IItemHandler itemHandler, IItemHandler myItemHandler, int maxTransferAmount) {
        if (conveyorMode == CoverConveyor.ConveyorMode.EXPORT && itemHandler instanceof ItemNetHandler &&
                itemTransferMode == TransferMode.KEEP_EXACT) {
            return 0;
        }
        if (conveyorMode == CoverConveyor.ConveyorMode.IMPORT && myItemHandler instanceof ItemNetHandler &&
                itemTransferMode == TransferMode.KEEP_EXACT) {
            return 0;
        }
        TransferMode mode = itemTransferMode;
        if (mode == TransferMode.TRANSFER_ANY) {
            return doTransferItemsAny(itemHandler, myItemHandler, maxTransferAmount);
        } else if (mode == TransferMode.TRANSFER_EXACT) {
            return doTransferExactItems(itemHandler, myItemHandler, maxTransferAmount);
        } else if (mode == TransferMode.KEEP_EXACT) {
            return doKeepExactItems(itemHandler, myItemHandler, maxTransferAmount);
        }
        return 0;
    }

    protected int doTransferExactItems(IItemHandler itemHandler, IItemHandler myItemHandler, int maxTransferAmount) {
        Map<ItemStack, TypeItemInfo> sourceItemAmount = doCountSourceInventoryItemsByType(itemHandler, myItemHandler);
        Iterator<ItemStack> iterator = sourceItemAmount.keySet().iterator();
        while (iterator.hasNext()) {
            TypeItemInfo sourceInfo = sourceItemAmount.get(iterator.next());
            int itemAmount = sourceInfo.totalCount;
            int itemToMoveAmount = itemFilterContainer.getSlotTransferLimit(sourceInfo.filterSlot);

            // if smart item filter
            if (itemFilterContainer.getFilterWrapper().getItemFilter() instanceof SmartItemFilter) {
                if (itemFilterContainer.getTransferStackSize() > 1 && itemToMoveAmount * 2 <= itemAmount) {
                    // get the max we can extract from the item filter variable
                    int maxMultiplier = Math.floorDiv(maxTransferAmount, itemToMoveAmount);

                    // multiply up to the total count of all the items
                    itemToMoveAmount *= Math.min(itemFilterContainer.getTransferStackSize(), maxMultiplier);
                }
            }

            if (itemAmount >= itemToMoveAmount) {
                sourceInfo.totalCount = itemToMoveAmount;
            } else {
                iterator.remove();
            }
        }

        int itemsTransferred = 0;
        int maxTotalTransferAmount = maxTransferAmount + itemsTransferBuffered;
        boolean notEnoughTransferRate = false;
        for (TypeItemInfo itemInfo : sourceItemAmount.values()) {
            if (maxTotalTransferAmount >= itemInfo.totalCount) {
                boolean result = doTransferItemsExact(itemHandler, myItemHandler, itemInfo);
                itemsTransferred += result ? itemInfo.totalCount : 0;
                maxTotalTransferAmount -= result ? itemInfo.totalCount : 0;
            } else {
                notEnoughTransferRate = true;
            }
        }
        // if we didn't transfer anything because of too small transfer rate, buffer it
        if (itemsTransferred == 0 && notEnoughTransferRate) {
            itemsTransferBuffered += maxTransferAmount;
        } else {
            // otherwise, if transfer succeed, empty transfer buffer value
            itemsTransferBuffered = 0;
        }
        return Math.min(itemsTransferred, maxTransferAmount);
    }

    protected int doKeepExactItems(IItemHandler itemHandler, IItemHandler myItemHandler, int maxTransferAmount) {
        Map<Object, GroupItemInfo> currentItemAmount = doCountDestinationInventoryItemsByMatchIndex(itemHandler,
                myItemHandler);
        Map<Object, GroupItemInfo> sourceItemAmounts = doCountDestinationInventoryItemsByMatchIndex(myItemHandler,
                itemHandler);
        Iterator<Object> iterator = sourceItemAmounts.keySet().iterator();
        while (iterator.hasNext()) {
            Object filterSlotIndex = iterator.next();
            GroupItemInfo sourceInfo = sourceItemAmounts.get(filterSlotIndex);
            int itemToKeepAmount = itemFilterContainer.getSlotTransferLimit(sourceInfo.filterSlot);

            // only run multiplier for smart item
            if (itemFilterContainer.getFilterWrapper().getItemFilter() instanceof SmartItemFilter) {
                if (itemFilterContainer.getTransferStackSize() > 1 && itemToKeepAmount * 2 <= sourceInfo.totalCount) {
                    // get the max we can keep from the item filter variable
                    int maxMultiplier = Math.floorDiv(sourceInfo.totalCount, itemToKeepAmount);

                    // multiply up to the total count of all the items
                    itemToKeepAmount *= Math.min(itemFilterContainer.getTransferStackSize(), maxMultiplier);
                }
            }

            int itemAmount = 0;
            if (currentItemAmount.containsKey(filterSlotIndex)) {
                GroupItemInfo destItemInfo = currentItemAmount.get(filterSlotIndex);
                itemAmount = destItemInfo.totalCount;
            }
            if (itemAmount < itemToKeepAmount) {
                sourceInfo.totalCount = itemToKeepAmount - itemAmount;
            } else {
                iterator.remove();
            }
        }
        return doTransferItemsByGroup(itemHandler, myItemHandler, sourceItemAmounts, maxTransferAmount);
    }

    public void setItemTransferMode(TransferMode transferMode) {
        this.itemTransferMode = transferMode;
        this.getCoverableView().markDirty();
        this.itemFilterContainer.setMaxStackSize(transferMode.maxStackSize);
    }

    public TransferMode getItemTransferMode() {
        return itemTransferMode;
    }

    public void setFluidTransferMode(TransferMode transferMode) {
        this.fluidTransferMode = transferMode;
        this.markDirty();
    }

    public TransferMode getFLuidTransferMode() {
        return fluidTransferMode;
    }

    private boolean shouldDisplayItemAmountSlider() {
        if (itemTransferMode == TransferMode.TRANSFER_ANY) {
            return false;
        }
        return itemFilterContainer.showGlobalTransferLimitSlider();
    }

    private boolean shouldDisplayFluidAmountSlider() {
        if (this.fluidFilterContainer.getFilterWrapper().getFluidFilter() != null) {
            return false;
        }
        return this.fluidTransferMode == TransferMode.TRANSFER_EXACT ||
                this.fluidTransferMode == TransferMode.KEEP_EXACT;
    }

    public String getFluidTransferAmountString() {
        return Integer.toString(
                this.bucketMode == CoverPump.BucketMode.BUCKET ? fluidTransferAmount / 1000 : fluidTransferAmount);
    }

    @Override
    protected String getUITitle() {
        return "cover.precise_dual_cover.title";
    }

    @Override
    protected ModularUI buildUI(ModularUI.Builder builder, EntityPlayer player) {
        WidgetGroup primaryGroup = new WidgetGroup();

        ServerWidgetGroup itemWidgetGroup = new ServerWidgetGroup(() -> dualMode == DualMode.ITEM);
        ServerWidgetGroup fluidWidgetGroup = new ServerWidgetGroup(() -> dualMode == DualMode.FLUID);

        itemWidgetGroup.addWidget(new CycleButtonWidget(91, 45, 75, 20,
                TransferMode.class, this::getItemTransferMode, this::setItemTransferMode)
                        .setTooltipHoverString("cover.robotic_arm.transfer_mode.description"));

        ServerWidgetGroup stackSizeGroupItems = new ServerWidgetGroup(this::shouldDisplayItemAmountSlider);
        stackSizeGroupItems.addWidget(new ImageWidget(111, 70, 35, 20, GuiTextures.DISPLAY));

        stackSizeGroupItems.addWidget(
                new IncrementButtonWidget(146, 70, 20, 20, 1, 8, 64, 512, itemFilterContainer::adjustTransferStackSize)
                        .setDefaultTooltip()
                        .setTextScale(0.7f)
                        .setShouldClientCallback(false));
        stackSizeGroupItems.addWidget(new IncrementButtonWidget(91, 70, 20, 20, -1, -8, -64, -512,
                itemFilterContainer::adjustTransferStackSize)
                        .setDefaultTooltip()
                        .setTextScale(0.7f)
                        .setShouldClientCallback(false));

        stackSizeGroupItems.addWidget(new TextFieldWidget2(113, 77, 31, 20,
                () -> String.valueOf(itemFilterContainer.getTransferStackSize()), val -> {
                    if (val != null && !val.isEmpty())
                        itemFilterContainer.setTransferStackSize(
                                MathHelper.clamp(Integer.parseInt(val), 1, itemTransferMode.maxStackSize));
                })
                        .setNumbersOnly(1, itemTransferMode.maxStackSize)
                        .setMaxLength(4)
                        .setScale(0.9f));

        itemWidgetGroup.addWidget(stackSizeGroupItems);

        ServerWidgetGroup stackSizeGroupFluids = new ServerWidgetGroup(this::shouldDisplayFluidAmountSlider);
        stackSizeGroupFluids.addWidget(new ImageWidget(110, 64, 38, 18, GuiTextures.DISPLAY));

        fluidWidgetGroup.addWidget(new CycleButtonWidget(92, 43, 75, 18,
                TransferMode.class, this::getFLuidTransferMode, this::setFluidTransferMode)
                        .setTooltipHoverString("cover.fluid_regulator.transfer_mode.description"));

        stackSizeGroupFluids
                .addWidget(new IncrementButtonWidget(148, 64, 18, 18, 1, 10, 100, 1000, this::adjustFluidTransferSize)
                        .setDefaultTooltip()
                        .setTextScale(0.7f)
                        .setShouldClientCallback(false));
        stackSizeGroupFluids
                .addWidget(
                        new IncrementButtonWidget(92, 64, 18, 18, -1, -10, -100, -1000, this::adjustFluidTransferSize)
                                .setDefaultTooltip()
                                .setTextScale(0.7f)
                                .setShouldClientCallback(false));

        stackSizeGroupFluids
                .addWidget(new TextFieldWidget2(111, 70, 36, 11, this::getFluidTransferAmountString, val -> {
                    if (val != null && !val.isEmpty()) {
                        int amount = Integer.parseInt(val);
                        if (this.bucketMode == CoverPump.BucketMode.BUCKET) {
                            amount = IntMath.saturatedMultiply(amount, 1000);
                        }
                        setFluidTransferAmount(amount);
                    }
                })
                        .setCentered(true)
                        .setNumbersOnly(1,
                                fluidTransferMode == TransferMode.TRANSFER_EXACT ? maxFluidTransferRate :
                                        Integer.MAX_VALUE)
                        .setMaxLength(10)
                        .setScale(0.6f));

        stackSizeGroupFluids
                .addWidget(new SimpleTextWidget(129, 78, "", 0xFFFFFF, () -> bucketMode.localeName).setScale(0.6f));

        fluidWidgetGroup.addWidget(stackSizeGroupFluids);

        primaryGroup.addWidget(itemWidgetGroup);
        primaryGroup.addWidget(fluidWidgetGroup);

        return super.buildUI(builder.widget(primaryGroup), player);
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);

        tagCompound.setInteger("ItemTransferMode", this.itemTransferMode.ordinal());

        tagCompound.setInteger("FluidTransferMode", this.fluidTransferMode.ordinal());
        tagCompound.setInteger("FluidTransferAmount", this.fluidTransferAmount);
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);

        this.itemTransferMode = TransferMode.values()[tagCompound.getInteger("ItemTransferMode")];

        this.fluidTransferMode = TransferMode.values()[tagCompound.getInteger("FluidTransferMode")];
        this.fluidTransferAmount = tagCompound.getInteger("FluidTransferAmount");
    }
}
