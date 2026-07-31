#!/bin/bash
set -e

TIMEOUT=30
SERVICES=()
COMMAND=()

# Parse args
while [[ $# -gt 0 ]]; do
  case "$1" in
    --)
      shift
      COMMAND=("$@")
      break
      ;;
    *)
      SERVICES+=("$1")
      shift
      ;;
  esac
done

# Wait for services
for SERVICE in "${SERVICES[@]}"; do
    HOST=$(echo $SERVICE | cut -d: -f1)
    PORT=$(echo $SERVICE | cut -d: -f2)

    echo "Waiting for $HOST:$PORT..."

    for ((i=0;i<TIMEOUT;i++)); do
        if nc -z "$HOST" "$PORT" >/dev/null 2>&1; then
            echo "$HOST:$PORT is available"
            break
        fi
        sleep 1
    done

    if ! nc -z "$HOST" "$PORT" >/dev/null 2>&1; then
        echo "Timeout waiting for $HOST:$PORT"
        exit 1
    fi
done

# Run app
exec "${COMMAND[@]}"