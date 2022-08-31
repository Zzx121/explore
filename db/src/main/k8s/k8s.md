# Some details for k8s config
## Set the root pass for mysql server
1. Create the secret to store the pass 
2. Ref the secret in the deployment config through the "secretKeyRef" by setting the env of mysql container

## Passing the initial sql script into container for start up executing
1. Create the init script in configMap named "mysql-initdb-config"
2. Config the "docker-entrypoint-initdb.d" through volumeMount
3. After that the init script will execute when the mysql server has started

## Waiting for mysql to start up successfully to prevent the dependent service starting fail e.g. db-server
1. Use the initContainers config to achieve this
2. Execute the nslookup to check the mysql service is on
