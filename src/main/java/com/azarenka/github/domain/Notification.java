package com.azarenka.github.domain;

import org.kohsuke.github.GHPullRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Notification {

    private Map<Long, GHPullRequest> notifications = new HashMap<>();

}
