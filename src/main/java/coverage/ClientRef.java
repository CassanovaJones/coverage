package coverage;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import java.util.ArrayList;

public class ClientRef extends ReactivePanacheMongoEntity {

  public String name;
  public String product;
  public String results;

  ArrayList<String> entryPoints;

  ClientRef updateFields(ClientRef a) {
    this.name = a.name;
    this.product = a.product;
    this.results = a.results;

    return this;
  }
}