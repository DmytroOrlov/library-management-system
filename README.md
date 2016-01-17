# Library management system

Deploy web server
```sh
sbt universal:packageZipTarball && ansible-playbook -i ansible/hosts ansible/playbooks/web-deploy.yml
```
