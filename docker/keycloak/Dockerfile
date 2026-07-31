FROM quay.io/keycloak/keycloak:21.1.1

USER root

COPY init-realm.sh /opt/keycloak/data/import/init-realm.sh
RUN chmod +x /opt/keycloak/data/import/init-realm.sh

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev", "--import-realm"]