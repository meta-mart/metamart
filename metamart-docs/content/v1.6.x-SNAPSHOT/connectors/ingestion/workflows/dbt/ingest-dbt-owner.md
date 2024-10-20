---
title: Ingest Owner from dbt
slug: /connectors/ingestion/workflows/dbt/ingest-dbt-owner
---

# Ingest Owner from dbt

Ingest the model/table owner information from dbt `manifest.json` or `catalog.json` file into metamart tables.

The owner can be a user or a team in MetaMart.

Follow the link [here](https://docs.getdbt.com/reference/resource-configs/meta) to add the owner to the dbt project's `schema.yml` file

## Requirements

### 1. Owner information in manifest.json file
Metamart fetches the owner information from the `manifest.json` file. Below is a sample `manifest.json` file node containing owner information under `node_name->metadata->owner`.

```json
"model.jaffle_shop.orders": {
			"metadata": {
				"type": "BASE TABLE",
				"schema": "dbt_jaffle",
				"name": "orders",
				"database": "dev",
				"comment": null,
				"owner": "metamart_team"
			}
}
```

### 2. Owner information in catalog.json file
Metamart fetches the owner information from the `catalog.json` file. Below is a sample `catalog.json` file node containing owner information under `node_name->metadata->owner`.

```json
"model.jaffle_shop.customers": {
      "metadata": {
        "type": "BASE TABLE",
        "schema": "dbt_jaffle",
        "name": "customers",
        "database": "dev",
        "comment": null,
        "owner": "metamart"
      },
}
```

### 3. Adding the User or Team to MetaMart
The user or team which will be set as the entity owner should be first created in MetaMart.

While linking the owner from `manifest.json` or `catalog.json` files to the entity, MetaMart first searches for the user if it is present. If the user is not present it searches for the team 

#### Following steps shows adding a User to MetaMart:
**1.** Click on the `Users` section from homepage

{% image
  src="/images/v1.5/features/ingestion/workflows/dbt/ingest_dbt_owner/click-users-page.webp"
  alt="click-users-page"
  caption="Click Users page"
 /%}


**2.** Click on the `Add User` button

{% image
  src="/images/v1.5/features/ingestion/workflows/dbt/ingest_dbt_owner/click-add-user.webp"
  alt="click-add-user"
  caption="Click Add User"
 /%}


3. Enter the details as shown for the user

{% note %}

If the owner's name in `manifest.json` or `catalog.json` file is `metamart`, you need to enter `metamart@youremail.com` in the email id section of add user form as shown below.

{% /note %}

{% image
  src="/images/v1.5/features/ingestion/workflows/dbt/ingest_dbt_owner/add-user-dbt.webp"
  alt="add-user-dbt"
  caption="Add User"
 /%}


#### Following steps shows adding a Team to MetaMart:
**1.** Click on the `Teams` section from homepage

{% image
  src="/images/v1.5/features/ingestion/workflows/dbt/ingest_dbt_owner/click-teams-page.webp"
  alt="click-teams-page"
  caption="Click Teams page"
 /%}

**2.** Click on the `Add Team` button

{% image
  src="/images/v1.5/features/ingestion/workflows/dbt/ingest_dbt_owner/click-add-team.webp"
  alt="click-add-team"
  caption="Click Add Team"
 /%}


**3.** Enter the details as shown for the team

{% note %}

If the owner's name in `manifest.json` or `catalog.json` file is `metamart`, you need to enter `metamart` in the name section of add team form as shown below.

{% /note %}

{% image
  src="/images/v1.5/features/ingestion/workflows/dbt/ingest_dbt_owner/add-team-dbt.webp"
  alt="add-team-dbt"
  caption="Add Team"
 /%}


## Linking the Owner to the table

After running the ingestion workflow with dbt you can see the created user or team getting linked to the table as it's owner as it was specified in the `manifest.json` or `catalog.json` file.

{% image
  src="/images/v1.5/features/ingestion/workflows/dbt/ingest_dbt_owner/linked-user.webp"
  alt="linked-user"
  caption="Linked User"
 /%}



{% note %}

If a table already has a owner linked to it, owner from the dbt will not update the current owner.

{% /note %}
