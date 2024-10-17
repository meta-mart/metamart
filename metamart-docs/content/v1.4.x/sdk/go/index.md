---
title: Go SDK
slug: /sdk/go
---

# Go SDK 

We now present a high-level Go API as a gentle wrapper to interact with the MetaMart API.

The open-source MetaMart SDK for Go simplifies provisioning, managing, and using MetaMart resources from the Go application code. \
The MetaMart SDK for Go modules build on top of the underlying MetaMart REST API, allows you to use those APIs through familiar Go paradigms.

You can find the source code for the MetaMart libraries in the [GitHub repository](https://github.com/meta-mart/metamart-sdk/tree/main/metamart-go-client). As an open-source project, contributions are always welcome!

You can add the module to your application with the below command

```sh
go get github.com/meta-mart/metamart-sdk/metamart-go-client
```

## Establish MetaMart Server Connection

To create MetaMart Gateway, you will need to establish a connection with *MetaMart Server*. The following inputs will be needed:
* `BaseURL`: The url on which your instance of MetaMart is up and running (include the port if you need to e.g. http://localhost:8585).
* `APIVersion`: pass an empty string -- this will be `v1` for now.
* `Retry`: number of time the request should retry if the status code returned is in `RetryCodes`. Use `0` to use the default value
* `RetryWait`: number of second to wait betwee retries. Pass 0 to use the default value
* `RetryCodes`: HTTP status that will trigger a retry. Pass `nil` to use the default
* `AuthTokenMode`: defaults to `Bearer`
* `AccessToken`: JWT token use to authenticate the request

```go
// main.go
package main

import (
	"github.com/meta-mart/metamart-sdk/metamart-go-client/pkg/ometa"
)

func main() {
    restConfig := ometa.NewRestConfig(
		"http://localhost:8585",
		"", // APIVersion
		0, // Retry -> defaults to 3
		0, // RetryWait -> defaults to 30 seconds
		nil, // RetryCodes -> defaults to [429, 504] 
		"JWTToken",
	)
}
```


## Create MetaMart Gateway

Once the connection details are provided, you can create an MetaMart Gateway
using the following piece of code.

```go
// main.go
...

func main() {
    ...
    rest := ometa.NewRest(restConfig)
}
```

## How to Use APIs Using the Java Client
Once you have instantiated a `Rest` object you can call any of the 4 methods to perform any kind of operation against MetaMart API

### `Get` method
**Method signature**
```go
func (rest Rest) Get(
	path string,
	header http.Header,
	extraHeader map[string][]string,
	queryParams map[string]string) (map[string]any, error)
```

**Example**
```go
// main.go
...
func main() {
    ...
    path := "tables"
	body, err := rest.Get(path, nil, nil, nil)
}
```


### `Post` method
**Method signature**
```go
func (rest Rest) Post(
	path string,
	data map[string]interface{},
	header http.Header,
	extraHeader map[string][]string,
	queryParams map[string]string) (map[string]any, error)
```

**Example**
```go
// main.go
...
func main() {
    ...
    path := "tables"
    data := map[string]interface{}{
        "name":           "goClientTestTable",
        "databaseSchema": "sample_data.ecommerce_db.shopify",
        "columns": []map[string]interface{}{
            {
                "name":     "columnOne",
                "dataType": "NUMBER",
            },
            {
                "name":     "columnTwo",
                "dataType": "NUMBER",
            },
        },
    }
	body, err := rest.Post(path, data, nil, nil, nil)
}
```

### `Put` method
**Method signature**
```go
func (rest Rest) Put(
	path string,
	data map[string]interface{},
	header http.Header,
	extraHeader map[string][]string,
	queryParams map[string]string) (map[string]any, error)
```

**Example**
```go
// main.go
...
func main() {
    ...
    path := "tables"
	data := map[string]interface{}{
		"name":           "goClientTestTable",
		"databaseSchema": "sample_data.ecommerce_db.shopify",
		"columns": []map[string]interface{}{
			{
				"name":     "columnOne",
				"dataType": "NUMBER",
			},
			{
				"name":     "columnTwo",
				"dataType": "NUMBER",
			},
			{
				"name":     "columnThree",
				"dataType": "NUMBER",
			},
		},
	}
	body, err := rest.Put(path, data, nil, nil, nil)
}
```

### `Patch` method
**Method signature**
```go
func (rest Rest) Patch(
	path string,
	data []map[string]interface{},
	header http.Header,
	extraHeader map[string][]string,
	queryParams map[string]string) (map[string]any, error)
```

**Example**
```go
// main.go
...
func main() {
    ...
    path := "tables"
	patchPath := fmt.Sprintf("tables/%s", id)
	data := []map[string]interface{}{
		{
			"op":    "add",
			"path":  "/description",
			"value": "This is a test table",
		},
	}
	body, err := rest.Patch(path, data, nil, nil, nil)
}
```

### `Delete` method
**Method signature**
```go
func (rest Rest) Delete(
	path string,
	header http.Header,
	extraHeader map[string][]string,
	queryParams map[string]string) (map[string]any, error)
```

**Example**
```go
// main.go
...
func main() {
    ...
	path := fmt.Sprintf("tables/%s", id)
	queryParams := map[string]string{
		"hardDelete": "true",
		"recursive":  "true",
	}
	rest.Delete(path, nil, nil, queryParams)
}
```
