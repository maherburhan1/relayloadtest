# EventBus Java Sample Client
A sample client to consume from the EventBus.

### Execution commands

runClient [-id group_id] [-filter filter_id] [-entity entity_names]

    -id:        Will override the event_bus_group_id with a specific value. 
                If not specified then the value from the reference.conf file is used.
    -filter:    Will only show events with a BizAppsCustomerID or systemGUID matching the filter
    -entity:    Will only show events with the entity names matching the filter. 
                Names must be comma separated no spaces.
  
i.e. The following will connect as "MyTestClientID" and pull only Client events

### Example
```bash
runClient.sh -id MyTestClientID -entity Client
```
