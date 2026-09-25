package io.github.nbcss.higherlogisticspatch.helper;

import com.simibubi.create.api.packager.InventoryIdentifier;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.RequestPromiseQueue;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Map;

/**
 * Save items arriving in a network-linked inventory to the network's promise queue only once
 */
public final class SharedArrivalBaselines {

    public record Key(ResourceKey<Level> dimension, InventoryIdentifier inventory) {}

    private SharedArrivalBaselines() {}

    public static void creditAndUpdate(RequestPromiseQueue queue, Key key, InventorySummary snapshot) {
        Map<Key, InventorySummary> baselines = ((ArrivalBaselineHolder) queue).hlp$arrivalBaselines();
        InventorySummary baseline = baselines.put(key, snapshot);
        if (baseline == null || queue.isEmpty())
            return;

        for (BigItemStack entry : snapshot.getStacks()) {
            int arrived = entry.count - baseline.getCountOf(entry.stack);
            if (arrived > 0)
                queue.itemEnteredSystem(entry.stack, arrived);
        }
    }
}
