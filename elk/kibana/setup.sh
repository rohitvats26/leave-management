#!/bin/bash
set -e

KIBANA_URL="http://kibana:5601"
ES_URL="http://elasticsearch:9200"

echo "Waiting for Kibana to be ready..."
until curl -s "$KIBANA_URL/api/status" | grep -q '"level":"available"'; do
  echo "  Kibana not ready yet — retrying in 5s"
  sleep 5
done

echo "Kibana ready. Creating index pattern lms-*..."

curl -s -X POST "$KIBANA_URL/api/saved_objects/index-pattern/lms-star" \
  -H "kbn-xsrf: true" \
  -H "Content-Type: application/json" \
  -d '{
    "attributes": {
      "title":         "lms-*",
      "timeFieldName": "@timestamp"
    }
  }' | cat

echo ""
echo "Setting lms-* as the default index pattern..."
curl -s -X POST "$KIBANA_URL/api/kibana/settings" \
  -H "kbn-xsrf: true" \
  -H "Content-Type: application/json" \
  -d '{"changes": {"defaultIndex": "lms-star"}}' | cat

echo ""
echo "Index template for lms-* in Elasticsearch..."
curl -s -X PUT "$ES_URL/_index_template/lms-template" \
  -H "Content-Type: application/json" \
  -d '{
    "index_patterns": ["lms-*"],
    "template": {
      "settings": {
        "number_of_shards":   1,
        "number_of_replicas": 0
      },
      "mappings": {
        "properties": {
          "@timestamp":  { "type": "date" },
          "level":       { "type": "keyword" },
          "service":     { "type": "keyword" },
          "logger":      { "type": "keyword" },
          "message":     { "type": "text" },
          "thread":      { "type": "keyword" },
          "env":         { "type": "keyword" }
        }
      }
    }
  }' | cat

echo ""
echo "✅ ELK setup complete. Open http://localhost:5601 → Discover → lms-*"
