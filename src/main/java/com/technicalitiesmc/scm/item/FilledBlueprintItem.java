package com.technicalitiesmc.scm.item;

import com.technicalitiesmc.lib.item.TKItem;
import com.technicalitiesmc.scm.SuperCircuitMaker;

public class FilledBlueprintItem extends TKItem {

    public FilledBlueprintItem() {
        super(new Properties().tab(SuperCircuitMaker.CREATIVE_TAB).stacksTo(1));
    }

}
