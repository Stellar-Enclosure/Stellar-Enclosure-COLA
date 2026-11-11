"""Regression tests for template-version-specific Plan structure."""

import tempfile
import unittest
from pathlib import Path

from validate_plan import validate


class PlanStructureTest(unittest.TestCase):
    def test_v5_requires_necessity_audit_and_dependency_matrix(self) -> None:
        template_path = Path(__file__).resolve().parent.parent / "assets" / "plan-template.md"
        template = template_path.read_text(encoding="utf-8")
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

                    legacy = without_subsection.replace("| Template Version | `5` |", "| Template Version | `4` |")
                    path.write_text(legacy, encoding="utf-8")
                    legacy_errors, _ = validate(path, strict=False)
                    self.assertFalse(any(missing in error or table in error for error in legacy_errors))


if __name__ == "__main__":
    unittest.main()
