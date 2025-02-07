# language: en
@DataSet
Feature: Dataset management

    Background:
        Given a dataset is saved
            Do http-post Post dataset to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/v1/datasets
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "name": "my dataset name",
                    "description": "my dataset description",
                    "lastUpdated": "2020-04-30T15:12:42.285Z",
                    "tags": [ "TAG1", "TAG2" ],
                    "uniqueValues": [
                        { "key": "uk1", "value": "uv1" }, { "key": "uk2", "value": "uv2" }
                    ],
                    "multipleValues": [
                        [ { "key": "mk1", "value": "mv11" }, { "key": "mk2", "value": "mv21" }, { "key": "mk3", "value": "mv31" } ],
                        [ { "key": "mk1", "value": "mv12" }, { "key": "mk2", "value": "mv22" }, { "key": "mk3", "value": "mv32" } ],
                        [ { "key": "mk1", "value": "mv13" }, { "key": "mk2", "value": "mv23" }, { "key": "mk3", "value": "mv33" } ]
                    ]
                }
                """
                Take datasetId ${#jsonPath(#body, "$.id")}
                Validate httpStatusCode_200 ${#status == 200}

    Scenario: Find existing dataset
        When search for the dataset
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/v1/datasets/${#datasetId}
                Take dataset ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then the dataset is retrieved
            Do compare
                With actual ${#jsonPath(#dataset, "$.name")}
                With expected my dataset name
                With mode equals

    Scenario: Delete existing dataset
        When delete the dataset
            Do http-delete
                On CHUTNEY_LOCAL
                With uri /api/v1/datasets/${#datasetId}
                Validate httpStatusCode_200 ${#status == 200}
        Then the dataset cannot be found
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/v1/datasets/${#datasetId}
                Validate httpStatusCode_404 ${#status == 404}

    Scenario: Versioning
        When a new version is saved
            Do http-post Post dataset to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/v1/datasets
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "id": "${#datasetId}",
                    "name": "new name",
                    "description": "new description",
                    "lastUpdated": "2020-05-01T10:09:00.134Z",
                    "tags": [ "NEW_TAG" ],
                    "uniqueValues": [
                        { "key": "uk1", "value": "new v1" }, { "key": "K3", "value": "uv3" }
                    ],
                    "multipleValues": [
                        [ { "key": "mk1", "value": "mv11" }, { "key": " mk2", "value": "mv21 - end with space " }, { "key": "mk3", "value": "mv31" } ],
                        [ { "key": "mk1", "value": "mv122" }, { "key": "mk2", "value": "mv222" }, { "key": "mk3", "value": "mv322" } ]
                    ]
                }
                """
                Take datasetVersion ${#jsonPath(#body, "$.version")}
                Validate httpStatusCode_200 ${#status == 200}
        And another version without space
            Do http-post Post dataset to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/v1/datasets
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "id": "${#datasetId}",
                    "name": "new name",
                    "description": "new description",
                    "lastUpdated": "2020-05-02T10:09:00.134Z",
                    "tags": [ "NEW_TAG" ],
                    "uniqueValues": [
                        { "key": "uk1", "value": "new v1" }, { "key": "K3", "value": "uv3" }
                    ],
                    "multipleValues": [
                        [ { "key": "mk1", "value": "mv11" }, { "key": "mk2", "value": "mv21 - without space" }, { "key": "mk3", "value": "mv31" } ],
                        [ { "key": "mk1", "value": "mv122" }, { "key": "mk2", "value": "mv222" }, { "key": "mk3", "value": "mv322" } ]
                    ]
                }
                """
                Take datasetVersion ${#jsonPath(#body, "$.version")}
                Validate httpStatusCode_200 ${#status == 200}
        And another new version
            Do http-post Post dataset to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/v1/datasets
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "id": "${#datasetId}",
                    "name": "new name",
                    "description": "new description",
                    "lastUpdated": "2020-05-02T11:09:00.134Z",
                    "tags": [ "NEW_TAG" ],
                    "uniqueValues": [
                        { "key": "uk1", "value": "new v1" }, { "key": "K3", "value": "uv3" }
                    ],
                    "multipleValues": [
                        [ { "key": "mk1", "value": "mv11" }, { "key": "mk2", "value": "mv21" }, { "key": "mk3", "value": "mv31" } ],
                        [ { "key": "mk1", "value": "mv122" }, { "key": "mk2", "value": "mv222" }, { "key": "mk3", "value": "mv322" } ]
                    ]
                }
                """
                Take datasetVersion ${#jsonPath(#body, "$.version")}
                Validate httpStatusCode_200 ${#status == 200}
        Then the dataset last version number is 4
            Do compare
                With actual ${T(Integer).toString(#datasetVersion)}
                With expected 4
                With mode equals
        And the list of version numbers is [1,2,3,4]
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/v1/datasets/${#datasetId}/versions
                Take datasetVersionList ${#body}
                Validate httpStatusCode_200 ${#status == 200}
            Do compare Assert version's list
                With actual ${#jsonPath(#datasetVersionList, "$[*].version").toString()}
                With expected [1,2,3,4]
                With mode equals
        And the search for the dataset bring the last version
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/v1/datasets/${#datasetId}
                Validate httpStatusCode_200 ${#status == 200}
            Do compare Assert last version number
                With actual ${#jsonPath(#body, "$.version").toString()}
                With expected 4
                With mode equals
        And the dataset version 2 can be found
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/v1/datasets/${#datasetId}/2
                Validate httpStatusCode_200 ${#status == 200}
            Do compare Assert version number
                With actual ${#jsonPath(#body, "$.version").toString()}
                With expected 2
                With mode equals
