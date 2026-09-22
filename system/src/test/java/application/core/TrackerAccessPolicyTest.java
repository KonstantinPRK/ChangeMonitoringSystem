package application.core;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackerAccessPolicyTest {
    @Test
    void allowsEveryTrackerWhenBotHasNoRestriction() {
        TrackerAccessPolicy policy = new TrackerAccessPolicy();

        assertTrue(policy.isAllowed("telegram", "github"));
        assertTrue(policy.isAllowed("telegram", "stackoverflow"));
    }

    @Test
    void appliesExplicitRestrictionAndCanRemoveIt() {
        TrackerAccessPolicy policy = new TrackerAccessPolicy();
        policy.allowOnly("telegram", Set.of("github"));

        assertTrue(policy.isAllowed("telegram", "github"));
        assertFalse(policy.isAllowed("telegram", "stackoverflow"));

        policy.allowAll("telegram");
        assertTrue(policy.isAllowed("telegram", "stackoverflow"));
    }
}
