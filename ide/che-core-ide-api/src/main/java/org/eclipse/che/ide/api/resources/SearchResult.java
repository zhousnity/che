package org.eclipse.che.ide.api.resources;

import com.google.common.base.MoreObjects;

import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.project.shared.Constants;
import org.eclipse.che.api.project.shared.SearchOccurrence;
import org.eclipse.che.api.project.shared.dto.SearchOccurrenceDto;
import org.eclipse.che.api.project.shared.dto.SearchResultDto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vitalii Parfonov
 */

public class SearchResult {

    private String                 name;
    private String                 path;
    private String                 project;


    private String                 contentUrl;
    private List<SearchOccurrence> occurrences;

    public SearchResult(SearchResultDto searchResultDto) {
        name = searchResultDto.getItemReference().getName();
        path = searchResultDto.getItemReference().getPath();
        project = searchResultDto.getItemReference().getProject();
        final List<Link> links = searchResultDto.getItemReference().getLinks();
        if (!links.isEmpty() && links.contains(Constants.LINK_REL_GET_CONTENT)) {
            contentUrl = searchResultDto.getItemReference().getLink(Constants.LINK_REL_GET_CONTENT).getHref();
        }
        final List<SearchOccurrenceDto> dtos = searchResultDto.getSearchOccurrences();
        occurrences = new ArrayList<>(dtos.size());
        for (SearchOccurrence dto : dtos) {
            occurrences.add(new SearchOccurrenceImpl(dto));
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public List<SearchOccurrence> getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(List<SearchOccurrence> occurrences) {
        this.occurrences = occurrences;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }




}
