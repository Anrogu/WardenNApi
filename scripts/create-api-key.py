#!/usr/bin/env python3
"""Create an API key for the Warden gateway and store only its SHA-256 hash.

Usage:
    python3 scripts/create-api-key.py --name acme --owner acme-corp --scopes read,write
    python3 scripts/create-api-key.py --name acme --owner acme-corp --expires-in-days 30
    python3 scripts/create-api-key.py --name old --owner acme-corp --disabled

The plaintext key is printed once and never stored. If you lose it, create a new one.
"""

from __future__ import annotations

import argparse
import hashlib
import os
import secrets
import subprocess
import sys
import uuid
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
PREFIX = "wdn_live_"
COMPOSE = ["docker", "compose"]


def load_env() -> dict[str, str]:
    env = dict(os.environ)
    env_file = ROOT / ".env"
    if env_file.exists():
        for line in env_file.read_text().splitlines():
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            env.setdefault(key.strip(), value.strip())
    return env


def sql_literal(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--name", required=True, help="human readable key name")
    parser.add_argument("--owner", required=True, help="consumer this key belongs to")
    parser.add_argument("--scopes", default="", help="comma separated scopes, e.g. read,write")
    parser.add_argument("--expires-in-days", type=int, default=None, help="optional TTL in days")
    parser.add_argument("--disabled", action="store_true", help="insert the key disabled")
    args = parser.parse_args()

    env = load_env()
    db_user = env.get("POSTGRES_USER")
    db_name = env.get("POSTGRES_DB")
    if not db_user or not db_name:
        raise SystemExit("POSTGRES_USER and POSTGRES_DB must be set in .env or the environment")

    random_part = secrets.token_urlsafe(32)
    api_key = f"{PREFIX}{random_part}"
    key_hash = hashlib.sha256(api_key.encode("utf-8")).hexdigest()
    key_prefix = api_key[: len(PREFIX) + 6]
    scopes = [scope.strip() for scope in args.scopes.split(",") if scope.strip()]
    scopes_sql = "ARRAY[" + ", ".join(sql_literal(s) for s in scopes) + "]::text[]"
    expires_sql = (
        f"now() + interval '{args.expires_in_days} days'" if args.expires_in_days else "NULL"
    )

    statement = (
        "INSERT INTO api_keys (id, name, owner, key_prefix, key_hash, scopes, enabled, expires_at)\n"
        f"VALUES ('{uuid.uuid4()}', {sql_literal(args.name)}, {sql_literal(args.owner)}, "
        f"{sql_literal(key_prefix)}, {sql_literal(key_hash)}, {scopes_sql}, "
        f"{'FALSE' if args.disabled else 'TRUE'}, {expires_sql});"
    )

    command = [*COMPOSE, "exec", "-T", "postgres", "psql", "-v", "ON_ERROR_STOP=1",
               "-U", db_user, "-d", db_name, "-q", "-c", statement]
    result = subprocess.run(command, cwd=ROOT, text=True, capture_output=True)
    if result.returncode != 0:
        sys.stderr.write(result.stderr)
        raise SystemExit("failed to insert the API key")

    print(api_key)
    print(
        f"# name={args.name} owner={args.owner} prefix={key_prefix} "
        f"scopes={','.join(scopes) or '-'}",
        file=sys.stderr,
    )


if __name__ == "__main__":
    main()