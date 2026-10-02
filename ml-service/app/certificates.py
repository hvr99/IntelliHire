import os
import re
from abc import ABC, abstractmethod
from dataclasses import dataclass
from io import BytesIO

import pytesseract
from PIL import Image, ImageOps
from pypdf import PdfReader


class TesseractUnavailable(RuntimeError):
    pass


@dataclass
class CertificateResult:
    raw_text: str
    certificate_title: str | None
    issuer: str | None
    detected_skills: list[dict]
    confidence: float
    needs_review: bool


class CertificateExtractor(ABC):
    @abstractmethod
    def extract(self, content: bytes, extension: str, skill_extractor) -> CertificateResult:
        raise NotImplementedError


class TextAndOcrCertificateExtractor(CertificateExtractor):
    def __init__(self, max_pages: int = 5, max_image_dimension: int = 2000):
        self.max_pages = max_pages
        self.max_image_dimension = max_image_dimension
        command = os.getenv("TESSERACT_CMD")
        if command:
            pytesseract.pytesseract.tesseract_cmd = command

    def extract(self, content: bytes, extension: str, skill_extractor) -> CertificateResult:
        if extension == ".pdf":
            raw_text, used_ocr = self._extract_pdf(content)
        else:
            raw_text = self._extract_image(content)
            used_ocr = True
        raw_text = raw_text.strip()
        title, issuer = self._metadata(raw_text)
        detected = skill_extractor.extract(raw_text)
        if used_ocr:
            detected = [{**item, "confidence": round(item["confidence"] * 0.85, 2)} for item in detected]
        confidence = 0.45 + (0.2 if title else 0) + (0.2 if issuer else 0) + (0.15 if detected else 0)
        if used_ocr:
            confidence -= 0.1
        confidence = round(max(0.0, min(1.0, confidence)), 2)
        return CertificateResult(raw_text, title, issuer, detected, confidence, used_ocr or confidence < 0.8)

    def _extract_pdf(self, content: bytes) -> tuple[str, bool]:
        reader = PdfReader(BytesIO(content))
        if len(reader.pages) > self.max_pages:
            raise ValueError(f"Certificate PDF exceeds the {self.max_pages} page limit")
        chunks: list[str] = []
        used_ocr = False
        for page in reader.pages:
            page_text = (page.extract_text() or "").strip()
            if page_text:
                chunks.append(page_text)
                continue
            used_ocr = True
            self._ensure_tesseract()
            images = []
            try:
                images = list(page.images)
            except Exception:
                images = []
            page_text = "\n".join(self._ocr_bytes(image.data) for image in images).strip()
            if page_text:
                chunks.append(page_text)
        return "\n".join(chunks), used_ocr

    def _extract_image(self, content: bytes) -> str:
        self._ensure_tesseract()
        return self._ocr_bytes(content)

    def _ocr_bytes(self, content: bytes) -> str:
        with Image.open(BytesIO(content)) as image:
            image = ImageOps.exif_transpose(image)
            image.thumbnail((self.max_image_dimension, self.max_image_dimension), Image.Resampling.LANCZOS)
            return pytesseract.image_to_string(image.convert("RGB"), config="--psm 6").strip()

    @staticmethod
    def _ensure_tesseract() -> None:
        try:
            pytesseract.get_tesseract_version()
        except (OSError, pytesseract.TesseractNotFoundError) as exc:
            raise TesseractUnavailable("Tesseract is unavailable; install system Tesseract or set TESSERACT_CMD") from exc

    @staticmethod
    def _metadata(text: str) -> tuple[str | None, str | None]:
        lines = [line.strip() for line in text.splitlines() if line.strip()]
        title = next((line.rstrip(":") for line in lines[:8] if re.search(r"certificate|certification|credential", line, re.I)), None)
        issuer = None
        for line in lines[:15]:
            match = re.search(r"(?:issued|awarded|provided|granted)\s+by\s*:?\s*(.+)$", line, re.I)
            if match:
                issuer = match.group(1).strip()
                break
            match = re.search(r"issuer\s*:\s*(.+)$", line, re.I)
            if match:
                issuer = match.group(1).strip()
                break
        return title, issuer
