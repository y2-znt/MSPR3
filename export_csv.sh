#!/bin/bash

tables=("country" "disease" "disease_case" "location" "region")

# Export CSV from container
for table in "${tables[@]}"; do
  echo "Exporting $table..."
  docker exec mspr2-db psql -U mspr2 -d mspr2db -c "\COPY public.$table TO '/tmp/$table.csv' WITH CSV HEADER"
done

# Copy CSV files to local machine
for table in "${tables[@]}"; do
  echo "Copying $table.csv to local machine..."
  docker cp mspr2-db:/tmp/$table.csv ./$table.csv
done

echo "✅ Tous les fichiers CSV ont été exportés avec succès."
