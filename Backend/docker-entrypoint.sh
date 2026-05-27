#!/bin/sh
set -e

# Transform DATABASE_URL from postgresql:// to jdbc:postgresql://
if [ -n "$DATABASE_URL" ]; then
  case "$DATABASE_URL" in
    jdbc:*)
      # Already in JDBC format
      export SPRING_DATASOURCE_URL="$DATABASE_URL"
      ;;
    postgresql://*)
      # Transform postgresql:// to jdbc:postgresql://
      export SPRING_DATASOURCE_URL="jdbc:$DATABASE_URL"
      ;;
    postgres://*)
      # Transform postgres:// to jdbc:postgresql://
      export SPRING_DATASOURCE_URL="jdbc:postgresql://${DATABASE_URL#postgres://}"
      ;;
    *)
      export SPRING_DATASOURCE_URL="$DATABASE_URL"
      ;;
  esac
  
  echo "✓ Database URL configured for JDBC"
fi

# Execute the Java application
exec java \
  -Dspring.profiles.active=prod \
  -Dserver.port=${PORT:-8080} \
  -Djava.security.egd=file:/dev/./urandom \
  -XX:+UseContainerSupport \
  -XX:+MaxRAMPercentage=75.0 \
  -jar app.jar
