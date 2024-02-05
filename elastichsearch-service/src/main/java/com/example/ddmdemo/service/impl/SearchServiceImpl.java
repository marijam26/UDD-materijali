package com.example.ddmdemo.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.example.ddmdemo.dto.LocationDTO;
import com.example.ddmdemo.dto.ResultDTO;
import com.example.ddmdemo.exceptionhandling.exception.MalformedQueryException;
import com.example.ddmdemo.indexmodel.AgencyContract;
import com.example.ddmdemo.service.interfaces.SearchService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import org.elasticsearch.common.unit.Fuzziness;
import org.jsoup.Jsoup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchOperations elasticsearchTemplate;

    private final UtilService utilService;

    @Override
    public ResultDTO simpleSearch(List<String> keywords, Pageable pageable) {

        if(keywords.get(0).contains(":")){
            List<HighlightField> fields = new ArrayList<>();
            for (String word: keywords) {
                fields.add(new HighlightField(word.split(":")[0]));
            }

            var searchQueryBuilder =
                    new NativeQueryBuilder().withQuery(buildPhrazeSearchQuery(keywords)).withHighlightQuery(new HighlightQuery
                            (new Highlight(fields), AgencyContract.class)).withPageable(pageable);

            return runQuery(searchQueryBuilder.build());
        }


        List<HighlightField> fields = new ArrayList<>();
        fields.add(new HighlightField("fullContent"));
        var searchQueryBuilder =
                new NativeQueryBuilder().withQuery(buildSimpleSearchQuery(keywords)).withHighlightQuery
                                (new HighlightQuery(new Highlight(fields), AgencyContract.class))
                        .withPageable(pageable);

        return runQuery(searchQueryBuilder.build());
    }

    @Override
    public ResultDTO advancedSearch(List<String> expression, Pageable pageable) {
        if (expression.size() != 3) {
            throw new MalformedQueryException("Search query malformed.");
        }

        String operation = expression.get(1);
        expression.remove(1);
        var field1 = expression.get(0).split(":")[0];
        var field2 = expression.get(1).split(":")[0];
        List<HighlightField> fields = new ArrayList<>();
        fields.add(new HighlightField(field1));
        fields.add(new HighlightField(field2));

        var searchQueryBuilder =
            new NativeQueryBuilder().withQuery(buildAdvancedSearchQuery(expression, operation)).withHighlightQuery(new HighlightQuery
                            (new Highlight(fields), AgencyContract.class)).withPageable(pageable);

        return runQuery(searchQueryBuilder.build());
    }

    @Override
    public ResultDTO geoSearch(LocationDTO locationDTO, Pageable pageable) {
        var searchQueryBuilder =
                new NativeQueryBuilder().withQuery(buildGeolocationSearchQuery(locationDTO))
                        .withPageable(pageable);

        return runQuery(searchQueryBuilder.build());
    }

    private Query buildSimpleSearchQuery(List<String> tokens) {
        return BoolQuery.of(q -> q.must(mb -> mb.bool(b -> {
            tokens.forEach(token -> {
                b.should(sb -> sb.match(m -> m.field("fullContent").query(token)));
            });
            return b;
        })))._toQuery();
    }

    private Query buildPhrazeSearchQuery(List<String> tokens) {
        return BoolQuery.of(q -> q.must(mb -> mb.bool(b -> {
            tokens.forEach(token -> {
                b.should(sb -> sb.match(m -> m.field(token.split(":")[0]).query(token.split(":")[1])));
            });
            return b;
        })))._toQuery();
    }

    private Query buildSimpleTokenSearchQuery(String token) {
        var field = token.split(":")[0];
        var value = token.split(":")[1];
        return BoolQuery.of(q -> q.must(mb -> mb.bool(b -> {
                b.should(sb -> sb.match(m -> m.field(field).query(value)));
            return b;
        })))._toQuery();
    }

    private Query buildGeolocationSearchQuery(LocationDTO locationDTO) {
        GeoPoint point = utilService.getLocationFromAddress(locationDTO.getCity());

        return BoolQuery.of(q -> q.must(mb -> mb.bool(b -> {
                b.filter(sb -> sb.geoDistance(m -> m.field("location").distance(locationDTO.getRadius()+"km").location(gl ->
                        gl.latlon(latlon -> latlon.lon(point.getLon()).lat(point.getLat())))));
            return b;
        })))._toQuery();
//        String query = "{\"geo_distance\": {\"distance\": \""+ 15 +"km\", \"location\": [" + 45.244847 +", " + 19.829414 +"]}}";
//        return new StringQuery(query);
    }

    private Query buildAdvancedSearchQuery(List<String> operands, String operation) {
        return BoolQuery.of(q -> q.must(mb -> mb.bool(b -> {
            var field1 = operands.get(0).split(":")[0];
            var value1 = operands.get(0).split(":")[1];
            var field2 = operands.get(1).split(":")[0];
            var value2 = operands.get(1).split(":")[1];

            switch (operation) {
                case "AND":
                    b.must(sb -> sb.match(
                        m -> m.field(field1).fuzziness(Fuzziness.ONE.asString()).query(value1)));
                    b.must(sb -> sb.match(m -> m.field(field2).query(value2)));
                    break;
                case "OR":
                    b.should(sb -> sb.match(
                        m -> m.field(field1).fuzziness(Fuzziness.ONE.asString()).query(value1)));
                    b.should(sb -> sb.match(m -> m.field(field2).query(value2)));
                    break;
                case "NOT":
                    b.must(sb -> sb.match(
                        m -> m.field(field1).fuzziness(Fuzziness.ONE.asString()).query(value1)));
                    b.mustNot(sb -> sb.match(m -> m.field(field2).query(value2)));
                    break;
            }

            return b;
        })))._toQuery();
    }

    private ResultDTO runQuery(NativeQuery searchQuery) {
        var result = new ResultDTO();
        result.highlighters = new ArrayList<>();
        var searchHits = elasticsearchTemplate.search(searchQuery, AgencyContract.class,
            IndexCoordinates.of("contract"));

        for (SearchHit<AgencyContract> hit : searchHits) {
            AgencyContract indexUnit = hit.getContent();
            try {
                String highlight = getHighlight(hit);
                System.out.println(highlight);
                result.highlighters.add(highlight);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        var searchHitsPaged = SearchHitSupport.searchPageFor(searchHits, searchQuery.getPageable());

        result.setPages((Page<AgencyContract>) SearchHitSupport.unwrapSearchHits(searchHitsPaged));
        return result;
    }

    private String getHighlight(SearchHit<AgencyContract> hit) {
        System.out.println(hit.getHighlightFields().values());
        return hit.getHighlightFields().values()
                .stream()
                .reduce((strings1, strings2) -> Stream.concat(strings1.stream(), strings2.stream()).collect(Collectors.toList()))
                .map(strings -> String.join(" ... ", strings))
                .map(this::stripHtml)

                .orElseGet(() -> {
                            String text = hit.getContent().getFullContent();
                            if (text != null) {
                                return text.substring(0, 200).concat(" ... ");
                            } else {
                                return "";
                            }
                        }
                );
    }

    private String stripHtml(String s) {
        return Jsoup.parse(s).text();
    }

}
