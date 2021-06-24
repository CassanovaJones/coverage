package coverage;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.LinkedHashMap;
import javax.inject.Inject;
import org.jboss.logging.Logger;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class AccountsTest {

  final String accountName = "Test Account";
  final String newName = "New Account Name";

  @Inject
  Logger log;

  String exampleAccountJson() {
    return new JSONObject()
      .put("name", accountName)
      .put("address", "Main St")
      .put("city", "Baton Rouge")
      .put("state", "LA")
      .put("zip", "70113")
      .toString();
  }

  String convertToJsonString(LinkedHashMap<String, String> a) {
    return new JSONObject()
      .put("name", a.get("name"))
      .put("address", a.get("address"))
      .put("city", a.get("city"))
      .put("state", a.get("state"))
      .put("zip", a.get("zip"))
      .toString();
  }

  @Test
  public void testAccountsEndpoint() {
    given().when().get("/accounts").then().statusCode(200);
  }

  @Test
  public void testAccountAddAndDeleteAll() {
    String a = exampleAccountJson();

    given().when().delete("/accounts").then().statusCode(200);

    String addedAccountURI = given()
      .contentType(ContentType.JSON)
      .body(a)
      .when()
      .post("/accounts")
      .then()
      .statusCode(201)
      .extract()
      .response()
      .asString();

    given()
      .when()
      .get(addedAccountURI)
      .then()
      .statusCode(200)
      .body("name", equalTo(accountName));

    given().when().delete("/accounts").then().statusCode(200);
  }

  @Test
  public void testUpdateAccount() {
    String a = exampleAccountJson();

    given().when().delete("/accounts").then().statusCode(200);

    String addedAccountURI = given()
      .contentType(ContentType.JSON)
      .body(a)
      .when()
      .post("/accounts")
      .then()
      .statusCode(201)
      .extract()
      .response()
      .asString();

    LinkedHashMap<String, String> a_map = given()
      .when()
      .get(addedAccountURI)
      .then()
      .statusCode(200)
      .extract()
      .path("$");

    String id = a_map.get("id");
    a_map.put("name", newName);
    String a1 = new JSONObject(a_map).toString();

    given()
      .pathParam("id", id)
      .contentType(ContentType.JSON)
      .body(a1)
      .when()
      .put("/accounts/{id}")
      .then()
      .statusCode(200);

    given()
      .when()
      .get(addedAccountURI)
      .then()
      .statusCode(200)
      .body("name", equalTo(newName));

    given().when().delete("/accounts").then().statusCode(200);
  }

  @Test
  public void getAccount() {
    String a = exampleAccountJson();

    given().when().delete("/accounts").then().statusCode(200);

    given()
      .contentType(ContentType.JSON)
      .body(a)
      .when()
      .post("/accounts")
      .then()
      .statusCode(201);

    String id = given()
      .when()
      .get("/accounts")
      .then()
      .statusCode(200)
      .extract()
      .path("[0].id");

    given()
      .pathParam("id", id)
      .when()
      .get("/accounts/{id}")
      .then()
      .statusCode(200);

    given().when().delete("/accounts").then().statusCode(200);
  }

  @Test
  public void failOnBadAccountId() {
    given().when().delete("/accounts").then().statusCode(200);

    given()
      .pathParam("id", "60d1434f7fe4d40a3c74d8c7")
      .when()
      .get("/accounts/{id}")
      .then()
      .statusCode(404);
  }

  @Test
  public void testDeleteAccount() {
    String a = exampleAccountJson();

    given().when().delete("/accounts").then().statusCode(200);

    given()
      .contentType(ContentType.JSON)
      .body(a)
      .when()
      .post("/accounts")
      .then()
      .statusCode(201);

    String id = given()
      .when()
      .get("/accounts")
      .then()
      .statusCode(200)
      .extract()
      .path("[0].id");

    given()
      .pathParam("id", id)
      .when()
      .delete("/accounts/{id}")
      .then()
      .statusCode(200);

    given().when().delete("/accounts").then().statusCode(200);
  }
}
