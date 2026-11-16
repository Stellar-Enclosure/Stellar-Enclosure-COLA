"""Regression tests for template-version-specific Plan structure."""

import re
import tempfile
import unittest
from pathlib import Path

from validate_plan import validate

TEMPLATE = (Path(__file__).resolve().parent.parent / "assets" / "plan-template.md").read_text(encoding="utf-8")


class PlanStructureTest(unittest.TestCase):
    def test_current_template_requires_necessity_audit_and_dependency_matrix(self) -> None:
        template = TEMPLATE
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "2026-11-11-12-00-structure-check.md"
            path.write_text(template, encoding="utf-8")
            complete_errors, _ = validate(path, strict=False)
            for subsection, table in (("4.5", "necessity audit table"), ("4.6", "dependency matrix")):
                with self.subTest(subsection=subsection):
                    missing = f"missing required subsection: ### {subsection}"
                    self.assertFalse(any(missing in error or table in error for error in complete_errors))
                    start = template.index(f"\n### {subsection} ")
                    end = template.index("\n### ", start + 1)
                    without_subsection = template[:start] + template[end:]
                    path.write_text(without_subsection, encoding="utf-8")
                    current_errors, _ = validate(path, strict=False)
                    self.assertTrue(any(missing in error for error in current_errors))
                    self.assertTrue(any(table in error for error in current_errors))

                    legacy = without_subsection.replace("| Template Version | `6` |", "| Template Version | `4` |")
                    path.write_text(legacy, encoding="utf-8")
                    legacy_errors, _ = validate(path, strict=False)
                    self.assertFalse(any(missing in error or table in error for error in legacy_errors))

    def check_prose_contract(self, plan: str) -> list[str]:
        # Use the complete template to exercise version dispatch and Step parsing.
        # Leave unrelated metadata, Spec and Manual Check placeholders outside these assertions.
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "2026-11-13-12-00-prose-plan.md"
            path.write_text(plan, encoding="utf-8")
            errors, _ = validate(path, strict=True)
        return [error for error in errors if "Required change" in error or "fenced" in error or "pseudocode" in error]

    def test_simple_change_accepts_prose_without_pseudocode(self) -> None:
        plan = re.sub(
            r"(?m)^- Required change:.*",
            "- Required change: Reject an empty order note and return the existing validation error.",
            TEMPLATE,
        )
        self.assertEqual([], self.check_prose_contract(plan))
        missing = plan.replace("- Required change: Reject", "- Required change:\nReject", 1)
        self.assertTrue(any("File 1 needs a prose Required change" in error
                            for error in self.check_prose_contract(missing)))

    def test_complex_logic_accepts_a_short_markdown_list(self) -> None:
        plan = re.sub(
            r"(?m)^- Required change:.*",
            "- Required change: Recover the result of concurrent order creation.\n\n"
            "- Complex logic (pseudocode):\n"
            "  - If the key exists, replay its committed result.\n"
            "  - Otherwise, persist the order and replay record together.",
            TEMPLATE,
            count=1,
        )
        self.assertEqual([], self.check_prose_contract(plan))

    def test_fenced_snippets_are_rejected_in_steps_and_appendices(self) -> None:
        examples = {
            "Step": TEMPLATE.replace(
                "- Verification contribution:",
                "```java\nreturn OrderStatus.OPEN;\n```\n\n- Verification contribution:",
                1,
            ),
            "appendix": TEMPLATE + "\n## Appendix\n\n~~~sql\nSELECT status FROM orders;\n~~~\n",
        }
        for location, plan in examples.items():
            with self.subTest(location=location):
                self.assertTrue(any("fenced blocks are forbidden" in error
                                    for error in self.check_prose_contract(plan)))

    def test_version_five_still_accepts_fenced_per_file_pseudocode(self) -> None:
        plan = TEMPLATE.replace("| Template Version | `6` |", "| Template Version | `5` |")
        plan = plan.replace("Implementation obligations", "Pseudocode obligations")
        self.assertTrue(any("needs a fenced pseudocode block" in error for error in self.check_prose_contract(plan)))
        plan = re.sub(
            r"(?m)^- Required change:.*",
            "- Implementation pseudocode:\n\n"
            "```text\n"
            "Read the order status from the authorized order repository.\n"
            "Return the existing status for orders owned by the requesting tenant.\n"
            "Return the approved not-found error for missing or cross-tenant orders.\n"
            "```",
            plan,
        )
        self.assertEqual([], self.check_prose_contract(plan))


if __name__ == "__main__":
    unittest.main()
