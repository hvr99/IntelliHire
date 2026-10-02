import json
import re
from abc import ABC, abstractmethod
from pathlib import Path
from dataclasses import dataclass


@dataclass(frozen=True)
class NormalizedSkill:
    skill: str
    matched: bool


class SkillNormalizer:
    """Catalog-backed, deterministic normalization shared by API and extraction."""

    def __init__(self, catalog_path: Path | None = None):
        path = catalog_path or Path(__file__).resolve().parents[2] / "shared" / "skills" / "skills.json"
        catalog = json.loads(path.read_text(encoding="utf-8"))
        self.lookup = {self._clean(term): item["name"] for item in catalog for term in [item["name"], *item["aliases"]]}

    @staticmethod
    def _clean(value: str) -> str:
        value = value.lower()
        value = re.sub(r"(?<=[a-z0-9])[./]+(?=[a-z0-9])", "", value)
        value = re.sub(r"[^a-zA-Z0-9+#\s]", " ", value)
        return re.sub(r"\s+", " ", value).strip()

    def normalize_one(self, value: str) -> NormalizedSkill:
        cleaned = self._clean(value)
        canonical = self.lookup.get(cleaned)
        if canonical:
            return NormalizedSkill(canonical, True)
        versionless = re.sub(r"\s+v?\d+(?:\.\d+)*$", "", cleaned).strip()
        canonical = self.lookup.get(versionless)
        return NormalizedSkill(canonical or cleaned, canonical is not None)

    def normalize(self, skills: list[str]) -> list[NormalizedSkill]:
        results: list[NormalizedSkill] = []
        seen: set[str] = set()
        for skill in skills:
            normalized = self.normalize_one(skill)
            if normalized.skill and normalized.skill not in seen:
                seen.add(normalized.skill)
                results.append(normalized)
        return results


class SkillExtractor(ABC):
    @abstractmethod
    def extract(self, text: str) -> list[dict]:
        raise NotImplementedError


class DictionarySkillExtractor(SkillExtractor):
    def __init__(self, catalog_path: Path | None = None):
        path = catalog_path or Path(__file__).resolve().parents[2] / "shared" / "skills" / "skills.json"
        self.catalog = json.loads(path.read_text(encoding="utf-8"))
        self.normalizer = SkillNormalizer(path)
        self.lookup = {term.lower(): item["name"] for item in self.catalog for term in [item["name"], *item["aliases"]]}

    def extract(self, text: str) -> list[dict]:
        normalized = text.lower()
        detected: dict[str, dict] = {}
        for term, canonical in sorted(self.lookup.items(), key=lambda pair: len(pair[0]), reverse=True):
            pattern = rf"(?<![a-z0-9+#.]){re.escape(term)}(?![a-z0-9+#.])"
            if re.search(pattern, normalized) and canonical not in detected:
                detected[canonical] = {"skill": canonical, "confidence": 1.0 if term == canonical else 0.95}
        return sorted(detected.values(), key=lambda item: item["skill"])
