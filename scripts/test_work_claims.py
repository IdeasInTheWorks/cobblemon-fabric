"""Deterministic lease, expiry, fencing and scope regression checks."""
from datetime import datetime, timedelta, timezone
import unittest
from work_claims import apply_operation, verify, Conflict, ClaimError, INTEGRATION


class WorkClaimsTests(unittest.TestCase):
    def setUp(self):
        self.now = datetime(2026, 10, 24, 20, 0, tzinfo=timezone.utc)
        self.empty = {"schema": 1, "generation": 0, "claims": {}, "events": []}

    def operation(self, work="CORE-01", owner="agent-a", token="lease-a", scopes=None, **kwargs):
        return {"kind": "acquire", "work_id": work, "owner": owner,
            "operation_id": token, "branch": INTEGRATION, "scopes": scopes or ["common/src"],
            "resources": ["integration:" + INTEGRATION], "minutes": 30, **kwargs}

    def test_parent_child_scope_collision(self):
        state, _ = apply_operation(self.empty, self.operation(), self.now)
        with self.assertRaises(Conflict):
            apply_operation(state, self.operation(work="B", owner="b", token="b", scopes=["common/src/main"]), self.now)

    def test_disjoint_paths_and_resources_can_run_concurrently(self):
        a = self.operation(branch="work/a", resources=[])
        state, _ = apply_operation(self.empty, a, self.now)
        b = self.operation(work="B", token="b", owner="b", scopes=["fabric/src"], resources=[], branch="work/b")
        state, _ = apply_operation(state, b, self.now)
        self.assertEqual(2, len(state["claims"]))

    def test_path_prefix_without_separator_does_not_collide(self):
        state, _ = apply_operation(self.empty, self.operation(scopes=["common/src"], resources=[]), self.now)
        state, _ = apply_operation(state, self.operation(work="B", token="b", scopes=["common/src2"], resources=[]), self.now)
        self.assertEqual(2, len(state["claims"]))

    def test_expired_lease_cannot_heartbeat(self):
        state, claim = apply_operation(self.empty, self.operation(), self.now)
        with self.assertRaises(Conflict):
            apply_operation(state, {"kind": "heartbeat", "work_id": "CORE-01", "owner": "agent-a", "lease_id": claim["lease_id"]}, self.now + timedelta(minutes=31))

    def test_expiry_takeover_fences_old_holder(self):
        state, old = apply_operation(self.empty, self.operation(), self.now)
        later = self.now + timedelta(minutes=31)
        state, new = apply_operation(state, self.operation(owner="b", token="b"), later)
        self.assertEqual(2, new["epoch"])
        with self.assertRaises(Conflict):
            verify(state, "agent-a", old["lease_id"], INTEGRATION, ["common/src/a.kt"], later)

    def test_close_requires_checkpoint(self):
        state, claim = apply_operation(self.empty, self.operation(), self.now)
        with self.assertRaises(ClaimError):
            apply_operation(state, {"kind": "complete", "work_id": "CORE-01", "owner": "agent-a", "lease_id": claim["lease_id"]}, self.now)

    def test_closed_claim_is_not_valid_for_push(self):
        state, claim = apply_operation(self.empty, self.operation(), self.now)
        state, _ = apply_operation(state, {"kind": "complete", "work_id": "CORE-01", "owner": "agent-a", "lease_id": claim["lease_id"], "checkpoint": "commit abc; tests passed"}, self.now)
        with self.assertRaises(Conflict):
            verify(state, "agent-a", claim["lease_id"], INTEGRATION, ["common/src/a.kt"], self.now)

    def test_push_outside_scope_is_rejected(self):
        state, claim = apply_operation(self.empty, self.operation(), self.now)
        with self.assertRaises(Conflict):
            verify(state, "agent-a", claim["lease_id"], INTEGRATION, ["fabric/src/a.kt"], self.now)

    def test_wrong_branch_or_owner_is_rejected(self):
        state, claim = apply_operation(self.empty, self.operation(), self.now)
        for owner, branch in [("b", INTEGRATION), ("agent-a", "work/wrong")]:
            with self.assertRaises(Conflict):
                verify(state, owner, claim["lease_id"], branch, [], self.now)

    def test_shared_branch_requires_integration_lease(self):
        state, claim = apply_operation(self.empty, self.operation(resources=[]), self.now)
        with self.assertRaises(Conflict):
            verify(state, "agent-a", claim["lease_id"], INTEGRATION, ["common/src/a.kt"], self.now)

    def test_idempotent_acquire_and_input_state_not_mutated(self):
        op = self.operation()
        state, claim = apply_operation(self.empty, op, self.now)
        same, again = apply_operation(state, op, self.now)
        self.assertEqual(state, same)
        self.assertEqual(claim, again)
        self.assertEqual({}, self.empty["claims"])

    def test_reject_traversal_and_absolute_scopes(self):
        for path in ["../common", "/common", "common/../fabric", "common\\src", "C:/repo"]:
            with self.assertRaises(ClaimError):
                apply_operation(self.empty, self.operation(scopes=[path]), self.now)

    def test_replayed_expired_operation_cannot_reuse_fencing_id(self):
        op = self.operation()
        state, _ = apply_operation(self.empty, op, self.now)
        with self.assertRaises(Conflict):
            apply_operation(state, op, self.now + timedelta(minutes=31))

    def test_public_lease_id_does_not_transfer_ownership(self):
        state, _ = apply_operation(self.empty, self.operation(), self.now)
        with self.assertRaises(Conflict):
            apply_operation(state, self.operation(owner="agent-b"), self.now)


if __name__ == "__main__":
    unittest.main()
