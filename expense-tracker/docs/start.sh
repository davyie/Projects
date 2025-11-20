#!/bin/bash
get_options() {
    echo "Choose one option: "
    echo "1. Add Expense" 
    echo "2. Delete Expense"
    echo "3. Update Expense"
    echo "4. Get All Expenses"
    echo "5. Get Expense by ID"
    echo "6. Save" 
    echo "7. Save and Exit" 
}

json_object() {
  # Usage: json_object key1 value1 key2 value2 ...
  if [ $(( $# % 2 )) -ne 0 ]; then
    echo "Error: arguments must come in key-value pairs" >&2
    return 1
  fi

  printf '{'
  first=true
  while [ $# -gt 0 ]; do
    key=$1
    value=$2
    shift 2

    # Add comma after the first element
    if [ "$first" = false ]; then
      printf ','
    fi
    first=false

    # Safely escape double quotes inside values
    esc_value=$(printf '%s' "$value" | sed 's/"/\\"/g')

    printf '"%s":"%s"' "$key" "$esc_value"
  done
  printf '}\n'
}


add_expense() {
    echo "Please enter expense name"
    read NAME
    echo "Please enter amount" 
    read AMOUNT
    echo "Please enter expense description" 
    read DESCRIPTION
    json=$(json_object name $NAME amount $AMOUNT description DESCRIPTION)
    echo $json
    ./add_expense.sh $json
}

delete_expense() {
  display_expenses
  echo "Please enter the name of expense you want to delete"
  read name
  ./delete_expense.sh $name 
}

display_expenses() {
  json_data=$(./get_all.sh) 
  echo "ID Name"
  echo "$json_data" | jq -r '.[] | "\(.id) \(.name)"'
}

update_expense() {
  display_expenses
  echo "Please enter the id of expense to be update" 
  read EXPENSE_ID
  data=$(get_expense_by_id $EXPENSE_ID)
  NAME=$(echo "$data" | jq -r '.name')
  AMOUNT=$(echo "$data" | jq -r '.amount')
  DESCRIPTION=$(echo "$data" | jq -r '.description')
  echo "What do you want to edit?"
  echo "1) name"
  echo "2) amount"
  echo "3) description"
  read OPTION 
  case $OPTION in 
    1) echo "Edit name"
    read $NAME
    ;;
    2) echo "Edit amount"
    read $AMOUNT
    ;;
    3) echo "Edit description"
    read $DESCRIPTION
    ;;
    *) echo "Invalid option"
    ;;
    esac
    echo $AMOUNT
    json=$(json_object name $NAME amount $AMOUNT description $DESCRIPTION)
    echo $json
    ./update_expense.sh $EXPENSE_ID json
    echo "Updated with the data $json"
}

get_expense_by_id() {
  data=$(./get_expense_by_id.sh $1)
  echo $data 
}


INPUT_STR=0

while [ "$INPUT_STR" != 7 ] 
do 
    get_options
    read INPUT_STR
    case $INPUT_STR in 
        1) add_expense
        ;;
        2) delete_expense
        ;; 
        3) update_expense
        ;; 
        4) ./get_all.sh
        ;; 
        5) get_expense_by_id
        ;; 
        6) echo "Not implemented"
        ;; 
        7) echo "Saving... Good bye!"
        ;;  
        *) echo "Invalid option"
        ;;
    esac
done 