#!/usr/bin/env python3
"""刷新 Gitee Wiki 各页的「最后修改时间」与页尾「上一章/下一章」导航。

用法：python3 scripts/update-wiki-meta.py [wiki目录]
默认目录为 SDK 仓库同级的 ../qqbot-sdk-wiki。

- 最后修改时间取文件 mtime，格式 `yyyy-MM-dd HH:mm:ss`，改动页面后重跑即可同步；
- 阅读顺序 = 目录序号（1-起步指南 … 10-更新日志）+ 目录内 `N-` 前缀；
- Home.md 只加时间，_Sidebar.md 不加（Gitee 侧边栏不是正文页）。
"""

import re
import sys
from datetime import datetime
from pathlib import Path

WIKI_BASE = "https://gitee.com/meilinkj/qqbot-sdk/wikis"
DATE_RE = re.compile(r"^> 最后修改时间：.*\n")
FOOTER_MARK = "<!-- 页尾导航：由 scripts/update-wiki-meta.py 自动生成，请勿手改 -->"


def page_order(wiki_dir: Path):
    dirs = sorted(
        (d for d in wiki_dir.iterdir() if d.is_dir() and re.match(r"^\d+-", d.name)),
        key=lambda d: int(d.name.split("-", 1)[0]),
    )
    pages = []
    for d in dirs:
        files = sorted(
            (f for f in d.glob("*.md") if re.match(r"^\d+-", f.name)),
            key=lambda f: int(f.name.split("-", 1)[0]),
        )
        pages.extend((d, f) for f in files)
    return pages


def title_of(path: Path) -> str:
    for line in path.read_text(encoding="utf-8").splitlines():
        if line.startswith("# "):
            return line[2:].strip()
    return path.stem.split("-", 1)[-1]


def url_of(directory: str, file_stem: str) -> str:
    return f"{WIKI_BASE}/{directory}/{file_stem}"


def mtime_date(path: Path) -> str:
    return datetime.fromtimestamp(path.stat().st_mtime).strftime("%Y-%m-%d %H:%M:%S")


def write_body(path: Path, date_str: str, body: str):
    lines = [line for line in body.splitlines(keepends=True) if not DATE_RE.match(line)]
    h1 = next((i for i, line in enumerate(lines) if line.startswith("# ")), None)
    if h1 is None:
        print(f"警告：{path} 没有一级标题，时间行写在文件首")
        head, rest = [], lines
    else:
        head, rest = lines[: h1 + 1], lines[h1 + 1 :]
        while rest and rest[0].strip() == "":
            rest.pop(0)
    new = head + ["\n", f"> 最后修改时间：{date_str}\n", "\n"] + rest
    path.write_text("".join(new), encoding="utf-8")


def write_footer(path: Path, prev_link: str, next_link: str):
    body = path.read_text(encoding="utf-8")
    cut = body.find(FOOTER_MARK)
    if cut != -1:
        body = body[:cut].rstrip() + "\n"
    footer = (
        f"\n{FOOTER_MARK}\n\n---\n\n"
        f"**上一章**：{prev_link} ｜ **下一章**：{next_link}\n"
    )
    path.write_text(body.rstrip() + footer, encoding="utf-8")


def link(directory, file_name, label):
    return f"[{label}]({url_of(directory, file_name.removesuffix('.md'))})"


def main():
    default_wiki = Path(__file__).resolve().parents[2] / "qqbot-sdk-wiki"
    wiki_dir = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else default_wiki
    if not (wiki_dir / "Home.md").is_file():
        sys.exit(f"错误：{wiki_dir} 不是 Wiki 仓库根（缺 Home.md）")

    pages = page_order(wiki_dir)
    if not pages:
        sys.exit(f"错误：{wiki_dir} 下没找到 `N-` 页")

    home = f"[文档中心首页]({WIKI_BASE}/Home)"
    total = len(pages)
    for i, (d, f) in enumerate(pages, start=1):
        stamp = mtime_date(f)
        prev = pages[i - 2] if i > 1 else None
        nxt = pages[i] if i < total - 1 else None
        prev_link = (
            link(prev[0].name, prev[1].name, title_of(prev[1])) if prev else home
        )
        next_link = link(nxt[0].name, nxt[1].name, title_of(nxt[1])) if nxt else home
        write_body(f, stamp, f.read_text(encoding="utf-8"))
        write_footer(f, prev_link, next_link)
        print(f"{i:>2}/{total}  {d.name}/{f.name}")

    home_path = wiki_dir / "Home.md"
    home_body = home_path.read_text(encoding="utf-8")
    cut = home_body.find(FOOTER_MARK)
    if cut != -1:
        home_body = home_body[:cut].rstrip() + "\n"
    write_body(home_path, mtime_date(home_path), home_body)
    print(f"    Home.md（只加时间）\n完成：共 {total} 页 + Home.md")


if __name__ == "__main__":
    main()
