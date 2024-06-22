package rocks.blackblock.topper.statistics;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import rocks.blackblock.bib.BibMod;
import rocks.blackblock.bib.augment.Augment;
import rocks.blackblock.topper.BlackBlockTopper;

import java.util.ArrayList;
import java.util.List;

public class CustomStatisticsAugment implements Augment.Global {

    private static CustomStatisticsAugment INSTANCE = null;
    private final List<CustomStatistic> customStatisticList = new ArrayList<>();
    private boolean is_dirty = false;

    public CustomStatisticsAugment() {
        if (INSTANCE != null) {
            BlackBlockTopper.LOGGER.warn("CustomStatisticsAugment already exists!");
        } else {
            BlackBlockTopper.LOGGER.info("Creating CustomStatisticsAugment!");
            INSTANCE = this;
        }
    }

    @Override
    public boolean isDirty() { return this.is_dirty; }

    @Override
    public void setDirty(boolean dirty) { this.is_dirty = dirty; }

    /**
     * Revive the custom blackblock stats from the given NBT data
     *
     * @author   Jade Godwin        <icanhasabanana@gmail.com>
     * @since    0.2.0
     */
    @Override
    public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        // Get custom statistics list.
        NbtList list = nbt.getList("custom_statistics", NbtElement.COMPOUND_TYPE);

        // For each entry in the list, add a new custom statistic.
        list.forEach(nbtElement -> {
            CustomStatistic customStatistic = CustomStatistic.fromNbt(nbtElement);
            if (customStatistic != null)
                customStatisticList.add(customStatistic);
        });
    }

    /**
     * Write the custom blackblock stats to the given NBT data
     *
     * @author   Jade Godwin        <icanhasabanana@gmail.com>
     * @since    0.2.0
     */
    @Override
    public NbtCompound writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        // Create an NBT List and add each statistic compound to it.
        NbtList list = new NbtList();
        customStatisticList.forEach(customStatistic -> list.add(customStatistic.toNbt()));
        tag.put("custom_statistics", list);
        return tag;
    }

    /**
     * Get a custom statistic out of the list via key.
     *
     * @author   Jade Godwin        <icanhasabanana@gmail.com>
     * @since    0.2.0
     */
    public CustomStatistic getCustomStatistic(String key) {
        Identifier key_identifier = Identifier.of("bbstats", key);
        for (CustomStatistic statistic : customStatisticList)
            if (statistic.getKey().equals(key_identifier)) return statistic;
        return null;
    }

    /**
     * Get all custom statistics.
     *
     * @author   Jade Godwin        <icanhasabanana@gmail.com>
     * @since    0.2.0
     */
    public List<CustomStatistic> getCustomStatistics() {
        // Prepare returned list.
        List<CustomStatistic> returned_list = new ArrayList<>();
        returned_list.addAll(customStatisticList);
        return returned_list;
    }

    /**
     * Get the custom statistics that pertain to the given player.
     *
     * @author   Jade Godwin        <icanhasabanana@gmail.com>
     * @since    0.2.0
     */
    public List<CustomStatistic> getCustomStatistics(ServerCommandSource serverCommandSource, CustomStatisticPertainability pertainability) {
        // If pertainability is all, just put them all on there.
        if (pertainability == CustomStatisticPertainability.ALL || serverCommandSource.hasPermissionLevel(1))
            return getCustomStatistics();

        // Prepare returned list and get server player.
        List<CustomStatistic> returned_list = new ArrayList<>();
        ServerPlayerEntity player = serverCommandSource.getPlayer();
        if (player != null) {
            String player_name = player.getName().getString();

            // If pertainability is owned, get all that have matching UUIDs in ownership.
            if (pertainability == CustomStatisticPertainability.OWNS) {
                customStatisticList.forEach(customStatistic -> {
                    if (customStatistic.getOwner().equals(player_name))
                        returned_list.add(customStatistic);
                });
            }

            // If pertainability is maintains, get all that have matching UUIDs in maintainers.
            else if (pertainability == CustomStatisticPertainability.MAINTAINS) {
                customStatisticList.forEach(customStatistic -> {
                    if (customStatistic.isMaintainer(player_name))
                        returned_list.add(customStatistic);
                });
            }
        }

        // Return.
        return returned_list;
    }

    /**
     * Create a new custom statistic
     *
     * @author   Jade Godwin        <icanhasabanana@gmail.com>
     * @since    0.2.0
     */
    public int createCustomStatistic(String key, String name, String owner_name) {
        // If a custom statistic already exists by the given key, just return.
        if (this.getCustomStatistic(key) != null)
            return 0;

        // Create and add a new CustomStatistic.
        CustomStatistic new_stat = new CustomStatistic(Identifier.of("bbstats", key), name, owner_name);
        customStatisticList.add(new_stat);
        this.markDirty();
        return 1;
    }

    /**
     * Delete a custom statistic.
     *
     * @author   Jade Godwin        <icanhasabanana@gmail.com>
     * @since    0.2.0
     */
    public int deleteCustomStatistic(CustomStatistic statistic) {
        // Remove.
        customStatisticList.remove(statistic);
        this.markDirty();
        return 1;
    }

    /**
     * Get the CustomStatisticsAugment instance
     *
     * @author   Jade Godwin        <icanhasabanana@gmail.com>
     * @since    0.2.0
     */
    public static CustomStatisticsAugment getInstance() {
        if (INSTANCE != null) return INSTANCE;
        return BlackBlockTopper.CUSTOM_STATS.get();
    }

    /**
     * Get the registry manager from the World instance
     *
     * @author   Jelle De Loecker <jelle@elevenways.be>
     * @since    0.2.1
     */
    @Override
    public RegistryWrapper.WrapperLookup getRegistryManager() {
        return BibMod.getDynamicRegistry();
    }
}
