package net.shinonomelabs.edtrader;

import net.shinonomelabs.edtrader.graph.Graph;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class EDTrader {
    public static void main(String[] args) {
        System.out.println("OS is " + System.getProperty("os.name"));
        System.out.println("Save location is " + SaveDataManager.SAVE_DIR);
        System.out.println("Loading data");
        SaveDataManager.load();
        Date date = null;
        try {
            date = SaveDataManager.DATE_FORMAT.parse(SaveDataManager.getData("populated_systems_last_updated", "2000-Jan-01 00:00:00 +0000").toString());
        }
        catch(ParseException ex) {
            // TODO handle, shouldn't happen though
            ex.printStackTrace();
        }
        Date now = new Date();
        long hoursSinceUpdate = ChronoUnit.HOURS.between(date.toInstant(), now.toInstant());
        System.out.println("JSON is " + hoursSinceUpdate + " hours old");
        if(hoursSinceUpdate >= 24) {
            System.out.println("JSON is outdated, updating...");
            DownloadManager mgr = SaveDataManager.updateJson();
            mgr.start();
            TimerTask tt = new TimerTask() {
                public void run() {
                    System.out.println(mgr.getAmountDownloaded());
                    if(mgr.isFinished()) cancel();
                }
            };

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(tt, 0L, 500L);
            while(!mgr.isFinished());
        }
        System.out.println("Finding core systems");
        JSONArray systems = (JSONArray)SaveDataManager.getData("populated_systems", new JSONArray());
        List<HashMap<Object,Object>> core = systems.toList().stream().filter(map -> {
            HashMap<Object,Object> cast = (HashMap<Object,Object>) map;
            // TODO make this not shit
            double x = ((Number)cast.get("x")).doubleValue();
            double y = ((Number)cast.get("y")).doubleValue();
            double z = ((Number)cast.get("z")).doubleValue();
            return x*x + y*y + z*z < 150*150;
        }).map(obj -> (HashMap<Object,Object>) obj).collect(Collectors.toList());
        System.out.println("There are " + core.size() + " populated core systems");
        final int mly = (int)SaveDataManager.getData("max_light_years",30);
        System.out.println("Generating graph (limit = " + mly + " ly)");
        Graph<String,Double> systemGraph = new Graph<>();
        for(int i = 0; i < core.size(); i++) {
            for(int j = i + 1; j < core.size(); j++) {
                HashMap<Object,Object> system1 = core.get(i);
                HashMap<Object,Object> system2 = core.get(j);
                double x1 = ((Number)system1.get("x")).doubleValue();
                double y1 = ((Number)system1.get("y")).doubleValue();
                double z1 = ((Number)system1.get("z")).doubleValue();
                double x2 = ((Number)system2.get("x")).doubleValue();
                double y2 = ((Number)system2.get("y")).doubleValue();
                double z2 = ((Number)system2.get("z")).doubleValue();

                double dist = (x1*x1 + y1*y1 + z1*z1) - (x2*x2 + y2*y2 + z2*z2);
                if(dist < mly*mly) {
                    systemGraph.addArc((String)system1.get("name"), (String)system2.get("name"), dist);
                }
            }
            System.out.println(i);
        }
    }
}
