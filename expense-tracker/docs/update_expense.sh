curl -X PUT http://localhost:8080/command/update \
     -H "Content-Type: application/json" \
     -d '{
           "id": 1,
           "name": "Sample Name",
           "amount": 30.2,
           "description": "New description text"
         }'
