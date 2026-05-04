"""Print 5 image-gen prompt batches (10 buildings each, 2 images each)."""
from pymongo import MongoClient

URI = "mongodb://tuandang2411:tuandang2411%40@localhost:27017/estateadvance?authSource=admin"


def main() -> None:
    col = MongoClient(URI)["estateadvance"]["buildings"]
    rows = []
    for d in col.find(
        {},
        {
            "_id": 1,
            "id": 1,
            "name": 1,
            "typeCode": 1,
            "district": 1,
            "ward": 1,
            "street": 1,
            "province": 1,
            "area": 1,
            "bedrooms": 1,
            "bathrooms": 1,
            "level": 1,
            "buildingName": 1,
            "structure": 1,
            "landType": 1,
            "width": 1,
            "length": 1,
            "roadWidth": 1,
        },
    ).sort("_id", 1):
        bid = d.get("id") or d.get("_id")
        typ = d.get("typeCode") or "BDS"
        prefix = f"bds_{bid}_{str(typ).lower()}"
        loc = ", ".join(
            str(x)
            for x in [d.get("street"), d.get("ward"), d.get("district"), d.get("province")]
            if x
        )
        attrs: list[str] = []
        if typ == "CAN_HO":
            attrs = [
                f"tòa {d.get('buildingName') or 'chung cư'}",
                f"tầng {d.get('level')}",
                f"{d.get('bedrooms')}PN {d.get('bathrooms')}WC",
            ]
        elif typ == "NGUYEN_CAN":
            attrs = [d.get("structure") or "nhà phố", f"{d.get('bedrooms')}PN {d.get('bathrooms')}WC"]
        elif typ == "DAT_NEN":
            attrs = [
                str(d.get("landType") or "đất nền"),
                f"{d.get('width')}x{d.get('length')}m",
                f"đường {d.get('roadWidth')}",
            ]
        attrs_s = "; ".join(a for a in attrs if a and "None" not in str(a))
        rows.append((bid, prefix, d.get("name", ""), typ, d.get("area"), loc, attrs_s))

    if len(rows) != 50:
        raise SystemExit(f"expected 50 buildings, got {len(rows)}")

    batch_size = 10
    for bi in range(0, 50, batch_size):
        chunk = rows[bi : bi + batch_size]
        bn = bi // batch_size + 1
        print(f"=== BATCH {bn} ===")
        for _bid, prefix, name, typ, area, loc, attrs_s in chunk:
            print(f"- {prefix}: {name} | {typ} | {area}m2 | {loc} | {attrs_s}")
        print()


if __name__ == "__main__":
    main()
