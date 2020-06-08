# explore
No particular theme, just applications of some ideas basing on java stack.

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
> okteto.yml
3. After okteto is up, execute command:
> mvn spring-boot:run
4. Config remote debug in your ide, and start the remote debug. Then you can develop and debug your project in the running container,
but without the image build, push and redeployment.
