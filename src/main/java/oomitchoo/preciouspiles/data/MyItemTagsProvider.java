package oomitchoo.preciouspiles.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

import static oomitchoo.preciouspiles.common.event.IngotPlaceEvent.STACKABLE_TAG;

public class MyItemTagsProvider extends ItemTagsProvider {
    public MyItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagLookup<Block>> tagLookup)
    {
        super(output, lookupProvider, tagLookup, "preciouspiles");
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider)
    {
        this.tag(STACKABLE_TAG).add(Items.IRON_INGOT).replace(false);
    }
}
