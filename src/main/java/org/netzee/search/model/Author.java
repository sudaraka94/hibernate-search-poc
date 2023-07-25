package org.netzee.search.model;

import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.bridge.builtin.annotation.GeoPointBinding;
import org.hibernate.search.mapper.pojo.bridge.builtin.annotation.Latitude;
import org.hibernate.search.mapper.pojo.bridge.builtin.annotation.Longitude;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

@Entity
@Indexed
@GeoPointBinding(fieldName = "location")
public class Author extends PanacheEntity {

  @FullTextField(analyzer = "name")
  @KeywordField(name = "firstName_sort", sortable = Sortable.YES, normalizer = "sort")
  public String firstName;

  @FullTextField(analyzer = "name")
  @KeywordField(name = "lastName_sort", sortable = Sortable.YES, normalizer = "sort")
  public String lastName;

  @Latitude
  public Double locationLatitude=0.0;
  @Longitude
  public Double locationLongitude=0.0;

  @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @IndexedEmbedded
  public List<Book> books;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Author)) {
      return false;
    }

    Author other = (Author) o;

    return Objects.equals(id, other.id);
  }

  @Override
  public int hashCode() {
    return 31;
  }
}
