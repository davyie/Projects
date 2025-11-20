ID=$1
INPUT=$2
curl -X PUT http://localhost:8080/command/update?id=$ID \
     -H "Content-Type: application/json" \
     -d "$INPUT"
