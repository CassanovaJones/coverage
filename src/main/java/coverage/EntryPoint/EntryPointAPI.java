package coverage.EntryPoint;

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



@Path("/entryPoints")
public class EntryPointAPI {
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Response> list() {
    return EntryPoint
      .listAll()
      .onItem()
      .transform(ep -> Response.ok().entity(ep).build())
      .onFailure()
      .recoverWithItem(err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build());
  }
  @GET
  @Path("/{entryPointId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> getEp(@PathParam("entryPointId") String id) {
    return EntryPoint
      .findByIdOptional(new ObjectId(id))
      .onItem()
      .transform(
        capOp -> {
          if (capOp.isPresent()){
            return Response.ok().entity(capOp).build();  
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
  public Uni<Response> addEntryPoint(EntryPoint a, @Context UriInfo uriInfo) {
    return a
      .persist()
      .onItem()
      .transform(i -> Response.status(Status.CREATED).build())
      .onFailure()
      .recoverWithItem(
        err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build()
      );
  }
  @PUT
  @Path("/{entryPintId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> updateEntryPoint(
    @PathParam("entryPointId") String id, EntryPoint ep) {
    return EntryPoint
      .<EntryPoint>findByIdOptional(new ObjectId(id))
      .onItem()
      .transformToUni(
        epo -> {
          if (epo.isPresent()) {
            EntryPoint ep1 = epo.get();
            ep1.name = ep.name;
            ep1.description = ep.description;

            return ep1
              .update()
              .onItem()
              .transform(i -> Response.ok().build())
              .onFailure()
              .recoverWithItem(
                err -> Response
                  .status(Status.INTERNAL_SERVER_ERROR)
                  .entity(err).build()
              );
          } else {
            return Uni
              .createFrom()
              .item(Response.status(Status.NOT_FOUND).entity(ep).build());
          }
        }
      )
      .onFailure()
      .recoverWithItem(err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build());
  }
  @DELETE
  public Uni<Response> deleteAllEntryPoint() {
    return EntryPoint
      .deleteAll()
      .onItem()
      .transform(count -> Response.ok().entity(count).build())
      .onFailure()
      .recoverWithItem(Response.status(Status.INTERNAL_SERVER_ERROR).build());
  }
  @DELETE
  @Path("/{entryPointId}")
  public Uni<Response> deleteEntryPointById(@PathParam("entryPointId") String id) {
    return EntryPoint
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
  @Path("{/entryPointId}/capabilities/{capabilityId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> addCapability(@PathParam("entryPointId") String epId, @PathParam("capabilityId") String capId) {
    return EntryPoint
      .<EntryPoint>findByIdOptional(new ObjectId(epId))
      .onItem()
      .transformToUni(                                            //Prevents two Unis from being returned
        epOpt -> {
          if (epOpt.isPresent()) {
                   epOpt.get().capabilities.add(capId);
            return epOpt.get().update();
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
      .recoverWithItem( err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build()
      );
  }
  @POST
  @Path("{/entryPointId}/products/{productId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> addProduct(@PathParam("entryPointId") String epId, @PathParam("productId") String prodId) {
    return EntryPoint
      .<EntryPoint>findByIdOptional(new ObjectId(epId))
      .onItem()
      .transformToUni(                                            //Prevents two Unis from being returned
        epOpt -> {
          if (epOpt.isPresent()) {
                   epOpt.get().products.add(prodId);
            return epOpt.get().update();
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
      .recoverWithItem( err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build()
      );
  }
  @POST
  @Path("{/entryPointId}/isAssociatedWith/{capabilityId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<Response> addClientRef(@PathParam("entryPointId") String epId, @PathParam("clientRefId") String cfId) {
    return EntryPoint
      .<EntryPoint>findByIdOptional(new ObjectId(epId))
      .onItem()
      .transformToUni(                                            //Prevents two Unis from being returned
        epOpt -> {
          if (epOpt.isPresent()) {
                   epOpt.get().capabilities.add(cfId);
            return epOpt.get().update();
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
      .recoverWithItem( err -> Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build()
      );
  }
}