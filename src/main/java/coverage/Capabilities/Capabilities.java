package coverage.Capabilities;

import java.util.ArrayList;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;


public class Capabilities extends ReactivePanacheMongoEntity {

  public String name;
  public String description;
 
 
  ArrayList<String> entryPoints;
  ArrayList<String> product;
  ArrayList<String> clientRef;


  Capabilities updateFields(Capabilities a) {
    this.name = a.name;
    this.description = a.description;
    this.entryPoints = a.entryPoints;

    return this;
  }
}
