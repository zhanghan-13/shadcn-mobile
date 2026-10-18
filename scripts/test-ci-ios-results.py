"""Exercise report failures that must never turn an iOS CI run green."""

import copy
import importlib.util
import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

CHECKER = Path(__file__).with_name("ci-check-ios-results.py")
spec = importlib.util.spec_from_file_location("ios_results", CHECKER)
checker = importlib.util.module_from_spec(spec)
spec.loader.exec_module(checker)


def report(count=8):
    # Minimal fixture retaining the actual Xcode 26 Test Plan -> UI test bundle
    # -> Test Suite -> Test Case shape. These are checker tests, not iOS results.
    return {
        "devices": [{"platform": "iOS Simulator"}],
        "testNodes": [{
            "nodeType": "Test Plan", "name": "iosApp", "result": "Passed",
            "children": [{
                "nodeType": "UI test bundle", "name": "iosAppUITests", "result": "Passed",
                "children": [{
                    "nodeType": "Test Suite", "name": "FormTests", "result": "Passed",
                    "children": [{
                        "nodeType": "Test Case", "name": f"testFlow{index}()", "result": "Passed",
                        "nodeIdentifierURL": f"test://com.apple.xcode/iosApp/iosAppUITests/FormTests/testFlow{index}",
                    } for index in range(count)],
                }],
            }],
        }],
    }


def cases(value):
    return value["testNodes"][0]["children"][0]["children"][0]["children"]


class ReportChecks(unittest.TestCase):
    def test_accepts_eight_distinct_passes(self):
        self.assertEqual(checker.verify(report(), "iosAppUITests", 8), 8)

    def test_rejects_empty_or_incomplete_run(self):
        for count in (0, 7):
            with self.subTest(count=count), self.assertRaises(ValueError):
                checker.verify(report(count), "iosAppUITests", 8)

    def test_rejects_failure_skip_expected_failure_and_missing_result(self):
        for result in ("Failed", "Skipped", "Expected Failure", "unknown", None):
            value = report()
            cases(value)[0]["result"] = result
            with self.subTest(result=result), self.assertRaises(ValueError):
                checker.verify(value, "iosAppUITests", 8)

    def test_rejects_failure_hidden_below_passing_case(self):
        value = report()
        cases(value)[0]["children"] = [{"nodeType": "Repetition", "name": "First Run", "result": "Failed"}]
        with self.assertRaises(ValueError):
            checker.verify(value, "iosAppUITests", 8)

    def test_rejects_retries_even_when_they_pass(self):
        value = report()
        cases(value)[0]["children"] = [
            {"nodeType": "Repetition", "name": name, "result": "Passed"}
            for name in ("First Run", "Retry 1")
        ]
        with self.assertRaises(ValueError):
            checker.verify(value, "iosAppUITests", 8)

    def test_rejects_duplicate_cases(self):
        value = report(7)
        cases(value).append(copy.deepcopy(cases(value)[0]))
        with self.assertRaises(ValueError):
            checker.verify(value, "iosAppUITests", 8)

    def test_rejects_wrong_target_or_destination(self):
        with self.assertRaises(ValueError):
            checker.verify(report(), "DifferentUITests", 8)
        value = report()
        value["devices"].append({"platform": "iOS Simulator"})
        with self.assertRaises(ValueError):
            checker.verify(value, "iosAppUITests", 8)

    def test_cli_fails_for_missing_corrupt_or_incomplete_reports(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory, "results.json")
            for contents in (None, "{", "{}", json.dumps(report(7))):
                if contents is not None:
                    path.write_text(contents, encoding="utf-8")
                result = subprocess.run(
                    [sys.executable, str(CHECKER), str(path), "iosAppUITests", "8"],
                    capture_output=True, text=True,
                )
                with self.subTest(contents=contents):
                    self.assertNotEqual(result.returncode, 0)
                    self.assertIn("iOS test verification failed", result.stderr)


if __name__ == "__main__":
    unittest.main()
