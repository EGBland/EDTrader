package net.shinonomelabs.edtrader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.Buffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SaveDataManager {
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MMM-d HH:mm:ss Z");
  public static final String SAVE_DIR =
      (System.getProperty("os.name").startsWith("Windows"))
          ? System.getProperty("user.home") + "\\AppData\\Local\\edtrader\\" // windows
          : System.getProperty("user.home") + "/.edtrader/"; // linux
  private static final List<StarSystem> starSystems = new ArrayList<>();
  private static final List<StarSystem> coreSystems = new ArrayList<>();
  private static Date lastUpdated = new Date(946684800); // 2000-Jan-01 00:00:00 +0000

  public static Date getLastUpdated() {
    synchronized (lastUpdated) {
      return new Date(lastUpdated.getTime());
    }
  }

  private static JSONObject pack() {
    JSONObject save = new JSONObject();

    // star systems
    JSONArray systemsJson = new JSONArray();
    synchronized (starSystems) {
      starSystems.forEach(s -> systemsJson.put(s.getJson()));
    }
    save.put("populated_systems", save);

    // last updated
    synchronized (lastUpdated) {
      save.put("populated_systems_last_updated", DATE_FORMAT.format(lastUpdated));
    }

    return save;
  }

  private static void unpack(JSONObject save) {
    // star systems
    JSONArray systemsFromJson = save.getJSONArray("populated_systems");
    synchronized (starSystems) {
      synchronized (coreSystems) { // careful with the order
        systemsFromJson.forEach(
            system -> {
              StarSystem s = new StarSystem((JSONObject) system);
              starSystems.add(s);
              if (s.distanceFromSol() <= 150) coreSystems.add(s);
            });
      }
    }

    // last updated
    String dateFromJson = save.getString("populated_systems_last_updated");
    try {
      synchronized (lastUpdated) {
        lastUpdated = DATE_FORMAT.parse(dateFromJson);
      }
    } catch (ParseException ex) {
      // TODO handle
      ex.printStackTrace();
    }
  }

  public static void save() {
    new File(SAVE_DIR).mkdirs();
    File savefile = new File(SAVE_DIR + File.separator + "data.json");

    JSONObject save = pack();

    try (FileWriter fw = new FileWriter(savefile)) {
      fw.write(save.toString());
    } catch (IOException ex) {
      // TODO handle
      ex.printStackTrace();
    }
  }

  public static boolean load() {
    File savefile = new File(SAVE_DIR + File.separator + "data.json");
    if (savefile.exists()) {
      try (FileReader fr = new FileReader(savefile)) {
        BufferedReader br = new BufferedReader(fr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
          sb.append(line);
        }

        unpack(new JSONObject(sb.toString()));

        return true;
      } catch (IOException ex) {
        // TODO handle
        ex.printStackTrace();
        return false;
      }
    } else return false;
  }

  public static DownloadManager updateJson() {
    return new DownloadManager("https://eddb.io/archive/v6/systems_populated.json") {
      @Override
      public void whenDownloaded() {
        String str = new String(this.getData());
        JSONArray jsonobj = new JSONArray(str);
        synchronized (starSystems) {
          jsonobj.forEach(
              s -> {
                starSystems.add(new StarSystem((JSONObject) s));
              });
        }
        synchronized (lastUpdated) {
          lastUpdated = new Date();
        }
        save();
      }
    };
  }
}
