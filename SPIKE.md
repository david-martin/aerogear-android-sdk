# GraphQL Spike

Run the starwars graphql server example
```
git clone https://github.com/apollographql/starwars-server
cd starwars-server
npm i
npm start
```

Fetch the schema from the server
```
cd aerogear-android-sdk
./fetch_graphql_schema.sh
```

Update the URL with your local IP address in `GraphQLFragment`

```
    private static final String BASE_URL = "http://192.168.42.1:8080/graphql/";
    private static final String SUBSCRIPTION_BASE_URL = "ws://192.168.42.1:8080/subscriptions";
```

Build and Run the example Android App


