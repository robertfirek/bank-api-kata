# Bank API Kata

## Prepare standalone Jar

`./gradlew build`

## Start application from the command line

`java -jar ./build/libs/bank-kata-fat-1.0.jar [<account_definition> <account_definition> ...>]`

where:
- `<account_definition>` - the account definition in the format `<account_number>,<account_balance>`
- `<account_number>` - an unique identifier of the accoun. Type: integer.
- `<account_balance>` - the balance of the account in the format `dd.dd` e.g. `0.00`, `10.23`. Type: decimal.

Example:
`java -jar ./build/libs/bank-kata-fat-1.0.jar 123,10.23 987,32.45`

Above application is going to have two account : 
- the account with the account number `123` and the balance of `10.23` 
- the account with the account number `987` and the balance of `32.45`

Localhost address: `http://localhost:4567/<api_endpoint>`.

## Definition of the API

### Balance: 
`GET /account/{accountNumber}/balance`

### Transfer:
`POST /account/{sourceAccountNumber}/transfer/{targetAccountNumber}`

Payload:
```
{
  "amount": <transfer_amount_as_integer>
}
```

More details in the file `API.openapi.yaml` (to see more user-friendly view use https://editor.swagger.io/)
