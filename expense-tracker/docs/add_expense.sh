curl -X POST http://localhost:8080/command/add \
     -H "Content-Type: application/json" \
     -d '{
           "id": 1,
           "name": "Sample Name",
           "amount": 12.50,
           "description": "Some description text"
         }'
