package net.azisaba.azisabareport.util;

import java.util.HashMap;

public class CoolTime {
  //  プレイヤーごとに分けるため、Mapで保存
  private static HashMap<String,Long> coolDownMap = new HashMap<>();

  /**
   * クールダウン中か
   * @param playerName playerName(Player.getUserName())
   * @param coolTime ミリ秒
   * @return result true=クールダウン中　false=クールダウン終了
   */
  public static boolean isCoolDown(String playerName, long coolTime){
    if (!coolDownMap.containsKey(playerName)) return false;
    long now = System.currentTimeMillis();
    long before = coolDownMap.get(playerName);
    return now - before < coolTime;
  }

  /**
   * クールダウンのスタート
   * @param playerName playerName(Player.getUserName())
   */
  public static void startCoolDown(String playerName){
    coolDownMap.remove(playerName);
    coolDownMap.put(playerName,System.currentTimeMillis());
  }
}
