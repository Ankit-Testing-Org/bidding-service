#!/bin/bash
# Sleep to allow Keycloak to start
sleep 10

REALM_NAME=master
CLIENT_ID=car-sticker
USERNAME=admin
PASSWORD=admin

# Create realm JSON
cat <<EOF > /opt/keycloak/data/import/${REALM_NAME}-realm.json
{
  "realm": "${REALM_NAME}",
  "enabled": true,
  "clients": [
    {
      "clientId": "${CLIENT_ID}",
      "publicClient": false,
      "secret": "api-secret",
      "directAccessGrantsEnabled": true,
      "protocol": "openid-connect"
    }
  ],
  "users": [
    {
      "username": "${USERNAME}",
      "enabled": true,
      "credentials": [
        {
          "type": "password",
          "value": "${PASSWORD}"
        }
      ]
    }
  ]
}
EOF

# Import the realm
/opt/keycloak/bin/kc.sh import --file /opt/keycloak/data/import/${REALM_NAME}-realm.json