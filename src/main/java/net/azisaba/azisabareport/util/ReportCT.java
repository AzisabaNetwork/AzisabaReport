package net.azisaba.azisabareport.util;

import java.util.HashMap;
import java.util.UUID;
import net.azisaba.azisabareport.ConfigManager;

public class ReportCT {

  // 1s = 1000ms
  private static final int coolTime = ConfigManager.getReportCoolTime() * 1000;

  private static final HashMap<UUID, Integer> coolTimeMap = new HashMap<>();

  /**
   * is cool time?
   * @param uuid uuid(Player.getUniqueId())
   * @return result true=Cooling down false=Not cooled down
   */
  public static Boolean isCoolDown(UUID uuid) {
    if (!(coolTimeMap.containsKey(uuid))) {
      return false;
    }

    long now = System.currentTimeMillis();
    long before = coolTimeMap.get(uuid);
    return now - before < coolTime;
  }

  /**
   * start cool time
   * @param uuid uuid(Player.getUniqueId())
   */
  public static void startCoolDown(UUID uuid) {
    coolTimeMap.remove(uuid);
    coolTimeMap.put(uuid, (int) System.currentTimeMillis());
  }

}
