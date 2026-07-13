import csv
import random

random.seed(42)

# Base merchants per category — Pakistani + international mix
BASE = {
    "Groceries": [
        "Al-Fatah", "Imtiaz Super Market", "Carrefour", "Metro Cash and Carry",
        "Chase Up", "Green Valley", "Springs Store", "Jalal Sons", "CSD Store",
        "Rainbow Cash and Carry", "Madina Cash and Carry", "Utility Store",
        "Punjab Cash and Carry", "Save Mart", "Euro Store", "D Mart",
    ],
    "Food": [
        "KFC", "McDonalds", "Pizza Hut", "Dominos Pizza", "Subway", "Hardees",
        "OPTP", "Cheezious", "Howdy", "Kababjees", "Student Biryani",
        "Bundu Khan", "Cafe Zouk", "Gloria Jeans", "Dunkin Donuts",
        "Burger King", "Johnny and Jugnu", "Monal Restaurant", "Savour Foods",
        "Chaaye Khana", "Tandoori Hut", "Biryani Express",
    ],
    "Transport": [
        "Careem", "Uber", "InDrive", "Bykea", "Daewoo Express", "Faisal Movers",
        "Pakistan Railways", "Metro Bus", "Speedo Bus", "Airlift",
        "Rent a Car Services", "Skyways",
    ],
    "Fuel": [
        "Shell", "PSO", "Total Parco", "Attock Petroleum", "Hascol",
        "Byco Petroleum", "Gasoline Service Station", "Caltex",
        "Puma Energy", "Euro Oil", "CNG Station",
    ],
    "Shopping": [
        "Almirah", "Khaadi", "Gul Ahmed", "Sapphire", "Nishat Linen",
        "Outfitters", "Bonanza Satrangi", "J. Junaid Jamshed", "Bata",
        "Service Shoes", "Stylo", "Sana Safinaz", "Alkaram Studio",
        "Levis Store", "Nike Store", "Daraz", "Lucky One Mall",
        "Packages Mall", "Dolmen Mall", "Centaurus Mall", "Chen One",
        "Ideas by Gul Ahmed", "Borjan", "Ndure",
    ],
    "Utilities": [
        "LESCO", "K-Electric", "IESCO", "GEPCO", "SNGPL", "SSGC",
        "PTCL", "Jazz", "Telenor", "Zong", "Ufone", "Nayatel",
        "StormFiber", "Wateen", "Water and Sanitation Agency",
    ],
    "Health": [
        "Shaukat Khanum Hospital", "Agha Khan Hospital", "Servaid Pharmacy",
        "Fazal Din Pharma", "D Watson Chemist", "Clinix Pharmacy",
        "Shifa Hospital", "Doctors Hospital", "Chughtai Lab", "Excel Labs",
        "Islamabad Diagnostic Centre", "Green Pharmacy", "Medicare Hospital",
    ],
    "Other": [
        "Cinepax Cinema", "Nueplex Cinema", "Arena Bowling", "Sozo Water Park",
        "Book Corner", "Readings Bookstore", "Liberty Books", "Photo Studio",
        "Barber Shop", "Salon Services", "Gym Fitness Club", "Toy Shop",
    ],
}

# Suffixes/prefixes that appear on real receipts
DECORATIONS = [
    "{}", "{} PVT LTD", "{} (PVT) LTD", "{} STORE", "{} SUPERSTORE",
    "{} - LAHORE", "{} KARACHI", "{} ISLAMABAD", "{} RAWALPINDI",
    "{} BRANCH 2", "{} OUTLET", "{} DHA", "{} GULBERG", "{} MALL",
    "{} F-10", "{} EXPRESS", "{} #23", "{} SADDAR",
]

def ocr_noise(name: str) -> str:
    """Simulate common OCR errors so the model learns to tolerate them."""
    if len(name) < 5 or random.random() < 0.55:
        return name
    s = list(name)
    op = random.random()
    i = random.randrange(1, len(s) - 1)
    if op < 0.4:            # substitute a char (like Serviee)
        s[i] = random.choice("abcdefghijklmnopqrstuvwxyz")
    elif op < 0.7:          # drop a char
        del s[i]
    else:                   # stray leading char (like IHamsafar)
        s.insert(0, random.choice("IL1|"))
    return "".join(s)

rows = []
for category, merchants in BASE.items():
    for merchant in merchants:
        for deco in DECORATIONS:
            name = deco.format(merchant)
            # original + noisy variant, in different cases
            for variant in [name, ocr_noise(name)]:
                case = random.choice([str.upper, str.lower, str.title])
                rows.append((case(variant), category))

random.shuffle(rows)

with open("merchants.csv", "w", newline="", encoding="utf-8") as f:
    writer = csv.writer(f)
    writer.writerow(["merchant", "category"])
    writer.writerows(rows)

print(f"Wrote {len(rows)} rows across {len(BASE)} categories")
