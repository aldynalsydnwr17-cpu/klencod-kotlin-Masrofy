#!/usr/bin/env python3
"""Converts the SVG assets in design/ into Android VectorDrawables (app/src/main/res/drawable).

* icons/*.svg and extra-icons/*.svg  -> ic_<name>_<red|white>.xml   (24dp)
* fnf-logo-transparent.svg           -> logo_fnf.xml                (text is outlined with Liberation Sans Bold,
                                                                      the metric-compatible twin of the Arial the SVG asks for)
* components/mod-image-placeholder.svg -> mod_placeholder.xml
Only the Python standard library and fontTools are used.
"""
import re
import sys
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "design" / "assets" / "fnf_mod_manager_assets"
EXTRA = ROOT / "design" / "extra-icons"
OUT = ROOT / "app" / "src" / "main" / "res" / "drawable"
NS = "{http://www.w3.org/2000/svg}"
RED, WHITE = "#F21D3D", "#F5F5F7"

# --------------------------------------------------------------------------- extra icons (same 24px / 1.8 stroke style
# as the icons in the ZIP). They exist because the ZIP has no layers / volume / filter / sort / x / alert / refresh /
# compass / crown / pause icon and the reference design uses them.
EXTRA_ICONS = {
    "layers": '<path d="m12 3 9 5-9 5-9-5z"/><path d="m3 12 9 5 9-5"/><path d="m3 16 9 5 9-5"/>',
    "volume": '<path d="M4 9.5v5h3.5l4.5 4v-13l-4.5 4z"/><path d="M16 9a4 4 0 0 1 0 6"/><path d="M18.5 6.5a8 8 0 0 1 0 11"/>',
    "volume-off": '<path d="M4 9.5v5h3.5l4.5 4v-13l-4.5 4z"/><path d="m16 9.5 5 5"/><path d="m21 9.5-5 5"/>',
    "filter": '<path d="M4 5h16l-6 7.5V19l-4 2v-8.5z"/>',
    "sort": '<path d="M7 4v16"/><path d="m3.5 16.5 3.5 3.5 3.5-3.5"/><path d="M17 20V4"/><path d="m13.5 7.5 3.5-3.5 3.5 3.5"/>',
    "x": '<path d="M6 6l12 12M18 6 6 18"/>',
    "alert": '<path d="M12 4 2.5 20h19z"/><path d="M12 10v4.5M12 17.5h.01"/>',
    "refresh": '<path d="M20 11a8 8 0 1 0-2.3 5.7"/><path d="M20 5v6h-6"/>',
    "compass": '<circle cx="12" cy="12" r="9"/><path d="m15.5 8.5-2 5-5 2 2-5z"/>',
    "crown": '<path d="m3 8 4.5 4L12 5l4.5 7L21 8l-2 11H5z"/>',
}
# filled icons: (body with {C} as the colour placeholder)
EXTRA_FILLED = {
    "pause": '<path d="M7 5h3.2v14H7zM13.8 5H17v14h-3.2z" fill="{C}" stroke="none"/>',
    "star-filled": '<path d="m12 3 2.7 5.5 6.1.9-4.4 4.3 1 6.1-5.4-2.9-5.4 2.9 1-6.1-4.4-4.3 6.1-.9z" fill="{C}"/>',
}


def write_extra_icons():
    EXTRA.mkdir(parents=True, exist_ok=True)
    tpl = ('<svg xmlns="http://www.w3.org/2000/svg" width="128" height="128" viewBox="0 0 24 24" fill="none">\n'
           '  <g stroke="{C}" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">{B}</g>\n</svg>\n')
    for name, body in EXTRA_ICONS.items():
        for tag, col in (("red", RED), ("white", WHITE)):
            (EXTRA / f"{name}-{tag}.svg").write_text(tpl.format(C=col, B=body))
    for name, body in EXTRA_FILLED.items():
        for tag, col in (("red", RED), ("white", WHITE)):
            (EXTRA / f"{name}-{tag}.svg").write_text(tpl.format(C=col, B=body.replace("{C}", col)))


# --------------------------------------------------------------------------- conversion helpers
def fmt(v: float) -> str:
    s = f"{v:.3f}".rstrip("0").rstrip(".")
    return "0" if s in ("", "-0") else s


def circle_path(cx, cy, r):
    return (f"M{fmt(cx - r)},{fmt(cy)}a{fmt(r)},{fmt(r)} 0 1,0 {fmt(2 * r)},0"
            f"a{fmt(r)},{fmt(r)} 0 1,0 {fmt(-2 * r)},0z")


def rect_path(x, y, w, h, rx):
    rx = min(rx, w / 2, h / 2)
    if rx <= 0:
        return f"M{fmt(x)},{fmt(y)}h{fmt(w)}v{fmt(h)}h{fmt(-w)}z"
    return (f"M{fmt(x + rx)},{fmt(y)}h{fmt(w - 2 * rx)}a{fmt(rx)},{fmt(rx)} 0 0 1 {fmt(rx)},{fmt(rx)}"
            f"v{fmt(h - 2 * rx)}a{fmt(rx)},{fmt(rx)} 0 0 1 {fmt(-rx)},{fmt(rx)}h{fmt(-(w - 2 * rx))}"
            f"a{fmt(rx)},{fmt(rx)} 0 0 1 {fmt(-rx)},{fmt(-rx)}v{fmt(-(h - 2 * rx))}"
            f"a{fmt(rx)},{fmt(rx)} 0 0 1 {fmt(rx)},{fmt(-rx)}z")


def color(c: str) -> str:
    c = c.strip()
    if c.startswith("#") and len(c) == 4:
        c = "#" + "".join(ch * 2 for ch in c[1:])
    return c.upper()


INHERIT = ("fill", "stroke", "stroke-width", "stroke-linecap", "stroke-linejoin")


def path_xml(d, a, indent="    "):
    fill, stroke = a.get("fill", "none"), a.get("stroke", "none")
    lines = [f'{indent}<path', f'{indent}    android:pathData="{d}"']
    if fill not in ("none", None):
        lines.append(f'{indent}    android:fillColor="{color(fill)}"')
    if stroke not in ("none", None):
        lines.append(f'{indent}    android:strokeColor="{color(stroke)}"')
        lines.append(f'{indent}    android:strokeWidth="{a.get("stroke-width", "1")}"')
        lines.append(f'{indent}    android:strokeLineCap="{a.get("stroke-linecap", "butt")}"')
        lines.append(f'{indent}    android:strokeLineJoin="{a.get("stroke-linejoin", "miter")}"')
    return "\n".join(lines) + "/>"


def walk(node, inherited, out, fonts=None):
    attrs = dict(inherited)
    for k in INHERIT:
        if k in node.attrib:
            attrs[k] = node.attrib[k]
    tag = node.tag.replace(NS, "")
    if tag == "path":
        out.append(path_xml(node.attrib["d"], attrs))
    elif tag == "circle":
        out.append(path_xml(circle_path(float(node.attrib["cx"]), float(node.attrib["cy"]), float(node.attrib["r"])), attrs))
    elif tag == "rect":
        out.append(path_xml(rect_path(float(node.attrib.get("x", 0)), float(node.attrib.get("y", 0)),
                                      float(node.attrib["width"]), float(node.attrib["height"]),
                                      float(node.attrib.get("rx", 0))), attrs))
    elif tag == "text" and fonts is not None:
        out.extend(text_paths(node, attrs, fonts))
    elif tag in ("defs", "linearGradient", "title", "desc"):
        return
    for child in node:
        if tag != "text":
            walk(child, attrs, out, fonts)


# --------------------------------------------------------------------------- text -> outlines (logo only)
def load_font():
    from fontTools.ttLib import TTFont
    for p in ("/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf",
              "/usr/share/fonts/truetype/liberation2/LiberationSans-Bold.ttf",
              "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"):
        if Path(p).exists():
            print("logo font:", p)
            return TTFont(p)
    raise SystemExit("no bold sans font found for the logo text")


def text_paths(node, attrs, font):
    from fontTools.pens.svgPathPen import SVGPathPen
    from fontTools.pens.transformPen import TransformPen
    size = float(node.attrib["font-size"])
    x_mid, y = float(node.attrib["x"]), float(node.attrib["y"])
    spacing = float(node.attrib.get("letter-spacing", 0))
    base_fill = node.attrib.get("fill", "#000000")
    segs = [(node.text or "", base_fill)]
    for ch in node:
        segs.append((ch.text or "", ch.attrib.get("fill", base_fill)))
        segs.append((ch.tail or "", base_fill))
    cmap, gs, hmtx = font.getBestCmap(), font.getGlyphSet(), font["hmtx"]
    s = size / font["head"].unitsPerEm
    glyphs = []
    for text, col in segs:
        for c in text:
            g = cmap[ord(c)]
            glyphs.append((g, hmtx[g][0] * s + spacing, col))
    x = x_mid - sum(a for _, a, _ in glyphs) / 2  # text-anchor="middle"
    by_color = {}
    for g, adv, col in glyphs:
        pen = SVGPathPen(gs, ntos=lambda v: fmt(v))
        gs[g].draw(TransformPen(pen, (s, 0, 0, -s, x, y)))
        by_color.setdefault(col, []).append(pen.getCommands())
        x += adv
    return [path_xml("".join(cmds), {"fill": col, "stroke": "none"}) for col, cmds in by_color.items()]


def convert(svg: Path, dst: Path, dp_w: float, dp_h: float, fonts=None):
    root = ET.parse(svg).getroot()
    vb = [float(v) for v in root.attrib["viewBox"].split()]
    out = []
    walk(root, {}, out, fonts)
    xml = ('<?xml version="1.0" encoding="utf-8"?>\n'
           '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
           f'    android:width="{fmt(dp_w)}dp"\n    android:height="{fmt(dp_h)}dp"\n'
           f'    android:viewportWidth="{fmt(vb[2])}"\n    android:viewportHeight="{fmt(vb[3])}">\n'
           + "\n".join(out) + "\n</vector>\n")
    dst.write_text(xml)


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    write_extra_icons()
    n = 0
    for folder in (ASSETS / "icons", EXTRA):
        for svg in sorted(folder.glob("*.svg")):
            name = svg.stem.replace("-", "_")  # chevron_left_red
            convert(svg, OUT / f"ic_{name}.xml", 24, 24)
            n += 1
    convert(ASSETS / "fnf-logo-transparent.svg", OUT / "logo_fnf.xml", 240, 100, fonts=load_font())
    convert(ASSETS / "components" / "mod-image-placeholder.svg", OUT / "mod_placeholder.xml", 120, 120)
    print(f"converted {n} icons + logo + placeholder -> {OUT}")


if __name__ == "__main__":
    sys.exit(main())
