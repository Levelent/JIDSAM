# New York Taxi Dataset

Trip Record Data from the New York City Taxi & Limousine Commission, specifically the Yellow Taxi Trip Records CSV from January 2021. The uncleaned file is over GitHub's 100MB file size limit - you can find it [on this page](https://www1.nyc.gov/site/tlc/about/tlc-trip-record-data.page), along with the Taxi Zone Lookup Table and Yellow Trips Data Dictionary.

The following fields are left intact:

- `tpep_pickup_datetime` - MODIFIED: Unix timestamp representing the date and time when the meter was engaged.
- `tpep_dropoff_datetime` - MODIFIED: Unix timestamp representing the date and time when the meter was disengaged.
- `passenger_count` - The number of passengers in the vehicle (driver-entered value).
- `trip_distance` - The elapsed trip distance in miles reported by the taximeter.
- `PULocationID` - MODIFIED: Taxi Zone in which the meter was engaged (see DGH).
- `DOLocationID` - MODIFIED: Taxi Zone in which the meter was disengaged (see DGH).
- `payment_type` - MODIFIED: How the passenger paid for the trip (see DGH).
- `fare_amount` - The time-and-distance fare calculated by the meter, in dollars.
- `tip_amount` - Automatically populated for credit card
tips. Cash tips are not included.
- `total_amount` - The total amount charged to passengers.