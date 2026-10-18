"""Require independently executed, passing XCUITests in xcresulttool JSON.

Input: `xcrun xcresulttool get test-results tests --path RESULT.xcresult`.
The Test Case/UI test bundle fields and Repetition nodes were checked against
Fastlane's actual Xcode 26 export (not the incompatible legacy object format):
https://github.com/fastlane/fastlane/blob/master/trainer/spec/fixtures/Xcode26-Mixed-XCTest-SwiftTesting.json
CI retains the selected Xcode's command help/output schema alongside its report.
Unknown/missing results fail closed; summary totals alone cannot prove that the
expected UI target or the required number of distinct methods actually ran.
"""

import json
import sys
from pathlib import Path


def verify(report: object, target: str, minimum: int) -> int:
    if not isinstance(report, dict):
        raise ValueError("Test report must be a JSON object")
    devices = report.get("devices")
    if not isinstance(devices, list) or len(devices) != 1:
        raise ValueError("Expected exactly one simulator in the test report")
    if not isinstance(devices[0], dict) or devices[0].get("platform") != "iOS Simulator":
        raise ValueError("Tests did not report an iOS Simulator destination")
    roots = report.get("testNodes")
    if not isinstance(roots, list) or not roots:
        raise ValueError("Missing or empty testNodes; no tests were verified")
    if minimum < 1:
        raise ValueError("Minimum test count must be positive")

    identifiers = set()
    bundles = 0

    def walk(nodes: list, in_target: bool = False) -> None:
        nonlocal bundles
        for node in nodes:
            if not isinstance(node, dict) or not isinstance(node.get("nodeType"), str):
                raise ValueError("Malformed test node")
            kind = node["nodeType"]
            children = node.get("children", [])
            if not isinstance(children, list):
                raise ValueError("Malformed test node children")
            if "result" in node and node["result"] != "Passed":
                raise ValueError(f"Unsuccessful {kind}: {node.get('name')}: {node['result']}")
            if kind == "Failure Message":
                raise ValueError(f"Test failure: {node.get('name')}")
            if kind in ("UI test bundle", "Unit test bundle"):
                if kind != "UI test bundle" or node.get("name") != target:
                    raise ValueError(f"Unexpected test bundle: {node.get('name')}")
                if node.get("result") != "Passed":
                    raise ValueError(f"Missing passing result for {target}")
                bundles += 1
                walk(children, True)
                continue
            if kind == "Test Case":
                if not in_target or node.get("result") != "Passed":
                    raise ValueError("Test case is outside the expected UI bundle or did not pass")
                identifier = node.get("nodeIdentifierURL") or node.get("nodeIdentifier")
                if not isinstance(identifier, str) or not identifier:
                    raise ValueError("Test case has no identifier")
                if identifier in identifiers:
                    raise ValueError(f"Repeated test case: {identifier}")
                identifiers.add(identifier)
                # A single 'First Run' is acceptable; retries/repeated executions
                # cannot inflate the number of independently executed tests.
                repetitions = count_repetitions(children)
                if repetitions > 1:
                    raise ValueError(f"Test case executed more than once: {identifier}")
            walk(children, in_target)

    def count_repetitions(nodes: list) -> int:
        total = 0
        for node in nodes:
            if not isinstance(node, dict):
                raise ValueError("Malformed test case child")
            children = node.get("children", [])
            if not isinstance(children, list):
                raise ValueError("Malformed test case children")
            total += int(node.get("nodeType") == "Repetition") + count_repetitions(children)
        return total

    walk(roots)
    if bundles != 1:
        raise ValueError(f"Expected one {target} UI bundle, found {bundles}")
    if len(identifiers) < minimum:
        raise ValueError(f"Expected at least {minimum} distinct UI tests, found {len(identifiers)}")
    return len(identifiers)


def main() -> None:
    if len(sys.argv) != 4:
        raise SystemExit("Usage: ci-check-ios-results.py TEST_RESULTS_JSON TARGET MINIMUM_TESTS")
    try:
        report = json.loads(Path(sys.argv[1]).read_text(encoding="utf-8"))
        count = verify(report, sys.argv[2], int(sys.argv[3]))
    except (OSError, ValueError) as error:
        raise SystemExit(f"iOS test verification failed: {error}") from error
    print(f"Verified {count} distinct passing {sys.argv[2]} tests; no failed/skipped tests or retries.")


if __name__ == "__main__":
    main()
