package io.github.nbcss.higherlogisticspatch.mixin;

import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.RequestPromiseQueue;
import io.github.nbcss.higherlogisticspatch.helper.ArrivalBaselineHolder;
import io.github.nbcss.higherlogisticspatch.helper.SharedArrivalBaselines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores shared arrival baselines on network promise queues, see {@link PackagerSharedInventoryMixin}.
 */
@Mixin(value = RequestPromiseQueue.class, remap = false)
public class RequestPromiseQueueMixin implements ArrivalBaselineHolder {
    @Unique
    private Map<SharedArrivalBaselines.Key, InventorySummary> hlp$arrivalBaselines;

    @Override
    public Map<SharedArrivalBaselines.Key, InventorySummary> hlp$arrivalBaselines() {
        if (hlp$arrivalBaselines == null)
            hlp$arrivalBaselines = new HashMap<>();
        return hlp$arrivalBaselines;
    }
}
