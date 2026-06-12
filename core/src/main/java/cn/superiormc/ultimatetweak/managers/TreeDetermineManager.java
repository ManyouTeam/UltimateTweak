package cn.superiormc.ultimatetweak.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TreeDetermineManager {

    public static TreeDetermineManager treeDetermineManager = new TreeDetermineManager();

    private final Map<UUID, TreeDefinition> debugDefinitions = new ConcurrentHashMap<>();

    public TreeDetermineManager() {
        treeDetermineManager = this;
    }

    public TreeDetectionResult determineTree(Player player, Block origin) {
        TreeDefinition debugDefinition = debugDefinitions.get(player.getUniqueId());
        if (debugDefinition != null) {
            sendDebugResult(player, origin, debugTree(player, origin, debugDefinition));
        }
        return determineTree(createSnapshot(player, origin, ConfigManager.configManager.getTreeDefinitionsByLog(HookManager.hookManager.getBlockId(origin))));
    }

    public void setDebugDefinition(Player player, TreeDefinition definition) {
        if (definition == null) {
            debugDefinitions.remove(player.getUniqueId());
        } else {
            debugDefinitions.put(player.getUniqueId(), definition);
        }
    }

    public TreeDetectionResult determineTree(TreeSnapshot snapshot) {
        for (TreeDefinition candidate : snapshot.candidates()) {
            CandidateAnalysis analysis = analyzeCandidate(snapshot, candidate);
            if (!analysis.isValid()) {
                continue;
            }

            return new TreeDetectionResult(candidate, snapshot, analysis.treeBlocks(),
                    getLowestLog(analysis.logs()), analysis.logAmount(), analysis.leafAmount());
        }
        return null;
    }

    public TreeDebugResult debugTree(Player player, Block origin, TreeDefinition definition) {
        TreeSnapshot snapshot = createSnapshot(player, origin, Collections.singletonList(definition));
        CandidateAnalysis analysis = analyzeCandidate(snapshot, definition);
        return new TreeDebugResult(
                definition,
                HookManager.hookManager.getBlockId(origin),
                analysis.originMatches(),
                analysis.logAmount(),
                analysis.leafAmount(),
                snapshot.blocks().size(),
                analysis.isValid()
        );
    }

    private void sendDebugResult(Player player, Block origin, TreeDebugResult result) {
        TreeDefinition definition = result.treeDefinition();
        LanguageManager.languageManager.sendStringText(player, "tree-debug.header",
                "id", definition.getId(), "position", position(origin));
        sendDebugCheck(player, result.originMatches(), "origin-block",
                String.valueOf(result.originBlockId()), String.join(", ", definition.getLogs()));
        sendDebugCheck(player,
                isWithin(result.logAmount(), definition.getMinLogAmount(), definition.getMaxLogAmount()),
                "logs", String.valueOf(result.logAmount()),
                range(player, definition.getMinLogAmount(), definition.getMaxLogAmount()));
        sendDebugCheck(player,
                isWithin(result.leafAmount(), definition.getMinLeafAmount(), definition.getMaxLeafAmount()),
                "leaves", String.valueOf(result.leafAmount()),
                range(player, definition.getMinLeafAmount(), definition.getMaxLeafAmount()));
        LanguageManager.languageManager.sendStringText(player, "tree-debug.scanned",
                "amount", String.valueOf(result.scannedBlockAmount()));
        LanguageManager.languageManager.sendStringText(player,
                result.valid() ? "tree-debug.result-pass" : "tree-debug.result-fail");
    }

    private void sendDebugCheck(Player player, boolean passed, String name, String actual, String expected) {
        LanguageManager.languageManager.sendStringText(player,
                passed ? "tree-debug.check-pass" : "tree-debug.check-fail",
                "name", LanguageManager.languageManager.getStringText(player, "tree-debug.check-name." + name),
                "actual", actual,
                "expected", expected);
    }

    private boolean isWithin(int value, int min, int max) {
        return value >= min && value <= max;
    }

    private String range(Player player, int min, int max) {
        return min + ".." + (max == Integer.MAX_VALUE
                ? LanguageManager.languageManager.getStringText(player, "tree-debug.unlimited")
                : max);
    }

    private String position(Block block) {
        return block.getWorld().getName() + " " + block.getX() + "," + block.getY() + "," + block.getZ();
    }

    private CandidateAnalysis analyzeCandidate(TreeSnapshot snapshot, TreeDefinition candidate) {
        Map<BlockOffset, SnapshotBlock> blockMap = createBlockMap(snapshot, candidate);
        SnapshotBlock origin = blockMap.get(new BlockOffset(0, 0, 0));
        if (origin == null || !candidate.getLogs().contains(origin.blockId())) {
            return new CandidateAnalysis(candidate, false, Collections.emptyList(), Collections.emptyList());
        }

        List<SnapshotBlock> logs = findOriginLogComponent(origin, blockMap, candidate);
        List<SnapshotBlock> leaves = findLeavesAroundLogs(logs, blockMap, candidate);
        return new CandidateAnalysis(candidate, true, logs, leaves);
    }

    private SnapshotBlock getLowestLog(List<SnapshotBlock> logs) {
        SnapshotBlock lowestLog = logs.get(0);
        for (int index = 1; index < logs.size(); index++) {
            SnapshotBlock block = logs.get(index);
            if (block.y() < lowestLog.y()
                    || block.y() == lowestLog.y() && (block.x() < lowestLog.x()
                    || block.x() == lowestLog.x() && block.z() < lowestLog.z())) {
                lowestLog = block;
            }
        }
        return lowestLog;
    }

    public TreeSnapshot createSnapshot(Player player, Block origin, List<TreeDefinition> candidates) {
        World world = origin.getWorld();
        int originX = origin.getX();
        int originY = origin.getY();
        int originZ = origin.getZ();
        int maxRadius = getMaxRadius(candidates);
        int maxHeight = getMaxHeight(candidates);
        int scanLimit = getConnectedScanLimit(candidates);

        List<SnapshotBlock> blocks = new ArrayList<>();
        Queue<Block> queue = new ArrayDeque<>();
        Set<LocationKey> visited = new HashSet<>();

        queue.add(origin);
        visited.add(LocationKey.of(origin));

        while (!queue.isEmpty() && visited.size() <= scanLimit) {
            Block block = queue.poll();
            String blockId = HookManager.hookManager.getBlockId(block);
            if (!isCandidateTreeBlock(blockId, candidates)) {
                continue;
            }

            blocks.add(new SnapshotBlock(
                    block.getX() - originX,
                    block.getY() - originY,
                    block.getZ() - originZ,
                    blockId,
                    isPersistentLeaf(block),
                    getLeafDistance(block)));

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) {
                            continue;
                        }

                        Block next = block.getRelative(dx, dy, dz);
                        if (player != null && !HookManager.hookManager.getProtectionCanUse(player, block.getLocation())) {
                            continue;
                        }
                        if (!isInsideScanBounds(next, world, originX, originY, originZ, maxRadius, maxHeight)) {
                            continue;
                        }
                        if (visited.size() >= scanLimit ||
                                !isCandidateTreeBlock(HookManager.hookManager.getBlockId(next), candidates)) {
                            continue;
                        }

                        LocationKey nextKey = LocationKey.of(next);
                        if (visited.add(nextKey)) {
                            queue.add(next);
                        }
                    }
                }
            }
        }

        return new TreeSnapshot(world, originX, originY, originZ, blocks, candidates);
    }

    public List<Block> getValidBlocks(TreeDetectionResult result) {
        List<Block> currentBlocks = new ArrayList<>();
        TreeDefinition definition = result.treeDefinition();
        World world = result.snapshot.world();
        for (SnapshotBlock snapshotBlock : result.treeBlocks()) {
            Block block = world.getBlockAt(
                    snapshotBlock.getWorldX(result.snapshot()),
                    snapshotBlock.getWorldY(result.snapshot()),
                    snapshotBlock.getWorldZ(result.snapshot()));
            String currentBlockId = HookManager.hookManager.getBlockId(block);
            if (definition.getLogs().contains(currentBlockId)) {
                currentBlocks.add(block);
            } else if (definition.getLeaves().contains(currentBlockId) &&
                    (definition.isPlayerLeave() || !isPersistentLeaf(block))) {
                currentBlocks.add(block);
            }
        }
        return currentBlocks;
    }

    private boolean isLeafOwnedByOriginTree(SnapshotBlock leaf,
                                            List<SnapshotBlock> ownLogs,
                                            Set<BlockOffset> ownLogOffsets,
                                            List<SnapshotBlock> allCandidateLogs) {
        int nearestOwnLogDistance = Integer.MAX_VALUE;
        int nearestOtherLogDistance = Integer.MAX_VALUE;
        // 当前树原木到该树叶的最近距离
        for (SnapshotBlock log : ownLogs) {
            nearestOwnLogDistance = Math.min(
                    nearestOwnLogDistance,
                    distanceManhattan3D(leaf, log)
            );
        }

        // 其他树原木到该树叶的最近距离
        for (SnapshotBlock log : allCandidateLogs) {
            BlockOffset offset = new BlockOffset(log.x(), log.y(), log.z());

            // 跳过当前树自己的原木
            if (ownLogOffsets.contains(offset)) {
                continue;
            }

            nearestOtherLogDistance = Math.min(
                    nearestOtherLogDistance,
                    distanceManhattan3D(leaf, log)
            );
        }

        // 附近没有其他树原木，直接认为属于当前树
        if (nearestOtherLogDistance == Integer.MAX_VALUE) {
            return true;
        }
        return nearestOwnLogDistance < nearestOtherLogDistance;
    }

    private int distanceManhattan3D(SnapshotBlock a, SnapshotBlock b) {
        return Math.abs(a.x() - b.x()) +
                Math.abs(a.y() - b.y()) +
                Math.abs(a.z() - b.z());
    }

    public boolean isInsideDefinitionBounds(SnapshotBlock block, TreeDefinition candidate) {
        return Math.abs(block.x()) <= candidate.getRadius() &&
                Math.abs(block.z()) <= candidate.getRadius() &&
                block.y() >= -candidate.getRadius() &&
                block.y() <= candidate.getHeight();
    }

    public boolean isPersistentLeaf(Block block) {
        return block.getBlockData() instanceof Leaves leaves && leaves.isPersistent();
    }

    private int getLeafDistance(Block block) {
        return block.getBlockData() instanceof Leaves leaves ? leaves.getDistance() : -1;
    }

    public boolean isLeafBlock(Block block, TreeDefinition definition) {
        return definition.getLeaves().contains(HookManager.hookManager.getBlockId(block));
    }

    private Map<BlockOffset, SnapshotBlock> createBlockMap(TreeSnapshot snapshot,
                                                           TreeDefinition candidate) {
        Map<BlockOffset, SnapshotBlock> blockMap = new HashMap<>();
        for (SnapshotBlock block : snapshot.blocks()) {
            if (isInsideDefinitionBounds(block, candidate)) {
                blockMap.put(new BlockOffset(block.x(), block.y(), block.z()), block);
            }
        }
        return blockMap;
    }

    private List<SnapshotBlock> findOriginLogComponent(SnapshotBlock origin,
                                                       Map<BlockOffset, SnapshotBlock> blockMap,
                                                       TreeDefinition candidate) {
        List<SnapshotBlock> logs = new ArrayList<>();
        Queue<SnapshotBlock> queue = new ArrayDeque<>();
        Set<BlockOffset> visited = new HashSet<>();

        queue.add(origin);
        visited.add(new BlockOffset(origin.x(), origin.y(), origin.z()));

        while (!queue.isEmpty() && logs.size() <= candidate.getMaxLogAmount()) {
            SnapshotBlock block = queue.poll();
            if (!candidate.getLogs().contains(block.blockId())) {
                continue;
            }
            logs.add(block);

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) {
                            continue;
                        }

                        BlockOffset nextOffset = new BlockOffset(block.x() + dx, block.y() + dy, block.z() + dz);
                        if (!visited.add(nextOffset)) {
                            continue;
                        }

                        SnapshotBlock next = blockMap.get(nextOffset);
                        if (next != null && candidate.getLogs().contains(next.blockId())) {
                            queue.add(next);
                        }
                    }
                }
            }
        }
        return logs;
    }

    private List<SnapshotBlock> findLeavesAroundLogs(List<SnapshotBlock> logs,
                                                     Map<BlockOffset, SnapshotBlock> blockMap,
                                                     TreeDefinition candidate) {
        List<SnapshotBlock> leaves = new ArrayList<>();
        if (logs.isEmpty()) {
            return leaves;
        }

        // 当前树自己的原木坐标集合
        Set<BlockOffset> ownLogOffsets = new HashSet<>();
        for (SnapshotBlock log : logs) {
            ownLogOffsets.add(new BlockOffset(log.x(), log.y(), log.z()));
        }

        // 当前扫描范围内，所有属于当前树定义的原木
        // 包括当前树原木，也包括可能在附近的隔壁树原木
        List<SnapshotBlock> allCandidateLogs = new ArrayList<>();
        for (SnapshotBlock block : blockMap.values()) {
            if (candidate.getLogs().contains(block.blockId())) {
                allCandidateLogs.add(block);
            }
        }

        Queue<SnapshotBlock> queue = new ArrayDeque<>(logs);
        Set<BlockOffset> visited = new HashSet<>();

        // 原木本身标记为已访问
        for (SnapshotBlock log : logs) {
            visited.add(new BlockOffset(log.x(), log.y(), log.z()));
        }

        while (!queue.isEmpty() && leaves.size() <= candidate.getMaxLeafAmount()) {
            SnapshotBlock block = queue.poll();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) {
                            continue;
                        }

                        BlockOffset nextOffset = new BlockOffset(
                                block.x() + dx,
                                block.y() + dy,
                                block.z() + dz
                        );

                        if (!visited.add(nextOffset)) {
                            continue;
                        }

                        SnapshotBlock next = blockMap.get(nextOffset);

                        if (next == null) {
                            continue;
                        }

                        // 不是当前树定义允许的树叶，跳过
                        if (!candidate.getLeaves().contains(next.blockId())) {
                            continue;
                        }

                        // 如果不允许玩家放置的树叶，并且当前树叶是 persistent，则跳过
                        if (!candidate.isPlayerLeave() && next.persistentLeaf()) {
                            continue;
                        }

                        // 必须在当前树原木的半径范围内
                        if (!isWithinVanillaLeafDistance(next, logs)) {
                            continue;
                        }

                        if (!isNearAnyLog(next, logs, candidate.getRadius())) {
                            continue;
                        }

                        // 核心新增逻辑：
                        // 判断这片树叶是否更像属于当前这棵树，而不是隔壁树
                        if (!isLeafOwnedByOriginTree(next, logs, ownLogOffsets, allCandidateLogs)) {
                            continue;
                        }

                        leaves.add(next);
                        queue.add(next);
                    }
                }
            }
        }

        return leaves;
    }

    private boolean isNearAnyLog(SnapshotBlock leaf, List<SnapshotBlock> logs, int radius) {
        int radiusSquared = radius * radius;
        for (SnapshotBlock log : logs) {
            int dx = leaf.x() - log.x();
            int dz = leaf.z() - log.z();
            if (dx * dx + dz * dz <= radiusSquared) {
                return true;
            }
        }
        return false;
    }

    private boolean isWithinVanillaLeafDistance(SnapshotBlock leaf, List<SnapshotBlock> logs) {
        if (leaf.leafDistance() < 0) {
            return true;
        }
        for (SnapshotBlock log : logs) {
            if (distanceManhattan3D(leaf, log) <= leaf.leafDistance()) {
                return true;
            }
        }
        return false;
    }

    private boolean isInsideScanBounds(Block block,
                                       World world,
                                       int originX,
                                       int originY,
                                       int originZ,
                                       int maxRadius,
                                       int maxHeight) {
        return block.getWorld().equals(world) &&
                block.getY() >= world.getMinHeight() &&
                block.getY() < world.getMaxHeight() &&
                Math.abs(block.getX() - originX) <= maxRadius &&
                Math.abs(block.getZ() - originZ) <= maxRadius &&
                block.getY() - originY >= -maxRadius &&
                block.getY() - originY <= maxHeight;
    }

    private boolean isCandidateTreeBlock(String blockId, List<TreeDefinition> candidates) {
        if (blockId == null) {
            return false;
        }
        for (TreeDefinition candidate : candidates) {
            if (candidate.getLogs().contains(blockId) || candidate.getLeaves().contains(blockId)) {
                return true;
            }
        }
        return false;
    }

    private int getMaxRadius(List<TreeDefinition> candidates) {
        int maxRadius = 0;
        for (TreeDefinition candidate : candidates) {
            maxRadius = Math.max(maxRadius, candidate.getRadius());
        }
        return maxRadius;
    }

    private int getMaxHeight(List<TreeDefinition> candidates) {
        int maxHeight = 0;
        for (TreeDefinition candidate : candidates) {
            maxHeight = Math.max(maxHeight, candidate.getHeight());
        }
        return maxHeight;
    }

    private int getConnectedScanLimit(List<TreeDefinition> candidates) {
        int limit = 256;
        for (TreeDefinition candidate : candidates) {
            if (candidate.getMaxLeafAmount() == Integer.MAX_VALUE) {
                continue;
            }
            limit = Math.max(limit, candidate.getMaxLogAmount() + candidate.getMaxLeafAmount() + 64);
        }
        return limit;
    }

    public record TreeSnapshot(World world, int originX, int originY, int originZ, List<SnapshotBlock> blocks, List<TreeDefinition> candidates) {

    }

    public record SnapshotBlock(int x, int y, int z, String blockId, boolean persistentLeaf, int leafDistance) {

        public int getWorldX(TreeSnapshot snapshot) {
            return snapshot.originX() + x;
        }

        public int getWorldY(TreeSnapshot snapshot) {
            return snapshot.originY() + y;
        }

        public int getWorldZ(TreeSnapshot snapshot) {
            return snapshot.originZ() + z;
        }
    }

    public record TreeDetectionResult(TreeDefinition treeDefinition, TreeSnapshot snapshot,
                                      List<SnapshotBlock> treeBlocks, SnapshotBlock lowestLog,
                                      int logAmount, int leafAmount) {

        public Block getLowestLogBlock() {
            return snapshot.world().getBlockAt(
                    lowestLog.getWorldX(snapshot),
                    lowestLog.getWorldY(snapshot),
                    lowestLog.getWorldZ(snapshot));
        }
    }

    public record TreeDebugResult(TreeDefinition treeDefinition, String originBlockId,
                                  boolean originMatches, int logAmount, int leafAmount,
                                  int scannedBlockAmount, boolean valid) {

    }

    private record CandidateAnalysis(TreeDefinition treeDefinition, boolean originMatches, List<SnapshotBlock> logs,
                                     List<SnapshotBlock> leaves) {

        private int logAmount() {
            return logs.size();
        }

        private int leafAmount() {
            return leaves.size();
        }

        private List<SnapshotBlock> treeBlocks() {
            List<SnapshotBlock> result = new ArrayList<>(logs.size() + leaves.size());
            result.addAll(logs);
            result.addAll(leaves);
            return result;
        }

        private boolean isValid() {
            return originMatches
                    && logAmount() >= treeDefinition.getMinLogAmount()
                    && logAmount() <= treeDefinition.getMaxLogAmount()
                    && leafAmount() >= treeDefinition.getMinLeafAmount()
                    && leafAmount() <= treeDefinition.getMaxLeafAmount();
        }
    }

    public static class TreeDefinition {

        private final String id;

        private final Set<String> logs;

        private final Set<String> leaves;

        private final int radius;

        private final int height;

        private final int minLogAmount;

        private final int maxLogAmount;

        private final int minLeafAmount;

        private final int maxLeafAmount;

        private final boolean playerLeave;

        private final Boolean sendFakeAttribute;

        public TreeDefinition(String id,
                              Set<String> logs,
                              Set<String> leaves,
                              int radius,
                              int height,
                              int minLogAmount,
                              int maxLogAmount,
                              int minLeafAmount,
                              int maxLeafAmount,
                              boolean playerLeave,
                              Boolean sendFakeAttribute) {
            this.id = id;
            this.logs = logs;
            this.leaves = leaves;
            this.radius = radius;
            this.height = height;
            this.minLogAmount = minLogAmount;
            this.maxLogAmount = maxLogAmount;
            this.minLeafAmount = minLeafAmount;
            this.maxLeafAmount = maxLeafAmount;
            this.playerLeave = playerLeave;
            this.sendFakeAttribute = sendFakeAttribute;
        }

        public static TreeDefinition fromSection(String fallbackId, ConfigurationSection section) {
            Set<String> logs = readBlockIds(section, "logs", "log");
            Set<String> leaves = readBlockIds(section, "leaves", "leaf");
            if (logs.isEmpty() || leaves.isEmpty()) {
                return null;
            }

            String id = section.getString("id", fallbackId);
            int minLogAmount = Math.max(1, section.getInt("min-log-amount", section.getInt("min-logs", 4)));
            int maxLogAmount = Math.max(minLogAmount, section.getInt("max-log-amount", section.getInt("max-logs", 128)));
            int minLeafAmount = Math.max(1, section.getInt("min-leaf-amount", section.getInt("min-leaves", 1)));
            int maxLeafAmount = Math.max(minLeafAmount, section.getInt("max-leaf-amount", section.getInt("max-leaves", Integer.MAX_VALUE)));
            boolean playerLeave = section.getBoolean("player-leave", false);
            Boolean sendFakeAttribute = section.contains("send-fake-attribute")
                    ? section.getBoolean("send-fake-attribute")
                    : null;
            return new TreeDefinition(
                    id,
                    logs,
                    leaves,
                    Math.max(0, section.getInt("radius", 4)),
                    Math.max(1, section.getInt("height", 32)),
                    minLogAmount,
                    maxLogAmount,
                    minLeafAmount,
                    maxLeafAmount,
                    playerLeave,
                    sendFakeAttribute
            );
        }

        public String getId() {
            return id;
        }

        public Set<String> getLogs() {
            return logs;
        }

        public Set<String> getLeaves() {
            return leaves;
        }

        public int getRadius() {
            return radius;
        }

        public int getHeight() {
            return height;
        }

        public int getMinLogAmount() {
            return minLogAmount;
        }

        public int getMaxLogAmount() {
            return maxLogAmount;
        }

        public int getMinLeafAmount() {
            return minLeafAmount;
        }

        public int getMaxLeafAmount() {
            return maxLeafAmount;
        }

        public boolean isPlayerLeave() {
            return playerLeave;
        }

        public Boolean getSendFakeAttribute() {
            return sendFakeAttribute;
        }

        private static Set<String> readBlockIds(ConfigurationSection section, String listKey, String singleKey) {
            Set<String> result = new LinkedHashSet<>();
            addBlockIds(result, section.getStringList(listKey));
            addBlockIds(result, section.getStringList(singleKey));

            String singleValue = section.getString(listKey);
            if (singleValue != null) {
                result.add(normalizeBlockId(singleValue));
            }
            singleValue = section.getString(singleKey);
            if (singleValue != null) {
                result.add(normalizeBlockId(singleValue));
            }
            return Collections.unmodifiableSet(result);
        }

        private static void addBlockIds(Set<String> result, Collection<String> values) {
            for (String value : values) {
                result.add(normalizeBlockId(value));
            }
        }

        public static String normalizeBlockId(String blockId) {
            if (blockId == null) {
                return "";
            }
            String normalized = blockId.trim().toLowerCase();
            if (normalized.isEmpty()) {
                return normalized;
            }
            return normalized.contains(":") ? normalized : "minecraft:" + normalized;
        }
    }

    private record BlockOffset(int x, int y, int z) {

    }

    private record LocationKey(UUID worldId, int x, int y, int z) {

        private static LocationKey of(Block block) {
            return new LocationKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof LocationKey that)) {
                return false;
            }
            return x == that.x && y == that.y && z == that.z && worldId.equals(that.worldId);
        }

        @Override
        public @NonNull String toString() {
            return worldId + ":" + x + ":" + y + ":" + z;
        }
    }
}
