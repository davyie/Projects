json_data=$1
curl -X POST http://localhost:8080/command/add \
     -H "Content-Type: application/json" \
     -d "$json_data"
