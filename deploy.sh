#!/bin/sh

#asadmin stop-domain

rm -f target/*.war

mvn package

asadmin start-domain # --debug

asadmin set configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.websockets-support-enabled=true
#asadmin set configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.comet-support-enabled=true

asadmin deploy --force --contextroot=/ target/stickies.war

#open http://localhost:8080
