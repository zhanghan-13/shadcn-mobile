"""Fail CI if a test command succeeds without executing the expected suite."""

import re
import sys
from pathlib import Path
from xml.etree import ElementTree


def main() -> None:
    if len(sys.argv) != 4:
        raise SystemExit("Usage: ci-check-tests.py junit|instrumentation PATH MINIMUM_TESTS")
    mode, location, minimum = sys.argv[1], Path(sys.argv[2]), int(sys.argv[3])
    if mode == "junit":
        files = sorted(location.glob("TEST-*.xml"))
        if not files:
            raise SystemExit(f"No JUnit reports found in {location}")
        total = failures = errors = skipped = 0
        for file in files:
            root = ElementTree.parse(file).getroot()
            total += int(root.attrib.get("tests", "0"))
            failures += int(root.attrib.get("failures", "0"))
            errors += int(root.attrib.get("errors", "0"))
            skipped += int(root.attrib.get("skipped", "0"))
        if failures or errors or skipped:
            raise SystemExit(f"Unsuccessful tests: {failures} failures, {errors} errors, {skipped} skipped")
    elif mode == "instrumentation":
        result = location.read_text(encoding="utf-8")
        match = re.search(r"^OK \((\d+) tests?\)\s*$", result, re.MULTILINE)
        if match is None or "FAILURES!!!" in result or "INSTRUMENTATION_FAILED" in result:
            raise SystemExit("Instrumentation did not report a successful test run")
        total = int(match.group(1))
    else:
        raise SystemExit(f"Unknown report type: {mode}")
    if total < minimum:
        raise SystemExit(f"Expected at least {minimum} tests, but only {total} executed")
    print(f"Verified {total} successful {mode} tests (minimum: {minimum}).")


if __name__ == "__main__":
    main()
