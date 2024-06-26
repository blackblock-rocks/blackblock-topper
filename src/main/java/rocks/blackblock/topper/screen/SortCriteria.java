package rocks.blackblock.topper.screen;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.util.StringIdentifiable;
import rocks.blackblock.screenbuilder.BBSB;
import rocks.blackblock.screenbuilder.textures.IconTexture;

import java.util.*;

public enum SortCriteria implements StringIdentifiable {

    DEFAULT("Default", null, null),
    ALPHABETICAL("Alphabetical", BBSB.SORT_ALPHABETICAL, null),
    OWNER("Owner", BBSB.SORT_OWNER, null),
    MINED("Times Mined", BBSB.SORT_MINED, null),
    BROKEN("Times Broken", BBSB.SORT_BROKEN, Stats.BROKEN),
    CRAFTED("Times Crafted", BBSB.SORT_CRAFTED, Stats.CRAFTED),
    USED("Times Used", BBSB.SORT_USED, Stats.USED),
    PICKED_UP("Picked Up", BBSB.SORT_PICKED_UP, Stats.PICKED_UP),
    DROPPED("Dropped", BBSB.SORT_DROPPED, Stats.DROPPED);

    // Store all values in order
    public static final SortCriteria[] values = new SortCriteria[]{
        DEFAULT, ALPHABETICAL, OWNER, MINED, CRAFTED, USED, BROKEN, PICKED_UP, DROPPED
    };

    private final String name;
    private final IconTexture icon;
    private final StatType<Item> statType;

    private SortCriteria(String name, IconTexture icon, StatType<Item> statType) {
        this.name = name; this.icon = icon; this.statType = statType;
    }
    public String toString() { return this.asString(); }
    public String asString() { return this.name; }
    public IconTexture getIcon() { return this.icon; }

    public SortCriteria next() { return values[(Arrays.asList(values).indexOf(this) + 1) % values.length]; }
    public SortCriteria prev() { return values[(Arrays.asList(values).indexOf(this) + values.length - 1) % values.length]; }

    public void sort(List<Item> items, SortOrder sortOrder, ServerPlayerEntity player) {
        // Alphabet sort
        if (this == SortCriteria.ALPHABETICAL) {
            items.sort(Comparator.comparing(item -> item.getName().getString().toLowerCase()));

        // Mined stat, being the only block stat, gets its own special part.
        } else if (this == SortCriteria.MINED) {
            items.sort(Comparator.comparing(item ->
                item instanceof BlockItem blockItem ? -player.getStatHandler().getStat(Stats.MINED, blockItem.getBlock()) : 1
            ));

        // For all other stats, attempt to sort by the stat.
        } else if (this.statType != null) {
            items.sort(Comparator.comparing(item -> -player.getStatHandler().getStat(this.statType, item)));
        }

        // Final reversal if we're doing ascending.
        if (sortOrder == SortOrder.ASCENDING)
            Collections.reverse(items);
    }

    public void sort(List<ItemStack> stacks, ServerPlayerEntity player, SortOrder sortOrder) {
        // Alphabet sort
        if (this == SortCriteria.ALPHABETICAL) {
            stacks.sort(Comparator.comparing(stack -> stack.getName().getString().toLowerCase()));

        // Owner sort. First, sort alphabetical, THEN by owner.
        } else if (this == SortCriteria.OWNER) {
            stacks.sort(Comparator.comparing(stack -> stack.getName().getString()));
            stacks.sort(Comparator.comparing(stack -> {
                String owner = stack.getComponents().get(DataComponentTypes.CUSTOM_DATA).getNbt().getString("custom_stat_owner");
                return owner.isEmpty() ? "zzzzzzzzzzzzzzzz" : owner;
            }));

        // Mined stat, being the only block stat, gets its own special part.
        } else if (this == SortCriteria.MINED) {
            stacks.sort(Comparator.comparing(stack ->
                    stack.getItem() instanceof BlockItem blockItem ? -player.getStatHandler().getStat(Stats.MINED, blockItem.getBlock()) : 1
            ));

        // For all other stats, attempt to sort by the stat.
        } else if (this.statType != null) {
            stacks.sort(Comparator.comparing(stack -> -player.getStatHandler().getStat(this.statType, stack.getItem())));
        }

        // Final reversal if we're doing ascending.
        if (sortOrder == SortOrder.ASCENDING)
            Collections.reverse(stacks);
    }
}
