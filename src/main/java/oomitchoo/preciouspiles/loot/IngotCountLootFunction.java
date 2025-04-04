package oomitchoo.preciouspiles.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import oomitchoo.preciouspiles.PreciousPiles;
import oomitchoo.preciouspiles.block.ModBlocks;
import oomitchoo.preciouspiles.block.StackedIngotsBlock;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public class IngotCountLootFunction implements LootItemFunction {
    private final boolean downFilled;
    private final boolean ingotsAtNe;
    private final boolean ingotsAtNw;
    private final boolean ingotsAtSe;
    private final boolean ingotsAtSw;

    public static final MapCodec<IngotCountLootFunction> CODEC =
            RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.BOOL.fieldOf("down_filled").forGetter(e -> e.downFilled),
                    Codec.BOOL.fieldOf("ingots_at_ne").forGetter(e -> e.ingotsAtNe),
                    Codec.BOOL.fieldOf("ingots_at_nw").forGetter(e -> e.ingotsAtNw),
                    Codec.BOOL.fieldOf("ingots_at_se").forGetter(e -> e.ingotsAtSe),
                    Codec.BOOL.fieldOf("ingots_at_sw").forGetter(e -> e.ingotsAtSw)
            ).apply(inst, IngotCountLootFunction::new));

    public IngotCountLootFunction(boolean downFilled, boolean ingotsAtNe, boolean ingotsAtNw, boolean ingotsAtSe, boolean ingotsAtSw) {
        this.downFilled = downFilled;
        this.ingotsAtNe = ingotsAtNe;
        this.ingotsAtNw = ingotsAtNw;
        this.ingotsAtSe = ingotsAtSe;
        this.ingotsAtSw = ingotsAtSw;
    }

    @Override
    public LootItemFunctionType<? extends LootItemFunction> getType() {
        return PreciousPiles.INGOT_COUNT_LOOT_FCT_TYPE.get();
    }

    @Override
    public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        int itemCount = 0;

        if (downFilled) itemCount = 32;
        if (ingotsAtNe) itemCount = itemCount + 8;
        if (ingotsAtNw) itemCount = itemCount + 8;
        if (ingotsAtSe) itemCount = itemCount + 8;
        if (ingotsAtSw) itemCount = itemCount + 8;

        itemStack.setCount(itemCount);
        return itemStack;
    }

    /*
    protected IngotCountLootFunction(List<LootItemCondition> predicates) {
        super(predicates);
    }

    @Override
    public LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
        return LootItemFunctions.SET_COUNT;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        int itemCount = 0;

        BlockState blockState = lootContext.getParameter(LootContextParams.BLOCK_STATE);
        if (blockState == null || (blockState.getBlock() != ModBlocks.IRON_INGOTS_STACKED_BLOCK.get())) return itemStack;

        if (blockState.getValue(StackedIngotsBlock.DOWN_FILLED)) itemCount = 32;
        if (blockState.getValue(StackedIngotsBlock.INGOTS_AT_NW)) itemCount = itemCount + 8;
        if (blockState.getValue(StackedIngotsBlock.INGOTS_AT_NE)) itemCount = itemCount + 8;
        if (blockState.getValue(StackedIngotsBlock.INGOTS_AT_SE)) itemCount = itemCount + 8;
        if (blockState.getValue(StackedIngotsBlock.INGOTS_AT_SW)) itemCount = itemCount + 8;

        itemStack.setCount(itemCount);
        return itemStack;
    }
     */
}
