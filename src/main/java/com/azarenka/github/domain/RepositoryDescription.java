package com.azarenka.github.domain;

import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;

import java.util.List;

public class RepositoryDescription {
    private final String name;
    private final GHRepository repository;
    private final List<GHPullRequest> prs;

    public RepositoryDescription(String name, GHRepository repository, List<GHPullRequest> prs) {
        this.name = name;
        this.repository = repository;
        this.prs = prs;
    }

    public String getName() {
        return name;
    }

    public GHRepository getRepository() {
        return repository;
    }

    public List<GHPullRequest> getPrs() {
        return prs;
    }
}
