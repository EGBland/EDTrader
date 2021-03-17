package net.shinonomelabs.edtrader;

import net.shinonomelabs.edtrader.graph.Graph;
import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class EDTrader {
  public static void main(String[] args) {
    System.out.println("Loading");
    if (SaveDataManager.load()) {
      long hoursSince =
          ChronoUnit.HOURS.between(
              SaveDataManager.getLastUpdated().toInstant(), new Date().toInstant());
      System.out.println("JSON last updated " + hoursSince + " hours ago");
      if (hoursSince >= 24) {
        System.out.println("Savefile outdated, updating");
        DownloadManager mgr = SaveDataManager.updateJson();
        mgr.start();
        while (!mgr.isFinished())
          ;
      }
    } else {
      System.out.println("Save file does not exist, creating");
      DownloadManager mgr = SaveDataManager.updateJson();
      mgr.start();
      while (!mgr.isFinished())
        ;
    }
  }
}
