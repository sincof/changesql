connect to the remote server

ssh -i /home/qiu/.ssh/laptop.pem root@121.41.55.205

scp local_file remote_username@remote_ip:remote_folder 

scp -i /home/qiu/.ssh/laptop.pem local_file root@121.41.55.205:/root/project/changesql/

scp -i /home/qiu/.ssh/laptop.pem /home/qiu/Desktop/Project/changesql/tdsql.zip  root@121.41.55.205:/root/project/changesql/