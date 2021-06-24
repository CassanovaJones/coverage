package coverage;

import io.smallrye.mutiny.Uni;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
// import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.bson.types.ObjectId;

/* The Path annotation sets up the base path for all the API entry points */
@Path("/accounts")
public class AccountServiceAPI {

  /* The annotations describe the HTTP METHOD verb and what data format the API consumes or produces */

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Response> list() {
    /* 
    This method returns a Response wrapped in the Uni class from the Quarkus Reactive library called Mutiny. 
    
    First, we ask the Account class to list all the accounts in the database. This is a reactive (Mutiny / asynchronous) call so we have to set up the instructions on what to do when it succeeds (onItem) and what to do if it fails.  In either case we need to return a HTTP reponse to the caller. 

    In the onItem branch, transform the item (account list) into a Reponse with status ok (200) and put the list of accounts in the body (entity) of the reponse. 
    
    In the case of failure, we recover from the failure by supplying an HTTP Response that there was a server error since this method always succeeds even if there are no records in the database. So, if we get onFailure, it has to be a more catastrophic error. We received the information on what actually caused the failure (err), so pass that back in the body of the reponse. 
    
    Both the transform method and the recoverWithItem method take the response returned from the lambda and wraps it in a Uni to satisfy the return type of the list() method. So, we can just return the Response from these methods and Quarkus magic makes it work for us. 
    */

    return Account
      .listAll()
      .onItem()
      .transform(accts -> Response.ok().entity(accts).build())
      .onFailure()
      .recoverWithItem(
        err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build()
      );
  }

  /* 
  The Path annotated here is concatenated onto the previous Path specified at the class level. So, the path to retrieve a specific account record is '.../accounts/{accountId}' where accountId is an actual valid hex object id.

  The PathParam annotation in the method signature tells Quarkus that the accountId passed in on the path is to be placed in the method argument 'id'.
  */

  @GET
  @Path("/{accountId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Response> getAccount(@PathParam("accountId") String id) {
    /* 
    We first try to find the account given the id passed in. The findByIdOptional method will return an Optional of type Account. An optional is the modern way to bypass a lot of null checking plus some other enhancements. Basically, the Optional itself is never null. But, it may or may not contain an Account within itself. 

    Contrast findByIdOptional to plain findById. The latter will return null if it does not find an Account with the provided id.

    In the onItem "branch", we call transform to create an appropriate Response. The variable, ao, is an optional of Account. 
    
    Check if the optional actually contains an account (isPresent). If so, return a Response with ok and the entity set to the contents of the optional (ao.get) so that the account information is passed back in the body. 
    
    If there is not an account present, return a Response that states that the given account was not found. 

    Since the findByIdOptional produces an item (ao) whether it finds an account or not, the failure branch means we've had a more catastrophic failure. That is, it is not simply that we couldn't find the account -- which is actually a successful completion. The failure is actually something like the network is down or the database is unreachable. Therefore, return a Response stating there is an internal server error, and also pass back the error object as the body of the reponse. 

    */
    return Account
      .findByIdOptional(new ObjectId(id))
      .onItem()
      .transform(
        ao -> {
          if (ao.isPresent()) {
            return Response.ok().entity(ao.get()).build();
          } else {
            return Response.status(Status.NOT_FOUND).build();
          }
        }
      )
      .onFailure()
      .recoverWithItem(
        err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build()
      );
  }

  /* 
  In this endpoint, we are creating a new account by posting to the root url (/accounts) and the JSON string containing the Account data is passed in the body of the request. Quarkus takes the JSON body, and converts it to an Account object and fills the method parameter (a).
  */

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> addAccount(Account a, @Context UriInfo uriInfo) {
    /* 
    First a todo: the account object needs to be validated. Quarkus fills the Account object by looking in the JSON string for elements whose key is the name of one of the fields of the Account class (name, city, etc.). If it finds one, it places that JSON element in the appropriate field of the passed in account object. It ignores all other fields. So, if someone passes in JSON that doesn't have some or all of the required data in the JSON string, then the Account object could be empty or invalid.

    For now, we assume valid account data has been passed in. We tell the account to persist (save / store) itself. This is a reactive call like the others, so we need to handle success (onItem) and failure (onFailure). The persist method actually returns a void (Uni<Void> to be precise), so the lambda in the transform gets a null parameter (v) -- do not attempt to use it. Return a reponse indicating that the account was created. 

    */

    return a
      .persist()
      .onItem()
      .transform(
        v ->
          Response
            .status(Status.CREATED)
            .entity(
              uriInfo
                .getAbsolutePathBuilder()
                .segment(a.id.toString())
                .build()
                .toString()
            )
            .build()
      )
      .onFailure()
      .recoverWithItem(
        err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build()
      );
  }

  /* 

  Like the POST, this method needs the validation code written. 

  This is the most complex method in this program. It combines all the features of the other methods, plus some other complexities. 

  The <Account>.update method does not actually return a "succeeded" boolean. Like create, it returns a void. So, we need to verify that the record being updated actually exists before we try to update it. Therefore, we end up with two reactive calls. The first is the findByIdOptional like we saw above. The other is the update method. The complexity is that we should only call the update method if the account actually exists. Therfore, the update method must be a part of the onItem clause  of the findByIdOptional call. The update call is nested inside. 

  The main method (updateAccount) wants to return a Uni<Response> to Quarkus. Remember, whenever "transform" is called, it takes whatever is return from its lambda and wraps it in a Uni. With nested reactive calls, the inner one (a1.update)returns a Uni<Response> coming out of its transform. 
  
  On the outer one, if we use just the normal "transform", it will receive the Uni<Response> from the inner and wrap it in a Uni. So, we end up with a Uni<Uni<Response>> which makes my head hurt, and it is not want we want to return from the method -- we need to return Uni<Response>. 
  
  To fix the Uni<Uni<Response>> problem, we use transformToUni for the outer onItem. Essentially, transformToUni is expecting that it will receive a Uni to return. So, it just skips wrapping the Uni<Response> in another Uni and merely returns the Uni<Response> directly. 

  One more complexity. Because of the way that Panache (ActiveRecord pattern) works, if you are trying to update an existing record in the database, the Account object has to be created by querying the database. The incoming account (a) is just a POJO. The database does not know about it. So, we use the accountId path parameter to perform the findByIdOptional command like before. If it finds it, the optional will have an Account present, and that account object the database gave us so it is linked. 

  Once we have a linked account (a1), we need to update all the fields in that account (a1) with the fields passed into the main method in the account parameter (a) which was created from the JSON body of the request. We can then update the linked account (a1) and when that completes (onItem), return a success Response. 

  */

  @PUT
  @Path("/{accountId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> updateAccount(
    @PathParam("accountId") String id,
    Account a
  ) {
    return Account
      .<Account>findByIdOptional(new ObjectId(id))
      .onItem()
      .transformToUni(
        // Received an optional from the find, so we have to check if it is present
        ao -> {
          if (ao.isPresent()) {
            /* 
            We found the account. Get the account object from the optional. a1 is linked to the database and it has all the current values of the record. 
            */
            Account a1 = ao.get();

            /* 
            Update the fields of a1 with all the fields from the account passed into the method (a)
            */
            a1.updateFields(a);

            /* 
            Now we can update the database with the values in the linked account (a1). This is a reactive call, and so we have to handle its completion (onItem) and return the appropriate Response. 
            
            If something went wrong (onFailure), we know it cannot be that the record doesn't exist because we are only in this code if it does exist. So, the error must be something more catastrphic. This onFailure could occur if the network crashed in-between the call to findByIdOptional and a1.update. 
            */
            return a1
              .update()
              .onItem()
              .transform(v -> Response.ok().build())
              .onFailure()
              .recoverWithItem(
                err ->
                  Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity(err)
                    .build()
              );
          } else {
            /* 
            This else clause gets executed only if the findByIdOptional returns an Optional with no Account inside - meaning the accountId passed in does not exist in the database. 
            */
            return Uni
              .createFrom()
              .item(Response.status(Status.NOT_FOUND).entity(a).build());
          }
        }
      )
      /* 
      This onFailure clause is on the findByIdOptional call. Remember, that method will always return an Optional (with a value present or not) unless there is some catastrophic error. 
      */
      .onFailure()
      .recoverWithItem(
        err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build()
      );
  }

  /* 
  The DELETE method on the base URL will call the Account.deleteAll method. On completion of the call (onItem), the item that is returned is the count of the records that were deleted. Return a Response of ok and place the count in the body of the response. 
  */

  @DELETE
  public Uni<Response> deleteAllAccounts() {
    return Account
      .deleteAll()
      .onItem()
      .transform(count -> Response.ok().entity(count).build())
      .onFailure()
      .recoverWithItem(Response.status(Status.INTERNAL_SERVER_ERROR).build());
  }

  /* 
  This one is very similar to the GET /accounts/{accountID}. See those comments. The difference is that onItem produces a boolean that states whether the record was deleted. Check for success and return the appropriate Response. 
  */

  @DELETE
  @Path("/{accountId}")
  public Uni<Response> deleteAccountById(@PathParam("accountId") String id) {
    return Account
      .deleteById(new ObjectId(id))
      .onItem()
      .transform(
        succeeded -> {
          if (succeeded) {
            return Response.ok().build();
          } else {
            return Response.status(Status.NOT_FOUND).build();
          }
        }
      )
      .onFailure()
      .recoverWithItem(Response.status(Status.INTERNAL_SERVER_ERROR).build());
  }
  // @PATCH
  // @Path("/{capabilityId}/hasEntryPoint/{entryPointId}")
  // public Uni<Response> addEntryPoint(@PathParam("capabilityId") String capId, @PathParam("entryPointId") String epId) {

  //   // Find the capability by capId - store in variable cap
  //   // cap.entrPoints.append(epId);
  //   // cap.update
  //   // Return Response

  // }

}
