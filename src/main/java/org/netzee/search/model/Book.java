package org.netzee.search.model;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

@Entity
@Indexed
public class Book extends PanacheEntity {

  @FullTextField(analyzer = "name")
  public String title;

  @ManyToOne
  @JsonIgnore
  public Author author;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Book)) {
      return false;
    }

    Book other = (Book) o;

    return Objects.equals(id, other.id);
  }

  @Override
  public int hashCode() {
    return 31;
  }
}
