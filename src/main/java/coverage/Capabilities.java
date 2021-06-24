package coverage;

import java.util.List;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;

public class Capabilities extends ReactivePanacheMongoEntity {

  public String name;
  public String description;
  public List<String> entryPoints;
}
