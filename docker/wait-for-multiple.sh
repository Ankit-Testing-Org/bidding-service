#!/bin/sh
# wait-for-multiple.sh
# Usage: ./wait-for-multiple.sh host1:port1 host2:port2 ... -- command args

set -e

TIMEOUT=30
SERVICES=()
COMMAND=()

# Parse args
while [ $# -gt 0 ]; do
  case "$1" in
    --)
      shift
      COMMAND="$@"
      break
      ;;
    *)
      SERVICES+=("$1")
      shift
      ;;
  esac
done

# Wait for each service
for SERVICE in "${SERVICES[@]}"; do
    HOST=$(echo $SERVICE | cut -d: -f1)
    PORT=$(echo $SERVICE | cut -d: -f2)

    echo "Waiting for $HOST:$PORT..."
    for i in $(seq $TIMEOUT); do
        nc -z "$HOST" "$PORT" >/dev/null 2>&1 && break
        sleep 1
    done

    nc -z "$HOST" "$PORT" >/dev/null 2>&1 || {
        echo "Timeout waiting for $HOST:$PORT"
        exit 1
    }
    echo "$HOST:$PORT is available"
done

# Execute command
if [ -n "$COMMAND" ]; then
    exec $COMMAND
fi