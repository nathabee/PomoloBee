# ️ SVG Integration for PomoloBee

<details>
<summary>Table of Contents</summary>
 
<!-- TOC -->
- [️ SVG Integration for PomoloBee](#svg-integration-for-pomolobee)
  - [SVG Structure](#svg-structure)
  - [Preparation](#preparation)
    - [Reserve IDs via Backend *planned*](#reserve-ids-via-backend-planned)
    - [Local-only Use](#local-only-use)
    - [step 1 define files in the config/locations.json](#step-1-define-files-in-the-configlocationsjson)
    - [step 2 copy svg in android](#step-2-copy-svg-in-android)
    - [step 3 . optional copy background picture](#step-3-optional-copy-background-picture)
  - [Creating SVGs with Inkscape](#creating-svgs-with-inkscape)
  - [️ Manual Optimization Optional](#manual-optimization-optional)
    - [Reduce Coordinate Precision](#reduce-coordinate-precision)
    - [Simplify Paths with Inkscape](#simplify-paths-with-inkscape)
  - [Auto-Creation Planned Feature](#auto-creation-planned-feature)
<!-- TOC END -->

</details>

---

## SVG Structure

In this explanation, we'll walk through how to create a field with `shortName: F1` and two rows (`row_id: 101` and `102`).

A typical SVG used in PomoloBee follows this structure:

```xml
<svg xmlns="http://www.w3.org/2000/svg"
     xmlns:xlink="http://www.w3.org/1999/xlink"
     width="1599" height="978" viewBox="0 0 1599 978">

  <!-- Group layer (required for Inkscape compatibility) -->
  <g id="g61" inkscape:label="Image" inkscape:groupmode="layer">

    <!-- Optional: Contour of the field -->
    <path id="field_contour"
          d="M 100,100 L 500,100 L 500,500 L 100,500 Z"
          style="fill:none;stroke:#000000;stroke-width:3px" />

    <!-- Clickable row paths -->
    <path id="row_101"
          d="M 150,150 L 450,150"
          style="fill:none;stroke:#000000;stroke-width:3px" />
    
    <path id="row_102"
          d="M 150,200 L 450,200"
          style="fill:none;stroke:#000000;stroke-width:3px" />

    <!-- Row labels using textPath (attached to the path) -->
    <text font-size="14" fill="red">
      <textPath href="#row_101" startOffset="50%" text-anchor="middle">
        row_101
      </textPath>
    </text>

    <text font-size="14" fill="red">
      <textPath href="#row_102" startOffset="50%" text-anchor="middle">
        row_102
      </textPath>
    </text>

    <!-- Optional: Free-positioned label -->
    <text x="160" y="140" font-size="12" fill="blue">Row Label</text>

  </g>
</svg>
```

These elements define the clickable areas (`<path>`), their identifiers (`id="row_..."`), and labels (`<textPath>` or `<text>`).

---

## Preparation

### Reserve IDs via Backend *planned*

1. Fields and their rows are created **first** via the backend (`POST /createLocation`).
2. Use the `GET /getLocations` endpoint to retrieve assigned IDs:
   - `field.shortName` (e.g. `F1`)
   - `rowId` values (e.g. `101`, `102`)
3. These IDs are used to name your SVG paths and labels.

You will also need to:
- Create the SVG file.
- Optionally add a background image to the SVG if available.

---

### Local-only Use

### step 1 define files in the config/locations.json

If you’re working locally without backend integration:

- You can define fields and rows manually in `config/locations.json`.
- Example:

```json
{
  "status": "success",
  "data": {
    "locations": [
      {
        "field": {
          "field_id": 10,
          "short_name": "F1",
          "name": "My Field",
          "description": "Near the Farm",
          "orientation": "NW",
          "svg_map_url": "/media/fields/svg/F1_map.svg" 
        },
        "rows": [
          {
            "row_id": 101,
            "short_name": "F1-R1",
            "name": "Row 1 Swing",
            "nb_plant": 38,
            "fruit_id": 1,
            "fruit_type": "Cultivar Swing on CG1"
          },
          {
            "row_id": 102,
            "short_name": "F1-R2",
            "name": "Row 2 Swing",
            "nb_plant": 40,
            "fruit_id": 1,
            "fruit_type": "Cultivar Swing on CG1"
          }
          ]
    }
    ]
    }
}
```

Make sure:
- Each field has a unique `shortName`.
- Each row has a unique `row_id` used in the SVG as `id="row_<row_id>"`.


### step 2 copy svg in android

copy in the storage that you have defined for the App in fields/svg/<SHORTNAME>_map.svg


### step 3 . optional copy background picture

if you need a background picture put it in fields/background/<SHORTNAME>.jpg
in this case  a reference in the locations.json
      
- Example:

```json
{
"field": {
          "field_id": 10,
          "short_name": "F1",
          "name": "My Field",
          "description": "Near the Farm",
          "orientation": "NW",
          "svg_map_url": "/media/fields/svg/F1_map.svg"  ,
          "background_image_url": "/media/fields/background/F1.jpeg"
        },  
```

---

## Creating SVGs with Inkscape

1. Open Inkscape and draw your map (rows, boundaries, etc.).
2. Use `Path → Object to Path` for drawn lines.
3. Assign each row path an ID like `row_101` in the "Object Properties" panel.
4. Add a label using either:
   - A `<textPath>` element linked to the row path, or
   - A fixed-position `<text>` element.
5. Export as **Plain SVG** (`File → Save As → Plain SVG`).

---

## ️ Manual Optimization Optional

### Reduce Coordinate Precision

To reduce file size and simplify geometry, you can lower the precision of SVG points:

```bash
npx svgo F1_map.svg --precision=0 -o F1_map_optimized.svg
```

---

### Simplify Paths with Inkscape

This helps remove unnecessary path nodes.

**Command line (may not work in all versions):**
```bash
inkscape F1_map.svg --batch-process --actions="select-all;path-simplify;export-plain-svg"
```

If the CLI doesn't work:

- Open the file in Inkscape GUI.
- `Ctrl+A` to select all paths.
- Use `Path → Simplify` (`Ctrl+L`).
- Save as **Plain SVG**.

---

## Auto-Creation Planned Feature

We're working on a script to auto-generate an SVG template from backend data:

```bash
python scripts/makesvg.py F1 

config of the field can be found in F1.json
```

This will generate:
- A basic SVG layout for field `F1`
- Placeholder paths and labels for all rows
 