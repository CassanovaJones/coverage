package coverage;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;

public class ClientRef extends ReactivePanacheMongoEntity {

  public String name;
  public String product;
  public String results;
}