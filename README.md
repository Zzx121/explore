# explore
MicroService deployed in k8s, hot reload and debug directly in the container.

## Architecture
This project mainly contains two modules(by now), one for operations with databases, such as MySql and Redis, which is under db folder.
Another module is for excel, contains some concurrency implements, which is under execl folder.

## How to deploy
Each module needs to deploy in k8s, files need for deployment is under src/main/k8s folder. e.g. for db module, follow these steps:
1. Ensure your docker and k8s service is up
2. Create generic secret for MySql root pass by this command:
> kubectl create secret generic mysql-pass --from-file=root-pass
3. Create MySql initial database configmap by execute this command:
> kubectl apply -f mysql-init-configmap.yml
4. Create PVC, services and deployments by execute this command:
> kubectl apply -f k8s.yml

## About hot reload and remote debug in container
### Related tech:
* SpringBoot devtools
* Idea's remote-debug [Debug your Java applications in Docker using IntelliJ IDEA](https://blog.jetbrains.com/idea/2019/04/debug-your-java-applications-in-docker-using-intellij-idea/)
* Oketo [Local development experience for Kubernetes apps ](https://github.com/okteto/okteto)

### How to start
1. Install Okteto CLI [Install the Okteto CLI](https://okteto.com/docs/getting-started/index.html#step-1-install-the-okteto-cli)
2. After deployments are successfully up, execute command, e.g. for db module:
> okteto up
3. After okteto is up, execute command:
> mvn spring-boot:run
4. Config remote debug in your ide, and start the remote debug. Then you can develop and debug your project in the running container,
but without the image build, push and redeployment.

## Features
### db module
* MySql
Batch insert:
Threre are three solotions:
1. Simple loop, not recommend, because there are too many round trips.
2. Set ExecutorType.BATCH when openSession, not obvious improvement in my test.
3. Generate big insert sql by Mybatis's <foreach>(need to devide into several sublist), compare pre two, there's signifcant improvement of execution time. Approximately about 20~30 times less time consumption when total data amount is about 100k. For further improvement, reduce the single insert statement's fields and use default value instead as possible.
  
* Redis
Also about batch insert:
There's two solutions:
1. Invoke the rightPushAll() method, in this way, passed list will be serialized into byte[][] as whole, than send to redis server one time. This is the fasted way to store big volume data into redis, about 0.5s for 100k, but you can't seperate objects, but you can only deserialize as whole.
2. Invoke execute() method, for loop send each object in the list, store each object as json into list. For performance, enable pipeline to reduce round trip. That way, about 2~3s for 100k.

* About transaction:
Because I use redis for data temporary store, when the data is restored in MySql, data in redis is no longer useful. So I need to delete the data after restore, but in the meantime, there may be unsuccess, so there a transaction needed. Because there's no roll back in redis's transaction, so I need to trigger deletion of the key after successfully restore data into MySql.

