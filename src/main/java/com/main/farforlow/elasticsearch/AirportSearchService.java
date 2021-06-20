package com.main.farforlow.elasticsearch;

import com.main.farforlow.entity.Airport;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FuzzyQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AirportSearchService {

    private static final String AIRPORT_INDEX = "airportindex";

    private ElasticsearchOperations elasticsearchTemplate = new ElasticsearchClientConfig().elasticsearchTemplate();

    public void createAirportIndexBulk(List<Airport> airports) {

        if (elasticsearchTemplate.indexOps(Airport.class).exists()) {
            elasticsearchTemplate.indexOps(Airport.class).delete();
        }

        List<IndexQuery> queries = airports.stream()
                .map(airport ->
                        new IndexQueryBuilder()
                                .withId(airport.getId())
                                .withObject(airport).build())
                .collect(Collectors.toList());
        ;

        elasticsearchTemplate.bulkIndex(queries, IndexCoordinates.of(AIRPORT_INDEX));
    }

    public List<Airport> exactCitySearch(String cityName) {

        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(new TermQueryBuilder("type", "large_airport"));
        queryBuilder.must(new MatchQueryBuilder("city", cityName));

        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .build();

        SearchHits<Airport> airportHits = elasticsearchTemplate.search(searchQuery, Airport.class, IndexCoordinates.of(AIRPORT_INDEX));

        List<Airport> airports = new ArrayList<>();
        airportHits.forEach(searchHit -> {
            airports.add(searchHit.getContent());
        });

        return airports;
    }

    public List<Airport> exactCityAndCountrySearch(String cityName, String country) {

        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(new TermQueryBuilder("type", "large_airport"));
        queryBuilder.must(new TermQueryBuilder("city", cityName));
        queryBuilder.must(new TermQueryBuilder("countryCode", country));

        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .build();

        SearchHits<Airport> airportHits = elasticsearchTemplate.search(searchQuery, Airport.class, IndexCoordinates.of(AIRPORT_INDEX));

        List<Airport> airports = new ArrayList<>();
        airportHits.forEach(searchHit -> {
            airports.add(searchHit.getContent());
        });

        return airports;
    }

    public List<Airport> fuzzyCitySearch(String cityName) {

        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(new TermQueryBuilder("type", "large_airport"));
        queryBuilder.must(new FuzzyQueryBuilder("city", cityName).fuzziness(Fuzziness.TWO));

        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .build();

        SearchHits<Airport> airportHits = elasticsearchTemplate.search(searchQuery, Airport.class, IndexCoordinates.of(AIRPORT_INDEX));

        List<Airport> airports = new ArrayList<>();
        for (SearchHit<Airport> airportSearchHit : airportHits) {
            airports.add(airportSearchHit.getContent());
        }

        if (airports.isEmpty() && cityName.contains(" ")) {
            airports = fuzzyCitySearch(cityName.split(" ")[1]);
        }

        return airports;
    }
}
