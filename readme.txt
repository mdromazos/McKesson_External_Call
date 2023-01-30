This is an example of Custom logic for BE Services.
- Custom Validation that requires Person BE to have at least one Address.
- Custom logic that prevents merging of Persons' Telephones

1. Build and deploy bes-external-call.ear
2. Register this web service in Provisioning UI as SOAPService. WSDL: http://localhost:8080/bes-external-call/CustomLogicService?wsdl
3. In Provisioning UI register External Call
    - operation: "validate"
    - Business Entity: "Person"
    - Service Phases: "MergeCO.BeforeEverything", "PreviewMergeCO.BeforeEverything", "WriteCO.BeforeValidate"
4. Try to create "Person" without address:

    POST /cmx/<ors>/Person?systemName=Admin
    {
        firstName: "John"
    }

   Validation error will be returned. The valid request would be:

    POST /cmx/<ors>/Person?systemName=Admin
    {
        firstName: "John",
        Addresses: {
            item: [
                {
                    cityName: "Toronto"
                }
            ]
        }
    }

5. Create two Persons with Addresses and Telephone numbers. (Execute the following request twice)

    POST /cmx/<ors>/Person?systemName=Admin
    {
        firstName: "John",
        Addresses: {
            item: [
                {
                    cityName: "Toronto"
                }
            ]
        },
        TelephoneNumbers: {
            item:[
                {
                    phoneNum: "111-11-11"
                }
            ]
        }
    }

    Response will contain rowidObject for
    - Person (ex: 341922,341923)
    - Addresses (ex: 102122, 102123)
    - TelephoneNumers (ex: 81721, 81722)
    Execute Preview Merge call for these two persons:

    {
        keys: [
            {
                rowid: "341922"
            }
        ]
    }

    Response is a one Person BE with two Addresses and two Telephone numbers.
    Add "overrides" to preview request to merge addresses and telephones:

    POST /cmx/<ors>/Person?systemName=Admin
    {
        keys: [
            {
                rowid: "341922"
            }
        ],
        overrides: {
            Person: {
                Addresses: {
                    item:[
                        {
                            rowidObject: "102123",
                            MERGE: {
                                item:[{key:{rowid: "102122"}}],
                                $original: {
                                    item:[null]
                                }
                            }
                        }
                    ]
                },
                TelephoneNumbers: {
                    item:[
                        {
                            rowidObject: "81722",
                            MERGE: {
                                item:[{key:{rowid: "81721"}}],
                                $original: {
                                    item:[null]
                                }
                            }
                        }
                    ]
                }
            }
        }
    }

    Response will show merged Addresses, but not merged Telephones