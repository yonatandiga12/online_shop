# Read this!
## In order to make sure we dont have different versions, everyone use open jdk 19
## Remember to add comments, logs, and unit tests

## Configuration

The configuration data for the database and initialization can be set in the `config.properties` file. Here is an example:

```properties
InitializePath=initData/data.json
DB_FIELD1=relative/path/to/DB.db
```

## Data Format

This is the format. It represents data with 2 fields:

1. `List<string>`: a list of admins to add
2. `List<RegisterUser>`: a list of registered users

Each user has the following properties:
- `List<Store>`: a list of stores

Each store has the following properties:
- `List<Item>`: a list of items
- `List<String> newOwnersToAdd`: a list of new owners to add
- `List<String> newManagersToAdd`: a list of new managers to add
- If user showed again then its for owner who is not the founder to add new owner.

- #### Please note that this is not a database, but an initialization structure.

- #### Admin and guest users are built-in in the system.

## Example (JSON Code)

```json
{
  "adminsList": ["Amir", "Tomer"],
  "registeredUserList": [
    {
      "username": "Sagi",
      "password": "sagisPass",
      "address": "addressOk",
      "bDay": "1999-07-11",
      "stores": [
        {
          "founderName": "Sagi",
          "storeName": "Sagi1 Store",
          "ownersList": ["Tomer", "Amir"],
          "managersList": ["Yonatan"],
          "itemList": [
            {
              "itemName": "Eggs",
              "itemPrice": 4.5,
              "itemCategory": "Dairy",
              "weight": 5.0,
              "amount": 20
            }
          ]
        }
      ]
    }
  ]
}
```

