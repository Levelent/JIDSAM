import pandas as pd
from datetime import datetime

# Get dictionary lookup of Pickup (PU) and Dropoff (DO) location IDs
with open("zone_lookup.csv") as file:
    # get the info we actually want
    locations = [line.split(",")[:3] for line in file.read().replace('"', "").split("\n")[1:]]

location_dict = {}  # useful for main data set
borough_dict = {}  # useful for dgh generation
for id, borough, zone in locations:
    location_dict[int(id)] = zone

    if borough in borough_dict:
        borough_dict[borough].append(zone)
    else:
        borough_dict[borough] = [zone]

del locations

with open("yellow_tripdata_2021-01.csv") as file:
    df = pd.read_csv(file, low_memory=False)
    # lines = file.read().split("\n")

# df = pd.DataFrame(data=table[1:], columns=table[0])
df.dropna(inplace=True)
df.drop(columns=["VendorID", "RatecodeID", "store_and_fwd_flag", "improvement_surcharge",
        "tolls_amount", "mta_tax", "extra", "congestion_surcharge"], inplace=True)

# Convert columns into other formats

# Pickup and Dropoff IDs converted to string
df["PULocationID"] = df["PULocationID"].map(location_dict.get)
df["DOLocationID"] = df["DOLocationID"].map(location_dict.get)

# ISO timestamp to integer unix timestamp
df["tpep_pickup_datetime"] = df["tpep_pickup_datetime"].map(
    lambda x: int(datetime.fromisoformat(x).timestamp()))
df["tpep_dropoff_datetime"] = df["tpep_dropoff_datetime"].map(
    lambda x: int(datetime.fromisoformat(x).timestamp()))

# Make passenger count an integer
df["passenger_count"] = df["passenger_count"].map(int)

# Payment type conversion
pay_type_dict = {1: "Credit Card", 2: "Cash", 3: "No charge",
                 4: "Dispute", 5: "Unknown", 6: "Voided trip"}
df["payment_type"] = df["payment_type"].map(pay_type_dict.get)


# 1271413 available to sample

df.index.name = "pid"

df.sample(n=1000000).to_csv("taxi-1000000.csv")
df.sample(n=100000).to_csv("taxi-100000.csv")
df.sample(n=10000).to_csv("taxi-10000.csv")
df.sample(n=1000).to_csv("taxi-1000.csv")
df.sample(n=100).to_csv("taxi-100.csv")

# Create DGH file with correct format

dgh_zones = ["New York"]
for borough in borough_dict.keys():
    dgh_zones.append(f"    {borough}")
    for zone in borough_dict[borough]:
        dgh_zones.append(f"        {zone}")

# Awkward, but no easy way to automate
s = "    "
dgh_pay_type = f"$payment_type\nAny\n{s}Paid\n{s*2}Credit Card\n{s*2}Cash\n{s}Unpaid\n{s*2}No charge\n{s*2}Dispute\n{s*2}Voided trip\n{s}Unknown"

with open("dgh.txt", "w") as file:
    file.writelines("\n".join(
        ["$PULocationID"] + dgh_zones +
        ["$DOLocationID"] + dgh_zones +
        [dgh_pay_type]
    ))
