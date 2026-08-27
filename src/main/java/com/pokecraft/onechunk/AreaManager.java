package com.pokecraft.onechunk;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AreaManager {

    private final OneChunkPlugin plugin;
    private final Map<UUID, PlayerAreaData> areas = new HashMap<>();
    private final File dataFolder;

    private final String worldPrefix;
    private final int startRadius;
    private final int radiusIncrease;
    private final int xpBase;
    private final int xpGrowth;
    private final Map<Material, Integer> xpPerBlock = new EnumMap<>(Material.class);

    public AreaManager(OneChunkPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        FileConfiguration cfg = plugin.getConfig();
        this.worldPrefix = cfg.getString("world-prefix", "chunk_");
        this.startRadius = cfg.getInt("start-radius", 8);
        this.radiusIncrease = cfg.getInt("radius-increase-per-stage", 4);
        this.xpBase = cfg.getInt("xp-required-base", 8);
        this.xpGrowth = cfg.getInt("xp-required-growth", 10);

        if (cfg.isConfigurationSection("xp-per-block")) {
            for (String key : cfg.getConfigurationSection("xp-per-block").getKeys(false)) {
                Material mat = Material.matchMaterial(key);
                if (mat != null) {
                    xpPerBlock.put(mat, cfg.getInt("xp-per-block." + key));
                }
            }
        }
    }

    // ---------- Persistence ----------

    public PlayerAreaData getData(Player player) {
        return areas.get(player.getUniqueId());
    }

    public boolean hasArea(Player player) {
        if (areas.containsKey(player.getUniqueId())) return true;
        File f = new File(dataFolder, player.getUniqueId() + ".yml");
        return f.exists();
    }

    private String worldNameFor(UUID uuid) {
        return worldPrefix + uuid;
    }

    /**
     * Charge les donnees existantes du joueur, ou lui cree un nouveau monde prive s'il n'en a pas encore.
     * Le monde est charge/cree ici (thread principal requis par Bukkit).
     */
    public PlayerAreaData loadOrCreate(Player player) {
        UUID id = player.getUniqueId();
        if (areas.containsKey(id)) return areas.get(id);

        File f = new File(dataFolder, id + ".yml");
        PlayerAreaData data;
        if (f.exists()) {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(f);
            String world = yml.getString("world", worldNameFor(id));
            int radius = yml.getInt("radius", startRadius);
            data = new PlayerAreaData(id, world, radius);
            data.stage = yml.getInt("stage", 1);
            data.xp = yml.getInt("xp", 0);
            data.logsObjectiveDone = yml.getBoolean("logsObjectiveDone", false);
            data.stoneObjectiveDone = yml.getBoolean("stoneObjectiveDone", false);
            data.logsCollected = yml.getInt("logsCollected", 0);
            data.stoneCollected = yml.getInt("stoneCollected", 0);
            ensureWorldLoaded(data.world);
        } else {
            String worldName = worldNameFor(id);
            data = new PlayerAreaData(id, worldName, startRadius);
            ensureWorldLoaded(worldName);
            save(data);
        }
        areas.put(id, data);
        return data;
    }

    /**
     * Charge le monde prive s'il existe deja sur le disque, sinon le genere.
     */
    public World ensureWorldLoaded(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) return world;

        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(World.Environment.NORMAL);
        creator.type(WorldType.NORMAL);
        return Bukkit.createWorld(creator);
    }

    public void save(PlayerAreaData data) {
        File f = new File(dataFolder, data.uuid + ".yml");
        YamlConfiguration yml = new YamlConfiguration();
        yml.set("world", data.world);
        yml.set("radius", data.radius);
        yml.set("stage", data.stage);
        yml.set("xp", data.xp);
        yml.set("logsObjectiveDone", data.logsObjectiveDone);
        yml.set("stoneObjectiveDone", data.stoneObjectiveDone);
        yml.set("logsCollected", data.logsCollected);
        yml.set("stoneCollected", data.stoneCollected);
        try {
            yml.save(f);
        } catch (IOException e) {
            plugin.getLogger().warning("Impossible de sauvegarder les donnees de " + data.uuid + ": " + e.getMessage());
        }
    }

    public void saveAll() {
        for (PlayerAreaData data : areas.values()) {
            save(data);
        }
    }

    // ---------- Border ----------

    public boolean isOutside(PlayerAreaData data, Location loc) {
        if (loc.getWorld() == null || !loc.getWorld().getName().equals(data.world)) return false;
        int dx = loc.getBlockX() - PlayerAreaData.CENTER_X;
        int dz = loc.getBlockZ() - PlayerAreaData.CENTER_Z;
        return Math.abs(dx) > data.radius || Math.abs(dz) > data.radius;
    }

    public Location getSpawnLocation(PlayerAreaData data) {
        World world = ensureWorldLoaded(data.world);
        world.getChunkAt(0, 0).load();
        int y = world.getHighestBlockYAt(PlayerAreaData.CENTER_X, PlayerAreaData.CENTER_Z) + 1;
        return new Location(world, PlayerAreaData.CENTER_X + 0.5, y, PlayerAreaData.CENTER_Z + 0.5);
    }

    public void teleportToArea(Player player, PlayerAreaData data) {
        player.teleport(getSpawnLocation(data));
    }

    // ---------- XP / stages ----------

    public int xpRequiredForStage(int stage) {
        return xpBase + xpGrowth * (stage - 1);
    }

    public void addXp(Player player, PlayerAreaData data, int amount) {
        data.xp += amount;
        int required = xpRequiredForStage(data.stage);
        if (data.xp >= required) {
            data.xp -= required;
            data.stage++;
            data.radius += radiusIncrease;
            save(data);

            String msg = ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.stage-up", "&a&lNIVEAU SUPERIEUR !")
                            .replace("%stage%", String.valueOf(data.stage))
                            .replace("%radius%", String.valueOf(data.radius)));
            player.sendMessage(msg);
            player.sendTitle(ChatColor.GREEN + "Stade " + data.stage,
                    ChatColor.YELLOW + "Zone agrandie ! Rayon: " + data.radius, 10, 60, 10);

            if (data.xp >= xpRequiredForStage(data.stage)) {
                addXp(player, data, 0);
            }
        } else {
            save(data);
        }
    }

    public int getXpPerBlock(Material material) {
        return xpPerBlock.getOrDefault(material, 0);
    }

    // ---------- Materiaux ----------

    private static final Set<Material> LOG_MATERIALS = EnumSet.of(
            Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG,
            Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.MANGROVE_LOG, Material.CHERRY_LOG,
            Material.CRIMSON_STEM, Material.WARPED_STEM
    );

    private static final Set<Material> STONE_MATERIALS = EnumSet.of(
            Material.STONE, Material.COBBLESTONE, Material.DEEPSLATE, Material.ANDESITE,
            Material.DIORITE, Material.GRANITE, Material.TUFF
    );

    public static boolean isLog(Material m) {
        return LOG_MATERIALS.contains(m);
    }

    public static boolean isStone(Material m) {
        return STONE_MATERIALS.contains(m);
    }

    // ---------- Generation de secours ----------

    /**
     * Verifie qu'un arbre (>=3 rondins) et de la pierre sont accessibles pres de (0,0).
     * Si absent, genere une petite reserve de secours pour garantir un debut jouable.
     */
    public void ensureStartingResources(PlayerAreaData data) {
        World world = ensureWorldLoaded(data.world);
        int cx = PlayerAreaData.CENTER_X;
        int cz = PlayerAreaData.CENTER_Z;
        int surfaceY = world.getHighestBlockYAt(cx, cz);

        int logCount = 0;
        int scanRadius = Math.min(data.radius, 12);
        for (int x = -scanRadius; x <= scanRadius && logCount < 3; x++) {
            for (int z = -scanRadius; z <= scanRadius && logCount < 3; z++) {
                for (int y = Math.max(world.getMinHeight(), surfaceY - 5); y <= surfaceY + 15; y++) {
                    Block b = world.getBlockAt(cx + x, y, cz + z);
                    if (isLog(b.getType())) {
                        logCount++;
                        if (logCount >= 3) break;
                    }
                }
            }
        }
        if (logCount < 3) {
            spawnBackupTree(world, cx + 2, cz + 2);
        }

        boolean stoneFound = false;
        for (int y = surfaceY; y >= Math.max(world.getMinHeight(), surfaceY - 30); y--) {
            if (isStone(world.getBlockAt(cx, y, cz).getType())) {
                stoneFound = true;
                break;
            }
        }
        if (!stoneFound) {
            spawnBackupStonePatch(world, cx - 2, cz - 2, surfaceY);
        }
    }

    private void spawnBackupTree(World world, int x, int z) {
        int y = world.getHighestBlockYAt(x, z) + 1;
        for (int i = 0; i < 4; i++) {
            world.getBlockAt(x, y + i, z).setType(Material.OAK_LOG);
        }
        for (int lx = -2; lx <= 2; lx++) {
            for (int lz = -2; lz <= 2; lz++) {
                for (int ly = 3; ly <= 4; ly++) {
                    if (Math.abs(lx) == 2 && Math.abs(lz) == 2) continue;
                    Block b = world.getBlockAt(x + lx, y + ly, z + lz);
                    if (b.getType() == Material.AIR) {
                        b.setType(Material.OAK_LEAVES);
                    }
                }
            }
        }
    }

    private void spawnBackupStonePatch(World world, int x, int z, int surfaceY) {
        int y = surfaceY - 1;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                world.getBlockAt(x + dx, y, z + dz).setType(Material.STONE);
            }
        }
    }
}
