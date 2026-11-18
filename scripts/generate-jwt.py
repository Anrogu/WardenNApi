#!/usr/bin/env python3
"""Generate a short-lived HS256 JWT for local Warden API demos."""

import base64
import hashlib
import hmac
import json
import os
import sys
import time


def b64url(value: bytes) -> str:
    return base64.urlsafe_b64encode(value).rstrip(b"=").decode("ascii")


def main() -> None:
    secret = os.environ.get("JWT_SECRET")
    if not secret:
        raise SystemExit("JWT_SECRET is required")

    subject = sys.argv[1] if len(sys.argv) > 1 else "demo-user"
    ttl_seconds = int(sys.argv[2]) if len(sys.argv) > 2 else 300

    issuer = os.environ.get("JWT_ISSUER", "warden-api")
    audience = os.environ.get("JWT_AUDIENCE", "warden-gateway")
    now = int(time.time())

    header = {"alg": "HS256", "typ": "JWT"}
    payload = {
        "sub": subject,
        "iss": issuer,
        "aud": audience,
        "iat": now,
        "exp": now + ttl_seconds,
    }

    encoded_header = b64url(json.dumps(header, separators=(",", ":")).encode())
    encoded_payload = b64url(json.dumps(payload, separators=(",", ":")).encode())
    signing_input = f"{encoded_header}.{encoded_payload}"
    signature = hmac.new(
        secret.encode("utf-8"), signing_input.encode("ascii"), hashlib.sha256
    ).digest()

    print(f"{signing_input}.{b64url(signature)}")


if __name__ == "__main__":
    main()
