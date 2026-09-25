package application.connector.github.api;

import application.snapshot.ResourceSnapshot;
import application.snapshot.ResourceSnapshotFactory;

import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GitHubSnapshotReader {
    private final ResourceSnapshotFactory snapshotFactory;


    public GitHubSnapshotReader(ResourceSnapshotFactory snapshotFactory) {
        this.snapshotFactory = snapshotFactory;
    }


    public ResourceSnapshot repository(JsonNode node) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("fullName", node.path("full_name").asString());
        summary.put("pushedAt", node.path("pushed_at").asString());
        summary.put("defaultBranch", node.path("default_branch").asString());
        summary.put("openIssues", node.path("open_issues_count").asInt());
        return snapshotFactory.create(
                "github",
                "repository",
                instant(node, "updated_at"),
                node.path("archived").asBoolean() ? "ARCHIVED" : "ACTIVE",
                summary
        );
    }


    public ResourceSnapshot issue(JsonNode node) {
        Map<String, Object> summary = commonIssueSummary(node);
        return snapshotFactory.create(
                "github",
                "issue",
                instant(node, "updated_at"),
                node.path("state").asString(),
                summary
        );
    }


    public ResourceSnapshot pullRequest(JsonNode node) {
        Map<String, Object> summary = commonIssueSummary(node);
        summary.put("merged", node.path("merged").asBoolean());
        summary.put("draft", node.path("draft").asBoolean());
        summary.put("reviewComments", node.path("review_comments").asInt());
        summary.put("mergeCommit", node.path("merge_commit_sha").asString());
        return snapshotFactory.create(
                "github",
                "pull_request",
                instant(node, "updated_at"),
                node.path("state").asString(),
                summary
        );
    }


    private Map<String, Object> commonIssueSummary(JsonNode node) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("number", node.path("number").asLong());
        summary.put("title", node.path("title").asString());
        summary.put("state", node.path("state").asString());
        summary.put("comments", node.path("comments").asInt());
        return summary;
    }


    private Instant instant(JsonNode node, String field) {
        return Instant.parse(node.path(field).asString());
    }
}
