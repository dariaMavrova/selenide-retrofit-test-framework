#!/usr/bin/env python3
"""Mechanical checks for docs/BEST_PRACTICES.md rules that a script can verify reliably.

Usage (from the repo root):
    python3 .claude/skills/aqa-framework/scripts/lint.py            # whole src/
    python3 .claude/skills/aqa-framework/scripts/lint.py FILE...    # only these files

Prints one line per finding: path:line  SEVERITY  rule  message
Exit code: 1 if any blocker was found, else 0. Findings are candidates: a reviewer confirms each one.
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
BLOCKER, MAJOR, MINOR = "🔴 blocker", "🟠 major", "🟡 minor"

TEST_ANNOTATION = re.compile(r"^\s*@(Test|ParameterizedTest|RepeatedTest)\b")
METHOD_DECL = re.compile(r"^\s*(public |protected |private )?(static )?void \w+\(")


def findings_for(path: Path):
    rel = path.relative_to(ROOT).as_posix()
    in_main = rel.startswith("src/main/")
    in_test = rel.startswith("src/test/")
    text = path.read_text(encoding="utf-8")
    lines = text.splitlines()

    def at(i, severity, rule, message):
        return f"{rel}:{i + 1}  {severity}  {rule}  {message}"

    for i, line in enumerate(lines):
        code = line.split("//", 1)[0]
        if "Thread.sleep" in code:
            yield at(i, BLOCKER, "7.1", "Thread.sleep: use a Selenide condition or wait for the real effect")
        if re.search(r"\bopen\(\s*\"/", code):
            yield at(i, BLOCKER, "5.5", "relative open(\"/...\"): use an absolute URL from Config")
        if re.search(r"\bConfiguration\.\w+\s*=[^=]", code) and not rel.endswith("ui/BaseUiTest.java"):
            yield at(i, BLOCKER, "6.1", "writes global Selenide Configuration outside BaseUiTest")
        if re.search(r"setValue\([^)]*(password|Password)", code) and "sensitive()" not in code:
            yield at(i, BLOCKER, "5.7", "password typed without withText(...).sensitive()")
        if re.search(r"@RetryingTest|rerunFailingTestsCount|@Retry\b", code):
            yield at(i, MAJOR, "7.2", "automatic retry hides flakiness: fix the cause")
        if "@TestMethodOrder" in code:
            yield at(i, MAJOR, "1.2", "ordered tests usually depend on each other")
        if in_main and re.search(r"\bassertThat\(|org\.assertj|org\.junit\.jupiter\.api\.Assertions", code):
            yield at(i, MAJOR, "3.1", "assertion in framework code: return values/elements, assert in the test")
        if in_test and re.search(r"\$\$?\(\s*\"", code):
            yield at(i, MAJOR, "5.1", "CSS selector in a test: move it into a page object")
        if in_test and ".getText()" in code:
            yield at(i, MAJOR, "3.2", "getText() reads once without waiting: use shouldHave(text(...))")
        if in_test and re.search(r"\b\w+\)?\.execute\(\)", code):
            yield at(i, MINOR, "4.5", "direct Call.execute(): use ApiCalls.execute / executeAndExpect")

    if in_test and any(TEST_ANNOTATION.match(l) for l in lines):
        is_ui = "/ui/" in rel
        if not is_ui and '@Tag("api")' not in text:
            yield at(0, MAJOR, "1.6", 'API test class without @Tag("api"): it never runs in CI')
        if is_ui and not re.search(r"\bextends \w*UiTest\b", text):
            yield at(0, MAJOR, "1.6", "UI test class does not extend BaseUiTest (no browser setup, no ui tag)")
        if "@Feature(" not in text:
            yield at(0, MINOR, "8.1", "test class without @Feature")

        # Every test method needs a @DisplayName in its annotation block.
        for i, line in enumerate(lines):
            if not TEST_ANNOTATION.match(line):
                continue
            j, block = i, []
            while j < len(lines) and not METHOD_DECL.match(lines[j]):
                block.append(lines[j])
                j += 1
            k = i - 1
            while k >= 0 and lines[k].strip().startswith("@"):
                block.append(lines[k])
                k -= 1
            if not any("@DisplayName" in b for b in block):
                yield at(i, MINOR, "1.1", "test without @DisplayName")


def build_findings():
    gradle = ROOT / "build.gradle"
    if not gradle.exists():
        return
    for i, line in enumerate(gradle.read_text(encoding="utf-8").splitlines()):
        if re.search(r"(implementation|testImplementation|runtimeOnly|testRuntimeOnly)\s+['\"][\w.-]+:[\w.-]+:\d", line):
            yield f"build.gradle:{i + 1}  {MINOR}  10.1  version in build.gradle: move it to gradle/libs.versions.toml"


def main(argv):
    if argv:
        files = [Path(a).resolve() for a in argv if a.endswith(".java")]
        check_build = any(a.endswith("build.gradle") for a in argv)
    else:
        files = sorted((ROOT / "src").rglob("*.java"))
        check_build = True

    results = [f for path in files if path.exists() for f in findings_for(path)]
    if check_build:
        results += list(build_findings())

    for r in results:
        print(r)
    blockers = sum(BLOCKER in r for r in results)
    print(f"\n{len(results)} finding(s), {blockers} blocker(s)" if results else "No findings.")
    return 1 if blockers else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
