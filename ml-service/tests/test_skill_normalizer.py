from app.skills import SkillNormalizer
from app.main import normalize_skills


def test_shared_normalization_fixtures():
    result = SkillNormalizer().normalize(["JS", " Python 3.12 ", "react.js", "JS", "Unknown Tool", "C++"])
    assert [(item.skill, item.matched) for item in result] == [
        ("javascript", True),
        ("python", True),
        ("react", True),
        ("unknown tool", False),
        ("cpp", True),
    ]


def test_normalize_api_returns_match_flags():
    assert normalize_skills(["py", "PY", "custom tool"]) == {
        "skills": [
            {"skill": "python", "matched": True},
            {"skill": "custom tool", "matched": False},
        ]
    }
