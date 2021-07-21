package coverage.Product;

import io.smallrye.mutiny.Uni;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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

import coverage.Account;

/* The Path annotation sets up the base path for all the API entry points */
@Path("/product")
public class ProductServiceAPI {
    @GET 
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> list() {
        return Product
        .listAll()
        .onItem()
        .transform(prod -> Response.ok().entity(prod).build())
        .onFailure()
        .recoverWithItem(
            err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build()
      );
    }

  @GET 
  @Path("/{productId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Response> getProduct(@PathParam("productId") String id) {
      return Product
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

  @POST 
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> addProduct(Product a, @Context UriInfo uriInfo) {
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

  @PUT
  @Path("/{productId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> updateAccount(
      @PathParam("productId") String id,
      Product a
  )   {
      return Product
        .<Product>findByIdOptional(new ObjectId(id))
        .onItem()
        .transformToUni(
          ao -> {
            if (ao.isPresent()) {
              Product a1 = ao.get();
              a1.updateFields(a);
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
              return Uni
                .createFrom()
                .item(Response.status(Status.NOT_FOUND).entity(a).build());
            }
          }
        )
        .onFailure()
        .recoverWithItem(
          err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build()
        );
  }

  @DELETE
  public Uni<Response> deleteAllAccounts() {
      return Account
        .deleteAll()
        .onItem()
        .transform(count -> Response.ok().entity(count).build())
        .onFailure()
        .recoverWithItem(Response.status(Status.INTERNAL_SERVER_ERROR).build());
  }

  @DELETE
  @Path("/{productId}")
  public Uni<Response> deleteProductById(@PathParam("productId") String id) {
      return Product
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

  @POST
  @Path("/{productId}/clientReferences/{clientRefId}")
  public Uni<Response> addClientRef(@PathParam("productId") String productId, @PathParam("clientRefId") String cfId) {
    return Product  
      .<Product>findByIdOptional(new ObjectId(productId))
      .onItem()
      .transformToUni(
        pr -> {
          if (pr.isPresent()) {
                   pr.get().clientRef.add(cfId);
            return pr.get().update();    
          } else {
            throw new NotFoundException();
          }
        }
      )
      .onItem()
      .transform(v -> {return Response.ok().build();})
      .onFailure(
        error -> {
          return error.getClass() == NotFoundException.class;
        }
      )
      .recoverWithItem(Response.status(Status.NOT_FOUND).build())
      .onFailure()
      .recoverWithItem(err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build());
  }

  @POST
  @Path("/{productId}/entryPoints/{entryPointId}")
  public Uni<Response> addEntryPoint(@PathParam("productId") String prodId, @PathParam("entryPointId") String epId) {
    return Product  
      .<Product>findByIdOptional(new ObjectId(prodId))
      .onItem()
      .transformToUni(
        pr -> {
          if (pr.isPresent()) {
                   pr.get().entryPoints.add(epId);
            return pr.get().update();    
          } else {
            throw new NotFoundException();
          }
        }
      )
      .onItem()
      .transform(v -> {return Response.ok().build();})
      .onFailure(
        error -> {
          return error.getClass() == NotFoundException.class;
        }
      )
      .recoverWithItem(Response.status(Status.NOT_FOUND).build())
      .onFailure()
      .recoverWithItem(err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build());
  }
  @POST
  @Path("/{productId}/capabilities/{capabilityId}")
  public Uni<Response> addCap(@PathParam("productId") String prodId, @PathParam("capabilityId") String capId) {
    return Product
      .<Product>findByIdOptional(new ObjectId(prodId))
      .onItem()
      .transformToUni(
        pr -> {
          if (pr.isPresent()) {
                   pr.get().capabilities.add(capId);
            return pr.get().update();
          } else {
            throw new NotFoundException();
          }
        }
      )
      .onItem()
      .transform(v -> {return Response.ok().build();})
      .onFailure(
        error -> {
          return error.getClass() == NotFoundException.class;
        }
      )
      .recoverWithItem(Response.status(Status.NOT_FOUND).build())
      .onFailure()
      .recoverWithItem(err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build());
  }
}
