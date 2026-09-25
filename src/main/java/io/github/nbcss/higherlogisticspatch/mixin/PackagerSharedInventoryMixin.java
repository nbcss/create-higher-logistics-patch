package io.github.nbcss.higherlogisticspatch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.GlobalLogisticsManager;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlock;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.RequestPromiseQueue;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import io.github.nbcss.higherlogisticspatch.helper.SharedArrivalBaselines;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Bug Fix: https://github.com/Creators-of-Create/Create/issues/10634
 */
@Mixin(value = PackagerBlockEntity.class, remap = false)
public abstract class PackagerSharedInventoryMixin {

    @Shadow public InvManipulationBehaviour targetInventory;

    @Unique
    private boolean hlp$networkArrivalsHandled;

    @Inject(method = "submitNewArrivals", at = @At("HEAD"))
    private void hlp$creditNetworkArrivalsOncePerInventory(InventorySummary before, InventorySummary after,
                                                           CallbackInfo ci) {
        hlp$networkArrivalsHandled = false;
        PackagerBlockEntity self = (PackagerBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null || level.isClientSide)
            return;

        IdentifiedInventory inventory = targetInventory.getIdentifiedInventory();
        if (inventory == null || inventory.identifier() == null)
            return;
        hlp$networkArrivalsHandled = true;

        // Same stock link lookup as vanilla, but regardless of whether the queue currently holds promises
        Set<UUID> frequencies = new HashSet<>();
        BlockPos pos = self.getBlockPos();
        for (Direction d : Iterate.directions) {
            BlockPos adjacentPos = pos.relative(d);
            if (!level.isLoaded(adjacentPos))
                continue;
            BlockState adjacentState = level.getBlockState(adjacentPos);
            if (!AllBlocks.STOCK_LINK.has(adjacentState))
                continue;
            if (PackagerLinkBlock.getConnectedDirection(adjacentState) != d)
                continue;
            if (level.getBlockEntity(adjacentPos) instanceof PackagerLinkBlockEntity plbe)
                frequencies.add(plbe.behaviour.freqId);
        }
        if (frequencies.isEmpty())
            return;

        // Vanilla keeps and later mutates `after` as the next `before`, so share an untouched copy instead
        InventorySummary snapshot = after.copy();
        SharedArrivalBaselines.Key key = new SharedArrivalBaselines.Key(level.dimension(), inventory.identifier());
        for (UUID freqId : frequencies) {
            RequestPromiseQueue queue = Create.LOGISTICS.getQueuedPromises(freqId);
            if (queue != null)
                SharedArrivalBaselines.creditAndUpdate(queue, key, snapshot);
        }
    }

    @WrapOperation(
        method = "submitNewArrivals",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/logistics/packagerLink/GlobalLogisticsManager;"
                + "hasQueuedPromises(Ljava/util/UUID;)Z"))
    private boolean hlp$skipNetworkQueueWhenShared(GlobalLogisticsManager manager, UUID freqId,
                                                   Operation<Boolean> original) {
        return !hlp$networkArrivalsHandled && original.call(manager, freqId);
    }
}
