#!/bin/bash

tables=("country" "disease" "disease_case" "location" "region")

mkdir -p exported_data

# Export CSV from container
for table in "${tables[@]}"; do
  echo "Exporting $table..."
  docker exec mspr2-db psql -U mspr2 -d mspr2db -c "\COPY public.$table TO '/tmp/$table.csv' WITH CSV HEADER"
done

# Copy CSV files to local machine
for table in "${tables[@]}"; do
  echo "Copying $table.csv to exported_data folder..."
  docker cp mspr2-db:/tmp/$table.csv ./exported_data/$table.csv
done

echo "✅ Tous les fichiers CSV ont été exportés avec succès."
