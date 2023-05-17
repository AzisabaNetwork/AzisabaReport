package net.azisaba.azisabareport.spigot.gui;

import net.azisaba.azisabareport.common.data.PlayerData;
import net.azisaba.azisabareport.common.util.StringUtil;
import net.azisaba.azisabareport.spigot.AzisabaReport;
import net.azisaba.azisabareport.spigot.data.ReportData;
import net.azisaba.azisabareport.spigot.sql.DataProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReportsScreen extends Screen {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
    private static final Set<PlayerData> PLAYERS = new HashSet<>();
    private final AzisabaReport plugin;
    private final List<ReportData> reports;
    private final int maxPage;
    private int page = 0;

    public ReportsScreen(@NotNull AzisabaReport plugin, @NotNull List<ReportData> reports) {
        super(54, "通報一覧");
        this.plugin = plugin;
        this.reports = reports.stream().sorted((a, b) -> Long.compare(b.createdAt(), a.createdAt())).collect(Collectors.toList());
        this.maxPage = (int) Math.floor(reports.size() / 45.0);
        reset();
    }

    public void reset() {
        inventory.clear();
        List<ReportData> subList = reports.subList(page * 9 * 5, Math.min(reports.size(), (page + 1) * 9 * 5 - 1));
        Set<UUID> uuids = new HashSet<>();
        for (int i = 0; i < subList.size(); i++) {
            ReportData data = subList.get(i);
            uuids.add(data.reportedId());
            String reportedName = PLAYERS.stream().filter(pd -> pd.uuid().equals(data.reportedId())).findAny().map(PlayerData::name).orElse("読み込み中...");
            List<String> lore = new ArrayList<>();
            List<String> reasonSplit = StringUtil.split(data.reason(), 30);
            lore.add(ChatColor.GOLD + "内容: " + ChatColor.WHITE + reasonSplit.get(0));
            for (int j = 1; j < reasonSplit.size(); j++) {
                lore.add(" " + ChatColor.WHITE + reasonSplit.get(j));
            }
            String status;
            String statusDetails;
            if (data.flags().contains(ReportData.OPEN)) {
                status = ChatColor.YELLOW + "未解決";
                statusDetails = "1か月以上経過しても未解決の場合はお問い合わせください。";
            } else if (data.flags().contains(ReportData.CLOSED) && data.flags().contains(ReportData.RESOLVED)) {
                status = ChatColor.GREEN + "対応済み";
                statusDetails = "この通報は対応済み(解決済み)です。";
            } else if (data.flags().contains(ReportData.CLOSED) && data.flags().contains(ReportData.INVALID)) {
                status = ChatColor.RED + "却下";
                statusDetails = "「運営からのコメント」が記載されていない場合は却下の理由をお伝えできません。";
            } else if (data.flags().contains(ReportData.CLOSED) && data.flags().contains(ReportData.NEED_MORE_PROOF)) {
                status = ChatColor.RED + "証拠不十分";
                statusDetails = "証拠が必要、もしくは証拠が不十分なためこの通報は却下されました。";
            } else {
                status = ChatColor.GRAY + "不明 (" + data.flags().getValue() + ")";
                statusDetails = "バグです。このアイテムのスクリーンショットを持って報告をお願いします。";
            }
            lore.add(ChatColor.GOLD + "状態: " + status);
            List<String> statusDetailsSplit = StringUtil.split(statusDetails, 40);
            lore.add(ChatColor.GRAY + " " + ChatColor.GRAY + ChatColor.ITALIC + " " + statusDetailsSplit.get(0));
            for (int j = 1; j < statusDetailsSplit.size(); j++) {
                lore.add(" " + ChatColor.WHITE + statusDetailsSplit.get(j));
            }
            if (data.publicComment() != null) {
                List<String> commentSplit = StringUtil.split(data.publicComment(), 40);
                lore.add(ChatColor.GOLD + "運営からのコメント: " + ChatColor.WHITE + commentSplit.get(0));
                for (int j = 1; j < commentSplit.size(); j++) {
                    lore.add(" " + ChatColor.WHITE + commentSplit.get(j));
                }
            }
            lore.add("");
            lore.add(ChatColor.GOLD + "通報日時: " + ChatColor.WHITE + DATE_FORMAT.format(data.createdAt()));
            lore.add(ChatColor.GOLD + "更新日時: " + ChatColor.WHITE + DATE_FORMAT.format(data.updatedAt()));
            setItem(i,
                    Material.PAPER,
                    1,
                    ChatColor.YELLOW + "#" + data.id() + ChatColor.WHITE + " | " + ChatColor.RED + reportedName,
                    lore.toArray(new String[0]));
        }
        uuids.removeIf(uuid -> PLAYERS.stream().anyMatch(pd -> pd.uuid().equals(uuid)));
        // fetch player names
        if (!uuids.isEmpty()) {
            new Thread(() -> {
                PLAYERS.addAll(DataProvider.getPlayerDataByIds(plugin.getDatabaseManager(), new ArrayList<>(uuids)));
                reset();
            }).start();
        }
        // menu
        setItem(49, Material.BARRIER, 1, ChatColor.YELLOW + "閉じる");
        List<String> statsLore = new ArrayList<>();
        statsLore.add(ChatColor.GOLD + "通報回数: " + ChatColor.GREEN + reports.size());
        long closed = reports.stream().filter(data -> data.flags().contains(ReportData.CLOSED)).count();
        long resolved = reports.stream().filter(data -> data.flags().contains(ReportData.CLOSED) && data.flags().contains(ReportData.RESOLVED)).count();
        double roundedAccuracy = Math.round((double) resolved / closed * 10000) / 100.0;
        ChatColor accuracyColor;
        if (roundedAccuracy > 90) {
            accuracyColor = ChatColor.GREEN;
        } else if (roundedAccuracy > 70) {
            accuracyColor = ChatColor.YELLOW;
        } else {
            accuracyColor = ChatColor.RED;
        }
        statsLore.add(ChatColor.GOLD + "通報精度 " + ChatColor.GRAY + "(精度 = 対応済み / 「未解決」以外)" + ChatColor.GOLD + ": " + accuracyColor + roundedAccuracy + '%');
        setItem(50, Material.DIAMOND, 1, ChatColor.GREEN + "あなたの通報の統計", statsLore.toArray(new String[0]));
        if (page > 0) {
            setItem(45, Material.ARROW, 1, ChatColor.GREEN + "← 前のページ");
        }
        if (page < maxPage) {
            setItem(53, Material.ARROW, 1, ChatColor.GREEN + "次のページ →");
        }
    }

    public static class EventListener implements Listener {
        @EventHandler
        public void onInventoryDrag(InventoryDragEvent e) {
            if (e.getInventory() != null && e.getInventory().getHolder() instanceof ReportsScreen) {
                e.setCancelled(true);
            }
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (e.getInventory() == null || !(e.getInventory().getHolder() instanceof ReportsScreen)) {
                return;
            }
            e.setCancelled(true);
            if (e.getClickedInventory() == null || !(e.getClickedInventory().getHolder() instanceof ReportsScreen)) {
                return;
            }
            ReportsScreen screen = (ReportsScreen) e.getInventory().getHolder();
            if (e.getSlot() == 49) { // close
                Bukkit.getScheduler().runTask(screen.plugin, () -> e.getWhoClicked().closeInventory());
            } else if (e.getSlot() == 45 && screen.page > 0) { // back
                screen.page--;
                screen.reset();
            } else if (e.getSlot() == 53 && screen.page < screen.maxPage) { // next
                screen.page++;
                screen.reset();
            }
        }
    }
}
