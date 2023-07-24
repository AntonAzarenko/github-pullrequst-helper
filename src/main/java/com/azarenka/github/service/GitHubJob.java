package com.azarenka.github.service;

import com.azarenka.github.domain.RepositoryDescription;
import com.azarenka.github.gui.Gui;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GitHubJob {

    private final GitHub gitHub;
    private final Gui gui = new Gui();
    private final Set<Long> allPrIds = new HashSet<>();
    private List<String> repositoriesNames = new ArrayList<>();
    private String token;
    private List<RepositoryDescription> repos;

    public GitHubJob() {
        try {
            initProperties();
            gitHub = new GitHubBuilder()
                    .withAppInstallationToken(token)
                    .build();
            init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initProperties() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("application.properties"));
            String[] names = ((String) properties.get("app.github.repositories")).split(",");
            IntStream.range(0, names.length).forEach(i -> repositoriesNames.add(names[i]));
            token = properties.getProperty("app.github.token");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void init() throws IOException {
        GHMyself myself = gitHub.getMyself();
        String login = myself.getLogin();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    boolean notifyForNewPrs = !allPrIds.isEmpty();
                    HashSet<GHPullRequest> newPrs = new HashSet<>();
                    repos = myself.getAllRepositories()
                            .values()
                            .stream()
                            .map(ghRepository -> {
                                try {
                                    if (repositoriesNames.contains(ghRepository.getName())) {
                                        List<GHPullRequest> pullRequests = ghRepository.queryPullRequests()
                                                .list()
                                                .toList();
                                        Set<Long> prIds = pullRequests.stream()
                                                .map(GHPullRequest::getId)
                                                .collect(Collectors.toSet());
                                        prIds.removeAll(allPrIds);
                                        allPrIds.addAll(prIds);
                                        pullRequests.forEach(pr -> {
                                            if (prIds.contains(pr.getId())) {
                                                newPrs.add(pr);
                                            }
                                        });
                                        return new RepositoryDescription(ghRepository.getFullName(), ghRepository,
                                                pullRequests);
                                    }
                                    return null;
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .collect(Collectors.toList());
                    gui.setMenu(login, repos);

                    if (notifyForNewPrs) {
                        newPrs.forEach(pr -> {
                            gui.showNotification(
                                    "New PR in " + pr.getRepository().getFullName(),
                                    pr.getTitle()
                            );
                        });
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 1000, 1000);
    }
}
