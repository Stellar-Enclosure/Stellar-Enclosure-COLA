#!/usr/bin/env python3
"""Check the two native HTTP archetypes' permission build inputs without starting services."""

import json
from pathlib import Path
import runpy
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
NS = {"m": "http://maven.apache.org/POM/4.0.0"}


def profiles(path):
    root = ET.parse(path).getroot()
    return {profile.findtext("m:id", namespaces=NS): profile for profile in root.findall("m:profiles/m:profile", NS)}


class PermissionProducerTemplatesTest(unittest.TestCase):
    def test_inventory_targets_and_profiles(self):
        for family in ("light", "web"):
            with self.subTest(family=family):
                name = "stellar-enclosure-source-" + family
                project = ROOT / "source-projects" / name
                config = json.loads((project / "permission-producers.json").read_text())
                self.assertEqual("PINNED_COMMIT", json.loads((project / "permission-toolchain.lock.json").read_text())["mode"])
                self.assertEqual({}, json.loads((project / "permission-targets.json").read_text())["targets"])
                producers = runpy.run_path(str(project / "scripts/permission-ci.py"), run_name="permission_test")["producer_config"](project)
                self.assertEqual(config["producers"], producers)
                producer = producers[0]
                self.assertEqual(name + "-java", producer["producerId"])
                self.assertEqual(name + "-exec.jar", producer["artifactFile"])
                self.assertTrue((project / producer["sourceModule"]).resolve().is_relative_to(project.resolve()))
                self.assertIn("permission-producer", profiles(project / "pom.xml") if family == "light"
                              else profiles(project / (name + "-adapter") / "pom.xml"))
                if family == "web":
                    self.assertIn("permission-producer", profiles(project / (name + "-starter") / "pom.xml"))
                for workflow in ("ci.yml", "permission-delivery.yml"):
                    self.assertTrue((project / ".github/workflows" / workflow).is_file())


if __name__ == "__main__":
    unittest.main()
