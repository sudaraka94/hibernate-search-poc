package org.netzee.search;


import jakarta.ws.rs.Consumes;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.search.engine.spatial.DistanceUnit;
import org.hibernate.search.engine.spatial.GeoPoint;
import org.netzee.search.model.Author;
import org.netzee.search.model.Book;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;

import io.quarkus.runtime.StartupEvent;

@Path("/library")
public class LibraryResource {
  @Inject
  SearchSession searchSession;

  @ConfigProperty(name = "quarkus.datasource.password")
  String pass;

  @ConfigProperty(name = "quarkus.datasource.username")
  String username;


  // only needed since we are importing data to postgres using sql script
  @Transactional
  void onStart(@Observes StartupEvent ev) throws InterruptedException {
    // only reindex if we imported some content
    if (Book.count() > 0) {
      searchSession.massIndexer()
          .startAndWait();
    }

    System.out.println("Password: " + pass);
    System.out.println("Username: " + username);
  }


  @PUT
  @Path("book")
  @Transactional
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public void addBook(@RestForm String title, @RestForm Long authorId) {
    Author author = Author.findById(authorId);
    if (author == null) {
      return;
    }

    Book book = new Book();
    book.title = title;
    book.author = author;
    book.persist();

    author.books.add(book);
    author.persist();
  }

  @DELETE
  @Path("book/{id}")
  @Transactional
  public void deleteBook(Long id) {
    Book book = Book.findById(id);
    if (book != null) {
      book.author.books.remove(book);
      book.delete();
    }
  }

  @PUT
  @Path("author")
  @Transactional
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public void addAuthor(@RestForm String firstName, @RestForm String lastName) {
    Author author = new Author();
    author.firstName = firstName;
    author.lastName = lastName;
    author.persist();
  }

  @POST
  @Path("author/{id}")
  @Transactional
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public void updateAuthor(Long id, @RestForm String firstName, @RestForm String lastName) {
    Author author = Author.findById(id);
    if (author == null) {
      return;
    }
    author.firstName = firstName;
    author.lastName = lastName;
    author.persist();
  }

  @DELETE
  @Path("author/{id}")
  @Transactional
  public void deleteAuthor(Long id) {
    Author author = Author.findById(id);
    if (author != null) {
      author.delete();
    }
  }

  @GET
  @Path("author/search")
  @Transactional
  public List<Author> searchAuthors(@RestQuery String pattern,
      @RestQuery Optional<Integer> size) {
    return searchSession.search(Author.class)
        .where(f ->
            pattern == null || pattern.trim().isEmpty() ?
                f.matchAll() :
                f.simpleQueryString()
                    .fields("firstName", "lastName", "books.title").matching(pattern)
        )
        .sort(f -> f.field("lastName_sort").then().field("firstName_sort"))
        .fetchHits(size.orElse(20));
  }

  @GET
  @Path("author/location-search")
  @Transactional
  public List<Author> searchAuthorsByLocation() {
    GeoPoint center = GeoPoint.of( 6.900777, 79.860133 );
    return searchSession.search( Author.class )
        .where( f -> f.spatial().within().field( "location" )
            .circle( center, 50, DistanceUnit.KILOMETERS ) )
        .fetchHits( 20 );
  }
}
