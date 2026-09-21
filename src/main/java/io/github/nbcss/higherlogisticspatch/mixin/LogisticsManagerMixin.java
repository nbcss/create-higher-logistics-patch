package io.github.nbcss.higherlogisticspatch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagingRequest;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.createmod.catnip.data.Pair;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Bug Fix: https://github.com/Creators-of-Create/Create/issues/10054
 */
@Mixin(value = LogisticsManager.class, remap = false)
public abstract class LogisticsManagerMixin {
    @Unique
    private static final ThreadLocal<Map<PackagerBlockEntity, InventorySummary>> hlp$ledger = new ThreadLocal<>();

    @Inject(method = "findPackagersForRequest", at = @At("HEAD"))
    private static void hlp$openLedger(java.util.UUID freqId, PackageOrderWithCrafts order,
                                       IdentifiedInventory ignoredHandler, String address,
                                       CallbackInfoReturnable<?> cir) {
        hlp$ledger.set(new IdentityHashMap<>());
    }

    @Inject(method = "findPackagersForRequest", at = @At("RETURN"))
    private static void hlp$closeLedger(java.util.UUID freqId, PackageOrderWithCrafts order,
                                        IdentifiedInventory ignoredHandler, String address,
                                        CallbackInfoReturnable<?> cir) {
        hlp$ledger.remove();
    }

    @WrapOperation(
        method = "findPackagersForRequest",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/logistics/packagerLink/LogisticallyLinkedBehaviour;"
                + "processRequest(Lnet/minecraft/world/item/ItemStack;ILjava/lang/String;I"
                + "Lorg/apache/commons/lang3/mutable/MutableBoolean;I"
                + "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrderWithCrafts;"
                + "Lcom/simibubi/create/content/logistics/packager/IdentifiedInventory;)"
                + "Lnet/createmod/catnip/data/Pair;"))
    private static Pair<PackagerBlockEntity, PackagingRequest> hlp$capByCommitted(
            LogisticallyLinkedBehaviour link, ItemStack stack, int amount, String address, int linkIndex,
            MutableBoolean finalLink, int orderId, PackageOrderWithCrafts context, IdentifiedInventory ignoredHandler,
            Operation<Pair<PackagerBlockEntity, PackagingRequest>> original) {

        Pair<PackagerBlockEntity, PackagingRequest> req =
            original.call(link, stack, amount, address, linkIndex, finalLink, orderId, context, ignoredHandler);
        if (req == null)
            return null;

        Map<PackagerBlockEntity, InventorySummary> ledger = hlp$ledger.get();
        if (ledger == null)
            return req;

        PackagerBlockEntity packager = req.getFirst();
        InventorySummary committed = ledger.computeIfAbsent(packager, p -> new InventorySummary());

        int rawCount = req.getSecond().getCount();                        // = min(amount, packagerAvail)
        int packagerAvail = packager.getAvailableItems().getCountOf(stack); // same value processRequest saw
        int remaining = packagerAvail - committed.getCountOf(stack);
        int effective = Math.min(rawCount, remaining);

        if (effective <= 0)
            return null;

        committed.add(stack, effective);
        if (effective < rawCount)
            req.getSecond().subtract(rawCount - effective); // MutableInt count, shrink in place
        return req;
    }
}
