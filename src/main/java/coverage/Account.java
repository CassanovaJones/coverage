package coverage;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import java.util.ArrayList;

public class Account extends ReactivePanacheMongoEntity {

  public String name;
  public String address;
  public String city;
  public String state;
  public String zip;

  ArrayList<String> salesPeople;

  Account updateFields(Account a) {
    this.name = a.name;
    this.address = a.address;
    this.city = a.city;
    this.state = a.state;
    this.zip = a.zip;

    return this;
  }
}
