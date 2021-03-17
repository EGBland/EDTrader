package net.shinonomelabs.edtrader;

import org.javatuples.Triplet;
import org.json.JSONObject;

public class StarSystem {
  private final JSONObject data;

  public StarSystem(JSONObject data) {
    this.data = data;
  }

  public String getName() {
    return data.get("name").toString();
  }

  public double getX() {
    return ((Number) data.get("x")).doubleValue();
  }

  public double getY() {
    return ((Number) data.get("y")).doubleValue();
  }

  public double getZ() {
    return ((Number) data.get("z")).doubleValue();
  }

  public double distanceFrom(StarSystem s) {
    double x = getX() - s.getX(), y = getY() - s.getY(), z = getZ() - s.getZ();
    return Math.sqrt(x * x + y * y + z * z);
  }

  public double distanceFromSol() {
    return Math.sqrt(getX() * getX() + getY() * getY() + getZ() * getZ());
  }

  JSONObject getJson() {
    return data;
  }
}
