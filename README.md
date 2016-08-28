# Library management system

One time server preparation (now only installs java 8)
```sh
ansible-playbook ansible/playbooks/web-prepare.yml
```

One time production config setup (now only generate and install secret)
```sh
ansible-playbook -e "secret=`sbt -Dsbt.log.noformat=true 'set showSuccess:=false' playGenerateSecret | tail -n 1 | cut -d ' ' -f 5-`" ansible/playbooks/web-production.yml
```

Deploy web server
```sh
sbt stage && ansible-playbook ansible/playbooks/web-deploy.yml
```
