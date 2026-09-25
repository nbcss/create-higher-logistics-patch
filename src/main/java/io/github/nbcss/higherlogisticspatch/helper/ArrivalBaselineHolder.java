package io.github.nbcss.higherlogisticspatch.helper;

import com.simibubi.create.content.logistics.packager.InventorySummary;

import java.util.Map;

/**
 * Duck interface mixed into {@link com.simibubi.create.content.logistics.packagerLink.RequestPromiseQueue}.
 */
public interface ArrivalBaselineHolder {
    Map<SharedArrivalBaselines.Key, InventorySummary> hlp$arrivalBaselines();
}
