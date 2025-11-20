json_data=$1
curl -X DELETE http://localhost:8080/command/delete \
     -H "Content-Type: text/plain" \
     -d "$json_data"
