#!/bin/bash
#
# Start script for advanced-company-search-consumer

PORT=8080

exec java -jar -Dserver.port="${PORT}" "advanced-company-search-consumer.jar"