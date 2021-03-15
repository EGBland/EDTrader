package net.shinonomelabs.edtrader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.Buffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SimpleTimeZone;

public class SaveDataManager {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MMM-d HH:mm:ss Z");

    public static final String SAVE_DIR =
            (System.getProperty("os.name").startsWith("Windows")) ?
                    System.getProperty("user.home") + "\\AppData\\Local\\edtrader\\"    // windows
                  : System.getProperty("user.home") + "/.edtrader/";                    // linux

    private static JSONObject data = new JSONObject();

    public static void save() {
        new File(SAVE_DIR).mkdirs();
        File savefile = new File(SAVE_DIR + File.separator + "data.json");
        try (FileWriter fw = new FileWriter(savefile)) {
            synchronized(data) {
                fw.write(data.toString());
            }
        } catch(IOException ex) {
            // TODO handle
            ex.printStackTrace();
        }
    }

    public static Object getData(String key, Object deflt) {
        synchronized(data) {
            if(data.keySet().contains(key)) {
                return data.get(key);
            }
            else {
                return deflt;
            }
        }
    }

    public static void load() {
        File savefile = new File(SAVE_DIR + File.separator + "data.json");
        if (savefile.exists()) {
            try(FileReader fr = new FileReader(savefile)) {
                BufferedReader br = new BufferedReader(fr);
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = br.readLine()) != null) {
                    sb.append(line);
                }

                JSONObject save = new JSONObject(sb.toString());
                synchronized(data) {
                    data = save; // this might be dodgy
                }
                //System.out.println();
            } catch(IOException ex) {
                // TODO handle
                ex.printStackTrace();
            }
        }
    }

    public static DownloadManager updateJson() {
        return new DownloadManager("https://eddb.io/archive/v6/systems_populated.json") {
            @Override
            public void whenDownloaded() {
                String str = new String(this.getData());
                JSONArray jsonobj = new JSONArray(str);
                synchronized(data) {
                    data.put("populated_systems", jsonobj);
                    data.put("populated_systems_last_updated", DATE_FORMAT.format(new Date()));
                }

                save();
            }
        };
    }
}
