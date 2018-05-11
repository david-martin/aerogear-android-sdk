#!/bin/bash
mkdir -p ./example/src/main/graphql/org/aerogear/mobile/example
apollo-codegen download-schema http://localhost:8080/graphql --output ./example/src/main/graphql/org/aerogear/mobile/example/schema.json

