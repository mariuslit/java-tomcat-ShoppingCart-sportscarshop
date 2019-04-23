package lt.bta.java2.api.services;

import lt.bta.java2.jpa.services.Dao;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * pagrindinių CRUD operacijų bazinis servisas
 * @param <T>
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public abstract class BaseService<T> {

    protected abstract Class<T> getEntityClass();

    protected Dao<T> createDao() {
        return new Dao<>(getEntityClass());
    }

    // CRUD

    @POST // CREATE
    public Response add(T e) {

        Dao<T> dao = createDao();
        dao.create(e);
        return Response.ok(e).build();
    }

    @GET  // READ
    @Path("/{id}")
    public Response get(@PathParam("id") int id) {

        Dao<T> dao = createDao();
        T entity = dao.read(id);
        if (entity == null) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(entity).build();
    }

    @GET // READ_LIST
    @Path("/list")
    public Response list(@QueryParam("size") @DefaultValue("10") int size, @QueryParam("skip") @DefaultValue("0") int skip) {

        Dao<T> dao = createDao();
        return Response.ok().entity(dao.list(size, skip)).build();
    }

    @PUT // UPDATE
    @Path("/{id}")
    public Response update(@PathParam("id") int id, T e) {

        Dao<T> dao = createDao();
        T entity = dao.read(id);
        if (entity == null) return Response.status(Response.Status.NOT_FOUND).build();
        dao.update(e);
        return Response.ok(e).build();
    }

    @DELETE // DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {

        Dao<T> dao = createDao();
        T entity = dao.read(id);
        if (entity == null) return Response.status(Response.Status.NOT_FOUND).build();
        dao.delete(entity);
        return Response.ok().build();
    }
}