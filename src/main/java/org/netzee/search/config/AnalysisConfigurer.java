package org.netzee.search.config;

import io.quarkus.hibernate.search.orm.elasticsearch.SearchExtension;
import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurationContext;
import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurer;

@SearchExtension
public class AnalysisConfigurer implements ElasticsearchAnalysisConfigurer {
  @Override
  public void configure(ElasticsearchAnalysisConfigurationContext context) {
    context.analyzer("name").custom()
        .tokenizer("standard")
        .tokenFilters("asciifolding", "lowercase");

    context.analyzer("english").custom()
        .tokenizer("standard")
        .tokenFilters("asciifolding", "lowercase", "porter_stem");

    context.normalizer("sort").custom()
        .tokenFilters("asciifolding", "lowercase");
  }
}
