package coverage.ClientRef;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import java.util.ArrayList;

public class ClientRef extends ReactivePanacheMongoEntity {

  public String name;
  public String results;

  ArrayList<String> entryPoints;
  ArrayList<String> products;
  ArrayList<String> capabilities;

  ClientRef updateFields(ClientRef a) {
    this.name = a.name;
    this.results = a.results;

    return this;
  }
}