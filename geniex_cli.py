#!/usr/bin/env python3
"""Small, approval-first CLI for the GenieX OpenAI-compatible phone server."""
from __future__ import annotations

import argparse
import json
import os
import subprocess
import sys
import urllib.error
import urllib.request
from pathlib import Path

BASE_URL = os.environ.get("GENIEX_BASE_URL", "http://127.0.0.1:8080/v1").rstrip("/")


def api(path: str, payload: dict | None = None) -> dict:
    data = None if payload is None else json.dumps(payload).encode()
    request = urllib.request.Request(
        f"{BASE_URL}{path}",
        data=data,
        headers={"Content-Type": "application/json"} if data else {},
        method="POST" if data else "GET",
    )
    try:
        with urllib.request.urlopen(request, timeout=120) as response:
            return json.loads(response.read())
    except urllib.error.HTTPError as exc:
        body = exc.read().decode(errors="replace")
        try:
            parsed = json.loads(body)
            message = parsed.get("error", {}).get("message", body)
        except json.JSONDecodeError:
            message = body or str(exc)
        raise SystemExit(f"GenieX request failed: {message}") from exc
    except urllib.error.URLError as exc:
        raise SystemExit(f"Cannot reach GenieX at {BASE_URL}: {exc}") from exc


def ask(prompt: str) -> str:
    models = api("/models").get("data", [])
    model = models[0]["id"] if models else "geniex"
    result = api(
        "/chat/completions",
        {"model": model, "messages": [{"role": "user", "content": prompt}]},
    )
    if "error" in result:
        raise SystemExit(result["error"].get("message", "GenieX request failed"))
    return result["choices"][0]["message"]["content"]


def run(*args: str, cwd: str | None = None) -> str:
    try:
        result = subprocess.run(args, cwd=cwd, text=True, capture_output=True, check=True)
    except subprocess.CalledProcessError as exc:
        print(exc.stdout, end="")
        print(exc.stderr, end="", file=sys.stderr)
        raise SystemExit(exc.returncode) from exc
    return result.stdout


def confirm(message: str) -> bool:
    return input(f"{message} [y/N] ").strip().lower() == "y"


def main() -> None:
    parser = argparse.ArgumentParser(prog="geniex", description=__doc__)
    sub = parser.add_subparsers(dest="command", required=True)

    ask_parser = sub.add_parser("ask", help="Ask the phone model a question")
    ask_parser.add_argument("prompt", nargs="+")

    sub.add_parser("status", help="Show Git status")
    pull = sub.add_parser("pull", help="Pull Git changes after approval")
    pull.add_argument("--cwd", default=".")
    commit = sub.add_parser("commit", help="Stage all changes and create an approved commit")
    commit.add_argument("message", nargs="*", help="Commit message; generated if omitted")
    commit.add_argument("--cwd", default=".")
    push = sub.add_parser("push", help="Push the current branch after approval")
    push.add_argument("--cwd", default=".")
    doc = sub.add_parser("doc", help="Generate a new document without overwriting existing files")
    doc.add_argument("path")
    doc.add_argument("request", nargs="+")

    args = parser.parse_args()
    if args.command == "ask":
        print(ask(" ".join(args.prompt)))
    elif args.command == "status":
        print(run("git", "status", "--short"), end="")
    elif args.command == "pull":
        if confirm(f"Pull Git changes in {Path(args.cwd).resolve()}?"):
            print(run("git", "pull", cwd=args.cwd), end="")
    elif args.command == "push":
        if confirm(f"Push the current branch from {Path(args.cwd).resolve()}?"):
            print(run("git", "push", cwd=args.cwd), end="")
    elif args.command == "commit":
        message = " ".join(args.message).strip()
        if not message:
            diff = run("git", "diff", "--stat", cwd=args.cwd)
            message = ask(f"Write one concise Git commit subject for this diff. Return only the subject.\n{diff}").strip()
        print(f"Commit message: {message}")
        if confirm("Stage all changes and commit?"):
            run("git", "add", "-A", cwd=args.cwd)
            print(run("git", "commit", "-m", message, cwd=args.cwd), end="")
    elif args.command == "doc":
        path = Path(args.path)
        if path.exists():
            raise SystemExit(f"Refusing to overwrite existing file: {path}")
        content = ask("Create the complete document. Return only document content.\n" + " ".join(args.request))
        if confirm(f"Create {path.resolve()}?"):
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content + "\n", encoding="utf-8")
            print(f"Created {path}")


if __name__ == "__main__":
    main()
