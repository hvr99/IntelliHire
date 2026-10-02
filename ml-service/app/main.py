import os
from pathlib import Path

from fastapi import FastAPI, File, HTTPException, UploadFile

from .certificates import CertificateResult, TesseractUnavailable, TextAndOcrCertificateExtractor
from .embeddings import EmbeddingProvider, FakeEmbeddingProvider, FastEmbedEmbeddingProvider
from .parsers import PARSERS
from .skills import DictionarySkillExtractor, SkillNormalizer
from .similarity import cosine_similarity

MAX_FILE_SIZE = int(os.getenv("MAX_RESUME_SIZE_BYTES", str(5 * 1024 * 1024)))
MAX_CERTIFICATE_SIZE = int(os.getenv("MAX_CERTIFICATE_SIZE_BYTES", str(10 * 1024 * 1024)))
MAX_CERTIFICATE_PAGES = int(os.getenv("MAX_CERTIFICATE_PAGES", "5"))
MAX_EMBED_TEXTS = int(os.getenv("EMBEDDING_MAX_BATCH_SIZE", "32"))
MAX_EMBED_TEXT_LENGTH = int(os.getenv("EMBEDDING_MAX_TEXT_LENGTH", "4000"))
MAX_SIMILARITY_JOBS = int(os.getenv("SIMILARITY_MAX_JOBS", str(MAX_EMBED_TEXTS - 1)))
ALLOWED_TYPES = {".pdf": {"application/pdf"}, ".docx": {"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/octet-stream"}}
CERTIFICATE_TYPES = {".pdf": {"application/pdf"}, ".png": {"image/png"}, ".jpg": {"image/jpeg"}, ".jpeg": {"image/jpeg"}}
extractor = DictionarySkillExtractor()
normalizer = SkillNormalizer()
certificate_extractor = TextAndOcrCertificateExtractor(max_pages=MAX_CERTIFICATE_PAGES)
app = FastAPI(title="Resume Extraction Service", version="1.0.0")


def _create_embedding_provider() -> EmbeddingProvider:
    provider = os.getenv("EMBEDDING_PROVIDER", "fake").lower()
    if provider == "fastembed":
        return FastEmbedEmbeddingProvider(os.getenv("EMBEDDING_MODEL_NAME", "BAAI/bge-small-en-v1.5"), os.getenv("EMBEDDING_CACHE_DIR", ".cache/fastembed"))
    return FakeEmbeddingProvider()


embedding_provider: EmbeddingProvider = _create_embedding_provider()


def detect_sections(text: str) -> list[str]:
    known = {"summary", "profile", "skills", "experience", "work experience", "education", "certifications", "projects"}
    return [line.strip().title() for line in text.splitlines() if line.strip().lower().rstrip(":") in known]


@app.get("/health")
def health() -> dict[str, str | bool]:
    return {"status": "UP", "modelLoaded": embedding_provider.model_loaded}


@app.post("/embed")
def embed_texts(texts: list[str]) -> dict:
    if not texts:
        raise HTTPException(status_code=400, detail="At least one text is required")
    if len(texts) > MAX_EMBED_TEXTS:
        raise HTTPException(status_code=413, detail=f"Embedding batch exceeds the {MAX_EMBED_TEXTS} text limit")
    if any(not isinstance(text, str) or not text.strip() for text in texts):
        raise HTTPException(status_code=400, detail="Texts must be non-empty strings")
    if any(len(text) > MAX_EMBED_TEXT_LENGTH for text in texts):
        raise HTTPException(status_code=413, detail=f"Each text must be at most {MAX_EMBED_TEXT_LENGTH} characters")
    try:
        embeddings = embedding_provider.embed(texts)
    except Exception as exc:
        raise HTTPException(status_code=503, detail="Embedding provider is unavailable") from exc
    return {"embeddings": embeddings, "count": len(embeddings), "dimension": len(embeddings[0]) if embeddings else 0}


@app.post("/similarity")
def similarity(request: dict) -> dict:
    candidate_text = request.get("candidateText") if isinstance(request, dict) else None
    jobs = request.get("jobs") if isinstance(request, dict) else None
    if not isinstance(candidate_text, str) or not candidate_text.strip():
        raise HTTPException(status_code=400, detail="candidateText must be a non-empty string")
    if not isinstance(jobs, list) or not jobs:
        raise HTTPException(status_code=400, detail="jobs must contain at least one job")
    if len(jobs) > MAX_SIMILARITY_JOBS:
        raise HTTPException(status_code=413, detail=f"Similarity batch exceeds the {MAX_SIMILARITY_JOBS} job limit")
    if len(candidate_text) > MAX_EMBED_TEXT_LENGTH:
        raise HTTPException(status_code=413, detail=f"candidateText must be at most {MAX_EMBED_TEXT_LENGTH} characters")
    job_ids = []
    job_texts = []
    for job in jobs:
        if not isinstance(job, dict) or "id" not in job or not isinstance(job.get("text"), str) or not job["text"].strip():
            raise HTTPException(status_code=400, detail="Each job must contain an id and non-empty text")
        if len(job["text"]) > MAX_EMBED_TEXT_LENGTH:
            raise HTTPException(status_code=413, detail=f"Each job text must be at most {MAX_EMBED_TEXT_LENGTH} characters")
        job_ids.append(job["id"])
        job_texts.append(job["text"])
    try:
        vectors = embedding_provider.embed([candidate_text, *job_texts])
    except Exception as exc:
        raise HTTPException(status_code=503, detail="Embedding provider is unavailable") from exc
    candidate_vector = vectors[0]
    matches = [
        {"jobId": job_id, "similarity": round(cosine_similarity(candidate_vector, vector), 6)}
        for job_id, vector in zip(job_ids, vectors[1:])
    ]
    return {"matches": matches}


@app.post("/normalize")
def normalize_skills(skills: list[str]) -> dict:
    return {"skills": [{"skill": item.skill, "matched": item.matched} for item in normalizer.normalize(skills)]}


@app.post("/extract/resume")
async def extract_resume(file: UploadFile = File(...)) -> dict:
    suffix = Path(file.filename or "").suffix.lower()
    if suffix not in PARSERS or file.content_type not in ALLOWED_TYPES[suffix]:
        raise HTTPException(status_code=415, detail="Only PDF and DOCX resume files are supported")
    content = await file.read(MAX_FILE_SIZE + 1)
    if not content:
        raise HTTPException(status_code=400, detail="Resume file is empty")
    if len(content) > MAX_FILE_SIZE:
        raise HTTPException(status_code=413, detail=f"Resume exceeds the {MAX_FILE_SIZE} byte limit")
    try:
        text = PARSERS[suffix].parse(content)
    except Exception as exc:
        raise HTTPException(status_code=422, detail="Resume could not be parsed") from exc
    if not text:
        raise HTTPException(status_code=400, detail="Resume contains no extractable text")
    return {"textLength": len(text), "sections": detect_sections(text), "matchedSkills": extractor.extract(text)}


@app.post("/extract/certificate")
async def extract_certificate(file: UploadFile = File(...)) -> dict:
    from pathlib import Path

    suffix = Path(file.filename or "").suffix.lower()
    if suffix not in CERTIFICATE_TYPES or file.content_type not in CERTIFICATE_TYPES[suffix]:
        raise HTTPException(status_code=415, detail="Only PDF, PNG, JPG, and JPEG certificate files are supported")
    content = await file.read(MAX_CERTIFICATE_SIZE + 1)
    if not content:
        raise HTTPException(status_code=400, detail="Certificate file is empty")
    if len(content) > MAX_CERTIFICATE_SIZE:
        raise HTTPException(status_code=413, detail=f"Certificate exceeds the {MAX_CERTIFICATE_SIZE} byte limit")
    try:
        result: CertificateResult = certificate_extractor.extract(content, suffix, extractor)
    except TesseractUnavailable as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
    except ValueError as exc:
        raise HTTPException(status_code=413 if "page limit" in str(exc) else 422, detail=str(exc)) from exc
    except Exception as exc:
        raise HTTPException(status_code=422, detail="Certificate could not be parsed") from exc
    if not result.raw_text:
        raise HTTPException(status_code=422, detail="Certificate contains no extractable text")
    return {"rawText": result.raw_text, "certificateTitle": result.certificate_title, "issuer": result.issuer, "detectedSkills": result.detected_skills, "confidence": result.confidence, "needsReview": result.needs_review}
